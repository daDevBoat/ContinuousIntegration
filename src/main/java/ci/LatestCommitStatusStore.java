package ci;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class LatestCommitStatusStore {
  public record LatestStatus(String sha, String state, Instant time, String message) {
    ;
  }

  private final AtomicReference<LatestStatus> latest = new AtomicReference<>();

  public void set(String sha, String state, String message) {
    latest.set(new LatestStatus(sha, state, Instant.now(), message));
  }

  public Optional<LatestStatus> get() {
    return Optional.ofNullable(latest.get());
  }
}
