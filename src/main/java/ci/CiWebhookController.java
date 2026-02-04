package ci;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CiWebhookController {

  @PostMapping("/webhook/github")
  public ResponseEntity<String> githubWebhook(
      @RequestHeader(value = "X-GitHub-Event", required = true) String event,
      @RequestBody(required = false) byte[] body) {

    return ResponseEntity.ok("Webhook received");
  }
}
