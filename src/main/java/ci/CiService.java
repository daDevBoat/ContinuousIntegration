package ci;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

  /** Service responsible for compiling the project. */
  private CompilationService compilationService = new CompilationService();

  @Async
  public void runBuild(JsonNode payload) {

    /* Sending pending status back to GitHub */
    GithubAPIHandler apiHandler = new GithubAPIHandler(payload);
    apiHandler.sendPost(
        authToken, targetUrl, "pending", "Starting building and testing (cross your fingers)");

    String sha = payload.path("after").asText("");

    /* Building the repo if it doesnt exist yet, and pulling newest changes */
    try {
      RepoSetup.createDir(repoParentDir);
      RepoSetup.cloneRepo(repoParentDir, repoID, repoSsh);
      RepoSetup.updateRepo(repoParentDir, repoID, sha);
    } catch (IllegalStateException e) {
      e.printStackTrace();
      apiHandler.sendPost(authToken, targetUrl, "failure", "Git error: Illegal State");
      return;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      apiHandler.sendPost(authToken, targetUrl, "failure", "Error when processing the SHA");
      return;
    } catch (IOException e) {
      e.printStackTrace();
      apiHandler.sendPost(authToken, targetUrl, "failure", "Error when creating the directory");
      return;
    }

    File dir = new File(repoParentDir, repoID);

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

      if (!compilationResult.isSuccess()) {
        System.out.println("[CI] Compilation FAILED");
        System.out.println("[CI] Exit code: " + compilationResult.getExitCode());
        System.out.println("[CI] Output:\n" + compilationResult.getOutput());

        apiHandler.sendPost(
            authToken,
            targetUrl,
            "failure",
            "Build was not successful. Ecited with exit code: " + compilationResult.getExitCode());
        return;
      }
      System.out.println("[CI] Compilation SUCCEEDED");
    } catch (IOException e) {
      e.printStackTrace();
      apiHandler.sendPost(
          authToken, targetUrl, "failure", "Failed to parse webhook or execute commands");
      return;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
      apiHandler.sendPost(authToken, targetUrl, "failure", "Command execution was interrupted");
      return;
    }

    String sha2 = payload.get("after").asText();

    apiHandler.sendPost(authToken, targetUrl, "success", "Build was successful (somehow)!");
  }
}
