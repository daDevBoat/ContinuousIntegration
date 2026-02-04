package ci;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CiWebhookController {

  @PostMapping("/webhook/github")
  public ResponseEntity<?> githubWebhook(
      @RequestHeader(value = "X-GitHub-Event", required = true) String event,
      @RequestBody(required = false) byte[] body) {

    /* First checking for correct event type */
    if (!ci.Validation.validatePushEvent(event)) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok("Webhook received");
  }
}
