package ci;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StoreLatestCommitTest {

  //   @Test
  //   public void updateLatestCommitCheckStoredTest() {
  //     ci.CiWebhookController.status.put(
  //         new LatestStatus("dummy-sha", "state", Instant.now(), "message"), false);
  //     assertTrue(ci.CiWebhookController.status.getLatest().isPresent());
  //     LatestStatus status = ci.CiWebhookController.status.getLatest().get();
  //     assertTrue(status.sha().equals("dummy-sha"));
  //     assertTrue(status.state().equals("state"));
  //     assertTrue(status.message().equals("message"));
  //   }
}
