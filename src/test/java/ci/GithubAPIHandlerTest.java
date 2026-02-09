package ci;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@SpringBootTest
public class GithubAPIHandlerTest {

  @Value("${server.auth}")
  private String authToken;

  @Value("${local.url}")
  private String targetUrl;

  @Test
  public void testSendPostSuccess() {
    /**
     * Contract: Given the CI server running and ngrok is activated this test should always
     * successfully update the commit status to a commit on the test/commit_status_api branch in the
     * "commit for testing"
     */
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode root = mapper.createObjectNode();
    root.put("after", "16c4e7b284a595ccd723e5dd0d9fbbebfa6463c9");
    ObjectNode repository = mapper.createObjectNode();
    repository.put("name", "ContinuousIntegration");
    ObjectNode owner = mapper.createObjectNode();
    owner.put("name", "daDevBoat");

    repository.set("owner", owner);
    root.set("repository", repository);

    // System.out.println(root.toPrettyString());
    // System.out.println(authToken);

    GithubAPIHandler testHandler = new GithubAPIHandler(root);
    Random rand = new Random();

    int testId = rand.nextInt(0, 1000000);

    System.out.println("API commit status test with id: " + testId + " commencing.");

    assertDoesNotThrow(
        () -> {
          testHandler.sendPost(
              authToken, targetUrl, "success", "Test " + testId + " was successful");
        });
  }

  @Test
  public void testSendPostFail() {
    /** Contract: The test should always fail as the auth token is invalid. */
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode root = mapper.createObjectNode();
    root.put("after", "16c4e7b284a595ccd723e5dd0d9fbbebfa6463c9");
    ObjectNode repository = mapper.createObjectNode();
    repository.put("name", "ContinuousIntegration");
    ObjectNode owner = mapper.createObjectNode();
    owner.put("name", "daDevBoat");

    repository.set("owner", owner);
    root.set("repository", repository);

    GithubAPIHandler testHandler = new GithubAPIHandler(root);

    assertThrows(
        WebClientResponseException.Unauthorized.class,
        () -> {
          testHandler.sendPost("Fake token", targetUrl, "success", "This should never been shown");
        });
  }
}
