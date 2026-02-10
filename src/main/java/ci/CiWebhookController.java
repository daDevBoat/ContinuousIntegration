package ci;

import ci.Status.CommitRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the Continuous Integration (CI) webhook endpoints.
 *
 * <p>This controller is responsible for: - Receiving Github webhook events. - Validating webhook
 * signatures and repository information. - Fetching and updating the repository. - Triggering the
 * compilation process. - Storing and exposing the latest build status
 *
 * <p>The controller is designed to be used as a lightweight CI server that reacts to Github push
 * events.
 */
@RestController
public class CiWebhookController {

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

  private final Status status;

  public CiWebhookController(Status status) {
    this.status = status;
  }

  /**
   * Serves the home page of the CI server.
   *
   * @return ResponseEntity with HTTP 200 OK status and a message saying that the server is running
   */
  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok("Server is running successfully");
  }

  /**
   * Handles GitHub webhook events
   *
   * <p>This endpoint: - Validates the event type. - Verifies the webhook signature. - Checks the
   * repository information. - Fetches and updates the repository. - Triggers the compilation
   * process. - Stores and reports the build result.
   *
   * @param event the GitHub event type (expected to be {@code push}).
   * @param signature the SHA-256 signature sent by GitHub.
   * @param body the raw JSON payload of the webhook request.
   * @return ResponseEntity indicating success or the reason for failure.
   */
  @PostMapping("/webhook/github")
  public ResponseEntity<?> githubWebhook(
      @RequestHeader(value = "X-GitHub-Event", required = true) String event,
      @RequestHeader(value = "X-Hub-Signature-256", required = true) String signature,
      @RequestBody(required = false) byte[] body) {

    /* Convert the body[] into a json object */
    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload = null;
    try {
      payload = mapper.readTree(body);
    } catch (Exception e) {
      // Add other error handling here with website
      System.out.println(e);
    }

    GithubAPIHandler apiHandler = new GithubAPIHandler(payload);

    /* Checking for correct event type */
    if (!ci.Validation.validatePushEvent(event)) {
      return ResponseEntity.badRequest()
          .body("The github event is not push, but is required to be so.");
    }

    /* Verify signature */
    boolean signatureValid;
    try {
      signatureValid = ci.Validation.validateSignature(sharedKey, body, signature);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An internal server error occured when verifying the signature");
    }
    if (!signatureValid) {
      return ResponseEntity.badRequest().body("Signature was invalid");
    }

    /* Checking for correct repository */
    if (!ci.Validation.validateRepoName(payload, repoName)) {
      return ResponseEntity.badRequest()
          .body("The repo name is not: " + repoName + ", while it is required to be so");
    }

    /* Extract commit SHA */
    String sha = payload.path("after").asText("");
    if (sha.isBlank()) {
      return ResponseEntity.badRequest().body("Missing commit sha");
    }

    /* Sending pending status back to GitHub */
    apiHandler.sendPost(
        authToken, targetUrl, "pending", "Starting building and testing (cross your fingers)");

    /* Building the repo if it doesnt exist yet, and pulling newest changes */
    try {
      RepoSetup.createDir(repoParentDir);
      RepoSetup.cloneRepo(repoParentDir, repoID, repoSsh);
      RepoSetup.updateRepo(repoParentDir, repoID, sha);
    } catch (IllegalStateException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Git error: " + e.getMessage());

    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Error when processing the SHA: " + e.getMessage());

    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Error when creating the directory " + e.getMessage());
    }

    File dir = new File(repoParentDir, repoID);

    /* Check if the directory exists */
    if (!dir.isDirectory()) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Repo dir was not found '" + dir.getAbsolutePath() + "'.");
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
            authToken, targetUrl, "failure", "Build was not successful (not surprisingly)!");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                "Compilation failed (exit "
                    + compilationResult.getExitCode()
                    + ")\n"
                    + compilationResult.getOutput());
      }
      System.out.println("[CI] Compilation SUCCEEDED");
    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to parse webhook or execute commands: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Command execution was interrupted: " + e.getMessage());
    }

    status.put(new CommitRecord(sha, "SUCCESS", Instant.now().toString(), "Webhook validated"));

    apiHandler.sendPost(authToken, targetUrl, "success", "Build was successful (somehow)!");
    return ResponseEntity.ok("Webhook received");
  }
}
