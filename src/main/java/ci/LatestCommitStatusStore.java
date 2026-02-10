package ci;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

/** Stores the most recent commit status in the memory for quick access. */
@Service
public class LatestCommitStatusStore {

  /**
   * Imutable snapshot of the latest status update
   *
   * @param sha commit SHA the status applies to
   * @param status status state
   * @param time time the status was recorded
   * @param message status message
   */
  public record LatestStatus(String sha, String state, Instant time, String message) {
    ;
  }

  private final AtomicReference<LatestStatus> latest = new AtomicReference<>();

  /**
   * Stores a new latest status snapshot with the current time.
   *
   * @param sha commit SHA the status applies to
   * @param state status state
   * @param message status message
   */
  public void set(String sha, String state, String message) {
    latest.set(new LatestStatus(sha, state, Instant.now(), message));
  }

  /**
   * Returns the most recent status snapshot if it exists.
   *
   * @return latest status or empty if none has been recorded
   */
  public Optional<LatestStatus> get() {
    return Optional.ofNullable(latest.get());
  }
}
