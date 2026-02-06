package ci;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ci.LatestCommitStatusStore.LatestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StoreLatestCommitTest {

  @Test
  public void updateLatestCommitCheckStoredTest() {
    ci.CiWebhookController.statusStore.set("dummy-sha", "state", "message");
    assertTrue(ci.CiWebhookController.statusStore.get().isPresent());
    LatestStatus status = ci.CiWebhookController.statusStore.get().get();
    assertTrue(status.sha().equals("dummy-sha"));
    assertTrue(status.state().equals("state"));
    assertTrue(status.message().equals("message"));
  }
}
