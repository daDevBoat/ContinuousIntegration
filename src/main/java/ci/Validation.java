package ci;

import com.fasterxml.jackson.databind.JsonNode;

public class Validation {

  /**
   * Used to validate that the event type from the GitHub WebHook is "push"
   *
   * @param event The event from the GitHub Webhook
   * @return The result of the push check validation
   */
  public static boolean validatePushEvent(String event) {
    return event.equals("push");
  }

  /**
   * Used to validate that the payload is from the correct repo
   *
   * @param payload
   * @return The result of the repo check validation
   */
  public static boolean validateRepoName(JsonNode payload, String expectedRepoName) {
    String repoName = payload.get("repository").get("full_name").asText();
    String cleanedExpected = expectedRepoName.replace("\"", "").trim();
    return repoName.trim().equals(cleanedExpected);
  }
}
