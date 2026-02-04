package ci;

public class Validation {

  /**
   * Used to validate that the event type from the GitHub WebHook is "push"
   *
   * @param event The event from the GitHub Webhook
   * @return The result of the push check validation
   */
  public static boolean validatePushEvent(String event) {
    return "push".equals(event);
  }
}
