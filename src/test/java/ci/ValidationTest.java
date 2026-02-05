package ci;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ValidationTest {

  @Value("${git.repoName:daDevBoat/ContinuousIntegration}")
  private String repoName;

  @Test
  public void validatePushEventTest() {

    /*
     * Contract: validatePushEvent returns true iff the event is "push"
     */
    String event = "push";
    assertTrue(ci.Validation.validatePushEvent(event));

    event = "puSH";
    assertFalse(ci.Validation.validatePushEvent(event));

    event = "pull";
    assertFalse(ci.Validation.validatePushEvent(event));
  }

  @Test
  public void validateRepoNameTest() {
    /*
     * Contract: validateRepoName returns true iff the repo is the given repo
     *  sat in application.resources
     */
    String testBody =
        """
    {
        "ref": "refs/heads/issue/28",
        "before": "0000000000000000000000000000000000000000",
        "after": "1c9e437098861810bc8b3e17cbd676356a9f85b9",
        "repository": {
          "id": 1145037701,
          "node_id": "R_kgDORD_jhQ",
          "name": "ContinuousIntegration",
          "full_name": "daDevBoat/ContinuousIntegration",
          "private": false,
          "owner": {
            "name": "daDevBoat",
            "email": "113507675+daDevBoat@users.noreply.github.com",
            "login": "daDevBoat",
            "id": 113507675,
            "node_id": "U_kgDOBsP9Ww",
            "avatar_url": "https://avatars.githubusercontent.com/u/113507675?v=4",
            "gravatar_id": "",
            "url": "https://api.github.com/users/daDevBoat",
            "html_url": "https://github.com/daDevBoat",
            "followers_url": "https://api.github.com/users/daDevBoat/followers",
            "following_url": "https://api.github.com/users/daDevBoat/following{/other_user}",
            "gists_url": "https://api.github.com/users/daDevBoat/gists{/gist_id}",
            "starred_url": "https://api.github.com/users/daDevBoat/starred{/owner}{/repo}",
            "subscriptions_url": "https://api.github.com/users/daDevBoat/subscriptions",
            "organizations_url": "https://api.github.com/users/daDevBoat/orgs",
            "repos_url": "https://api.github.com/users/daDevBoat/repos",
            "events_url": "https://api.github.com/users/daDevBoat/events{/privacy}",
            "received_events_url": "https://api.github.com/users/daDevBoat/received_events",
            "type": "User",
            "user_view_type": "public",
            "site_admin": false
          }
        }
    }
    """;

    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload = null;
    try {
      payload = mapper.readTree(testBody);
    } catch (Exception e) {
      // Add other error handling here with website
      System.out.println(e);
    }

    assertTrue(ci.Validation.validateRepoName(payload, repoName));

    ObjectNode repository = (ObjectNode) payload.get("repository");
    repository.put("full_name", "daDevBoat/test");
    assertFalse(ci.Validation.validateRepoName(payload, repoName));
  }

  @Test
  public void validateSignatureTest() {
    /*
     * Contract: A signature is valid if it is the same as computing
     *  the signature of a message using the sharedKey with the same algorithm,
     *  in this case SHA256.
     */
    String body = "this is the text that should be hashed";
    String sharedKey = "test1";
    String signature = "sha256=8235f5dde6be4a508848a58c377aaec2e954905cf9d40f1582d3e1a0f44e6771";

    assertDoesNotThrow(
        () -> {
          boolean validSignature =
              ci.Validation.validateSignature(sharedKey, body.getBytes("UTF-8"), signature);
          assertTrue(validSignature);
        });
  }

  @Test
  public void validateSignatureWrongSignatureTest() {
    /*
     * Contract: A signature is valid if it is the same as computing the signature of a message
     *  using the sharedKey with the same algorithm, in this case SHA256. In this case we replace
     *  in the given signature to yield a faulty signature and make sure the validation function
     *  returns false.
     */
    String body = "this is the text that should be hashed";
    String sharedKey = "test1";
    String signature =
        "sha256=8235f5dde6be4a508848a58c377aaec2e954905cf9d40f1582d3e1a0f44e6771".replace("3", "1");

    assertDoesNotThrow(
        () -> {
          boolean validSignature =
              ci.Validation.validateSignature(sharedKey, body.getBytes("UTF-8"), signature);
          assertFalse(validSignature);
        });
  }

  @Test
  public void validateSignatureBadBodyInputTest() {
    /*
     * Contract: The validation function uses another function which requires non-null body
     *  (and non-empty).
     */
    byte[] body = null;
    String sharedKey = "test1";
    String signature = "sha256=8235f5dde6be4a508848a58c377aaec2e954905cf9d40f1582d3e1a0f44e6771";

    assertThrows(
        IllegalArgumentException.class,
        () -> ci.Validation.validateSignature(sharedKey, body, signature));
  }
}
