package ci;

import ci.Status.CommitRecord;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** CiService is a Spring service for managing the continuous integration pipeline */
@Service
public class CiService {

  /** Shared secret key used to validate Github webhook signatures. */
  @Value("${sharedKey:xxxxxxxx}")
  private String sharedKey;

  /** Full GitHub repository name expected in the webhokk payload. */
  @Value("${git.repoName:daDevBoat/ContinuousIntegration}")
  private String repoName;

  /** Local parent directory where the Git repository will be cloned. */
  @Value("${ci.repoParentDir:not a file}")
  private String repoParentDir;

  /** SSH URL of the Git repository used for cloning and pulling updates. */
  @Value("${ci.repoSsh:git-ssh}")
  String repoSsh;

  /** Local directory name of the repository. */
  @Value("${ci.repoID:ContinuousIntegration}")
  String repoID;

  /** Authentication token used when sending build status notifications. */
  @Value("${server.auth:Invalid auth token}")
  private String authToken;

  /** Target URL where CI build results are reported after processing a webhook. */
  @Value("${local.url:Invalid target url}")
  private String targetUrl;

  private final Status status;

  /**
   * Constructs a CiService with the specified Status service
   *
   * @param status the Status service used to store and retrieve build history
   */
  public CiService(Status status) {
    this.status = status;
  }

  /** Service responsible for compiling the project. */
  private CompilationService compilationService = new CompilationService();

  /**
   * Executes the CI build pipeline for a GitHub webhook push event
   *
   * @param payload the GitHub webhook payload containing repository and commit information
   */
  @Async
  public void runBuild(JsonNode payload) {

    /* Sending pending status back to GitHub */
    GithubAPIHandler apiHandler = new GithubAPIHandler(payload);
    apiHandler.sendPost(
        authToken, targetUrl, "pending", "Starting building and testing (cross your fingers)");

    String sha = payload.path("after").asText("");
    String runDir = repoParentDir + "/test/" + sha;

    /* Building the repo if it doesnt exist yet, and pulling newest changes */
    try {
      RepoSetup.createDir(runDir);
      RepoSetup.cloneRepo(runDir, repoID, repoSsh);
      RepoSetup.updateRepo(runDir, repoID, sha);
    } catch (Exception e) {
      String errorMsg = "";
      switch (e) {
        case IllegalStateException is -> errorMsg = "Git error: Illegal State";
        case IllegalArgumentException ia -> errorMsg = "Error when processing the SHA";
        case IOException io -> errorMsg = "Error when creating the directory";
        default -> errorMsg = "Error when trying to clone remote repo";
      }
      e.printStackTrace();
      apiHandler.sendPost(authToken, targetUrl, "failure", errorMsg);

      try {
        RepoSetup.removeDir(runDir);
      } catch (Exception inner) {
        inner.printStackTrace();
      }
      return;
    }

    File dir = new File(runDir, repoID);

    /* Check if the directory exists */
    if (!dir.isDirectory()) {
      apiHandler.sendPost(
          authToken,
          targetUrl,
          "failure",
          "\"Repo dir was not found '\" + dir.getAbsolutePath() + \"'.\"");
      return;
    }
    try {
      /* Starts the compilation */
      System.out.println("[CI] Starting Compilation...");
      CompilationService.CompilationResult compilationResult = compilationService.compile(dir);

      try {
        RepoSetup.removeDir(runDir);
      } catch (Exception rm) {
        rm.printStackTrace();
      }

      if (!compilationResult.isSuccess()) {
        System.out.println("[CI] Compilation FAILED");
        System.out.println("[CI] Exit code: " + compilationResult.getExitCode());

        // Save the failed build
        status.put(new CommitRecord(sha, "FAILURE", compilationResult.getOutput()));
        System.out.println(
            "[CI] Output:\n"
                + compilationResult
                    .getOutput()); // Keep line to see what tests failed? -> Delete this
        // line.

        apiHandler.sendPost(
            authToken,
            targetUrl,
            "failure",
            "Build was not successful. Exited with exit code: " + compilationResult.getExitCode());
        return;
      }
      System.out.println("[CI] Compilation SUCCEEDED");

      status.put(new CommitRecord(sha, "SUCCESS", compilationResult.getOutput()));

    } catch (IOException e) {
      /* Create a list with the exception message. */
      status.put(
          new CommitRecord(
              sha, "ERROR", List.of("Failed to execute build process: " + e.getMessage())));

      e.printStackTrace();
      apiHandler.sendPost(
          authToken, targetUrl, "failure", "Failed to parse webhook or execute commands");
      return;

    } catch (InterruptedException e) {
      /* Create a list with the exception message. */
      status.put(
          new CommitRecord(
              sha, "ERROR", List.of("Command execution was interrupted: " + e.getMessage())));

      Thread.currentThread().interrupt();
      e.printStackTrace();
      apiHandler.sendPost(authToken, targetUrl, "failure", "Command execution was interrupted");
      return;
    }

    apiHandler.sendPost(authToken, targetUrl, "success", "Build was successful (somehow)!");
  }
}
