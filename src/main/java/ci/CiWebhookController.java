package ci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CiWebhookController {

  @Value("${sharedKey:xxxxxxxx}")
  private String sharedKey;

  @Value("${git.repoName:daDevBoat/ContinuousIntegration}")
  private String repoName;

  @Value("${ci.repoParentDir:not a file}")
  private String repoParentDir;

  @Value("${ci.repoSsh:git-ssh}")
  String repoSsh;

  @Value("${ci.repoID:ContinuousIntegration}")
  String repoID;

  @Value("${server.auth:Invalid auth token}")
  private String authToken;

  @Value("${local.url:Invalid target url}")
  private String targetUrl;

  private CompilationService compilationService = new CompilationService();
  public static LatestCommitStatusStore statusStore = new LatestCommitStatusStore();

  // public CiWebhookController(LatestCommitStatusStore statusStore) {
  //   this.statusStore = statusStore;
  // }

  /**
   * Serves the home page of the CI server.
   *
   * @return ResponseEntity with HTTP 200 OK status and a message saying that the server is running
   */
  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok("Server is running successfully");
  }

  @GetMapping("/status/latest")
  public ResponseEntity<?> latestStatus() {
    return statusStore
        .get()
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

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

    /* Checking for correct repo */
    if (!ci.Validation.validateRepoName(payload, repoName)) {
      return ResponseEntity.badRequest()
          .body("The repo name is not: " + repoName + ", while it is required to be so");
    }

    /* Checking if sha exists */
    String sha = payload.path("after").asText("");
    if (sha.isBlank()) {
      return ResponseEntity.badRequest().body("Missing commit sha");
    }

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

    String sha2 = payload.get("after").asText();
    statusStore.set(sha2, "SUCCESS", "Webhook validated");

    apiHandler.sendPost(authToken, targetUrl, "success", "Build was successful (somehow)!");
    return ResponseEntity.ok("Webhook received");
  }
}
