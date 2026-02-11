package ci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ci.service.Status;
import ci.service.Status.CommitRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StatusTest {

  @TempDir Path tempDir;

  @Test
  public void putSetsLatestAndWritesToFileTest() throws IOException {
    /**
     * Contract: When a new commit is persisted it should be the latest persisted commit and it
     * should be persisted in the file as specified by the Status constructor.
     */
    Status status = new Status(tempDir.resolve("commits.json"));
    CommitRecord latestStatus = new CommitRecord("dummy-sha", "pass", Arrays.asList("message"));

    status.put(latestStatus);

    assertTrue(status.getLatest().isPresent());
    assertEquals("dummy-sha", status.getLatest().get().sha());

    String json = Files.readString(tempDir.resolve("commits.json"));
    assertTrue(json.contains("dummy-sha"));
  }

  @Test
  public void getCommitsMapIsNotEmptyAfterReadingFromFile() throws IOException {
    /**
     * Contract: When a file contains valid json which maps SHA to CommitRecord instances the
     * init-method should load those records and the commitsMap should not be empty.
     */
    Path commitsFilePath = tempDir.resolve("commits.json");

    String json =
"""
{
  "sha1": {
    "sha": "sha1",
    "state": "SUCCESS",
    "time": "2026-02-08 10:00:00",
    "logs": ["m1"]
  },
  "sha2": {
    "sha": "sha2",
    "state": "FAIL",
    "time": "2026-02-08 11:00:00",
    "logs": ["m2"]
  }
}
      """;

    Files.writeString(commitsFilePath, json);
    Status status = new Status(tempDir.resolve("commits.json"));
    status.init();
    assertFalse(status.getCommitsMap().isEmpty());
    assertEquals(status.getCommitsMap().size(), 2);
  }

  @Test
  public void getCommitsIsNotEmptyAfterReadingFromFile() throws IOException {
    /**
     * Contract: When a file contains valid json which maps SHA to CommitRecord instances the
     * init-method should load those records and the getCommits method should not return an empty
     * list.
     */
    Path commitsFilePath = tempDir.resolve("commits.json");

    String json =
"""
{
  "sha1": {
    "sha": "sha1",
    "state": "SUCCESS",
    "time": "2026-02-08 10:00:00",
    "logs": ["m1"]
  },
  "sha2": {
    "sha": "sha2",
    "state": "FAIL",
    "time": "2026-02-08 11:00:00",
    "logs": ["m2"]
  }
}
      """;

    Files.writeString(commitsFilePath, json);
    Status status = new Status(tempDir.resolve("commits.json"));

    status.init();
    assertFalse(status.getCommits().isEmpty());
    assertEquals(status.getCommits().size(), 2);
  }

  @Test
  public void initFunctionLoadsFromFile() throws IOException {
    /**
     * Contract: The init function should load from the file specified in the Status constructor and
     * therefor the commits map should not be empty.
     */
    Path commitsFilePath = tempDir.resolve("commits.json");

    String json =
"""
{
  "sha1": {
    "sha": "sha1",
    "state": "SUCCESS",
    "time": "2026-02-08 10:00:00",
    "logs": ["m1"]
  },
  "sha2": {
    "sha": "sha2",
    "state": "FAIL",
    "time": "2026-02-08 11:00:00",
    "logs": ["m2"]
  }
}
      """;

    Files.writeString(commitsFilePath, json);
    Status status = new Status(tempDir.resolve("commits.json"));

    status.init();
    assertFalse(status.getCommits().isEmpty());
    assertEquals(status.getCommits().size(), 2);
  }

  @Test
  public void logsForCommitWithLogsIsNonEmpty() throws IOException {
    /**
     * Contract: When a commit has been read that includes logs, the logs attribute for that commit
     * record should contain the logs.
     */
    Path commitsFilePath = tempDir.resolve("commits.json");

    String json =
"""
{
  "sha1": {
    "sha": "sha1",
    "state": "SUCCESS",
    "time": "2026-02-08 10:00:00",
    "logs": ["m1"]
  }
}
      """;

    Files.writeString(commitsFilePath, json);
    Status status = new Status(tempDir.resolve("commits.json"));

    status.init();
    assertTrue(status.getCommits().getFirst().logs().getFirst().equals("m1"));
  }
}
