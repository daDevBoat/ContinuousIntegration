package ci;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ValidationTest {

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
}
