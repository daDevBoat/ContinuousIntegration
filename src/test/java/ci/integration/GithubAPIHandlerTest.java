package ci.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@SpringBootTest
public class GithubAPIHandlerTest {

  @Value("${server.auth:Invalid token}")
  private String authToken;

  @Value("${local.url:Invalid url}")
  private String targetUrl;

  @Test
  @Disabled
  public void testSendPostSuccess() {
    /**
     * Contract: Given the CI server running and ngrok is activated this test should always
     * successfully update the commit status to successful to a commit on the test/commit_status_api
     * branch in the "commit for testing success" commit
     */
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode root = mapper.createObjectNode();
    root.put("after", "c1de4e80a87d43338955c03d5ae47667e419e977");
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
          testHandler.sendPost(authToken, targetUrl, "success", "Test " + testId);
        });
  }

  @Test
  @Disabled
  public void testSendPostFail() {
    /**
     * Contract: Given the CI server running and ngrok is activated this test should always
     * successfully update the commit status to successful to a commit on the test/commit_status_api
     * branch in the "commit for testing fail" commit
     */
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode root = mapper.createObjectNode();
    root.put("after", "3a6e3bf8fe4b89000a0a6dd199bee7e3790aa252");
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
          testHandler.sendPost(authToken, targetUrl, "failure", "Test " + testId);
        });
  }

  @Test
  @Disabled
  public void testSendPostPending() {
    /**
     * Contract: Given the CI server running and ngrok is activated this test should always
     * successfully update the commit status to pending to a commit on the test/commit_status_api
     * branch in the "commit for testing pending" commit
     */
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode root = mapper.createObjectNode();
    root.put("after", "2c42992330ff4b155b0eba29be436a085e9411ef");
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
          testHandler.sendPost(authToken, targetUrl, "pending", "Test " + testId);
        });
  }

  @Test
  public void testSendPostInvalid() {
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
