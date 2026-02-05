package ci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CiWebhookController {

  @Value("${git.repoName:daDevBoat/ContinuousIntegration}")
  private String repoName;

  /**
   * Serves the home page of the CI server.
   *
   * @return ResponseEntity with HTTP 200 OK status and a message saying that the server is running
   */
  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok("Server is running successfully");
  }

  @PostMapping("/webhook/github")
  public ResponseEntity<?> githubWebhook(
      @RequestHeader(value = "X-GitHub-Event", required = true) String event,
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

    /* Checking for correct event type */
    if (!ci.Validation.validatePushEvent(event)) {
      return ResponseEntity.badRequest()
          .body("The github event is not push, but is required to be so.");
    }

    /* Checking for correct repo */

    if (!ci.Validation.validateRepoName(payload, repoName)) {
      return ResponseEntity.badRequest()
          .body("The repo name is not: " + repoName + ", while it is required to be so");
    }

    return ResponseEntity.ok("Webhook received");
  }
}
