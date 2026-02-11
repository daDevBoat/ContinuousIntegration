package ci;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepoSetupTest {
  @TempDir Path temp;

  @Test
  void createDir_createsDirectoryIfMissing(@TempDir Path temp) throws Exception {
    /* Contract: createDir creates a dir on the given path, if no file/dir exists there */
    Path newDir = temp.resolve("newDirectory");
    assertFalse(Files.exists(newDir));
    RepoSetup.createDir(newDir.toString());
    assertTrue(Files.isDirectory(newDir));
  }

  @Test
  void createDir_failes_because_of_existing_file(@TempDir Path temp) throws Exception {
    /* Contract: createDir throws an exception, if a file already exists under the given path */
    Path newFilePath = temp.resolve("newDirectory");
    Files.createFile(newFilePath);
    assertThrows(IOException.class, () -> RepoSetup.createDir(newFilePath.toString()));
  }

  @Test
  void removeDirSuccess(@TempDir Path temp) throws Exception {
    /* Contract: removeDir removes a directory if a directory already exists under the given path */
    Path newFilePath = temp.resolve("newDirectoryTest");
    Files.createDirectory(newFilePath);
    assertDoesNotThrow(() -> RepoSetup.removeDir(newFilePath.toString()));
  }

  @Test
  void removeDirFailureNoDirExists(@TempDir Path temp) throws Exception {
    /* Contract: removeDir removes a directory if a directory already exists under
     * the given path, in this caseno file exists
     */
    Path newFilePath = temp.resolve("newDirectoryTest");
    assertThrows(IOException.class, () -> RepoSetup.removeDir(newFilePath.toString()));
  }

  @Test
  @Disabled
  void cloneRepo_with_correct_ssh(@TempDir Path temp) throws Exception {
    /* Contract: cloneRepo clones the repo from GitHub in the given subfolder */
    Path newDir = temp.resolve("newDirectory");
    Files.createDirectory(newDir);
    assertTrue(Files.exists(newDir));

    String testRepo = "testRepo";
    RepoSetup.cloneRepo(
        newDir.toString(), testRepo, "git@github.com:daDevBoat/ContinuousIntegration.git");
    Path gitDir = newDir.resolve(testRepo);
    assertTrue(Files.isDirectory(gitDir));
  }

  @Test
  @Disabled
  void cloneRepo_with_wrong_ssh(@TempDir Path temp) throws Exception {
    /* Contract: cloneRepo fails to clone the repo from GitHub when the ssh is wrong */
    Path newDir = temp.resolve("newDirectory");
    Files.createDirectory(newDir);
    assertTrue(Files.exists(newDir));

    String testRepo = "testRepo";
    assertThrows(
        IllegalStateException.class,
        () ->
            RepoSetup.cloneRepo(
                newDir.toString(), testRepo, "git@github:daDevBoat/ContinuoussIntegration.git"));
  }

  @Test
  @Disabled
  void updateRepo_and_checkout_with_correct_sha(@TempDir Path temp) throws Exception {
    /* Contract: updateRepo pulls the newest changes and checksout the commit under sha */
    // Create Dictionary
    Path newDir = temp.resolve("newDirectory");
    Files.createDirectory(newDir);
    assertTrue(Files.exists(newDir));

    // Clone the repo
    String testRepo = "testRepo";
    RepoSetup.cloneRepo(
        newDir.toString(), testRepo, "git@github.com:daDevBoat/ContinuousIntegration.git");

    // Pull the newest changes and checkout sha:
    String sha = "b785fdb134699dddb1209e496fe213707b665bd3";
    RepoSetup.updateRepo(newDir.toString(), testRepo, sha);

    // Get the sha from git to confirm they are matching
    Path repoDir = newDir.resolve(testRepo);
    ProcessBuilder builder = new ProcessBuilder("git", "rev-parse", "HEAD");
    builder.directory(new File(repoDir.toString()));
    builder.redirectErrorStream(true);
    Process p = builder.start();
    String result;
    try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      result = r.readLine();
    }
    int exit = p.waitFor();
    if (exit != 0) {
      throw new IllegalStateException("git rev-parse HEAD failed with exit code " + exit);
    }
    result.trim();
    assertEquals(sha, result);
  }

  @Test
  @Disabled
  void updateRepo_and_checkout_with_wrong_sha(@TempDir Path temp) throws Exception {
    /* Contract: updateRepo pulls the newest changes and throws and exception when the sha
     *  is wrong
     */

    // Create Dictionary
    Path newDir = temp.resolve("newDirectory");
    Files.createDirectory(newDir);
    assertTrue(Files.exists(newDir));

    // Clone the repo
    String testRepo = "testRepo";
    RepoSetup.cloneRepo(
        newDir.toString(), testRepo, "git@github.com:daDevBoat/ContinuousIntegration.git");

    // Pull the newest changes and checkout sha:
    String sha = "b785fdb134699dd9e496fe213707b665bd3";
    assertThrows(
        IllegalStateException.class, () -> RepoSetup.updateRepo(newDir.toString(), testRepo, sha));
  }

  @Test
  @Disabled
  void updateRepo_with_sha_out_of_zeros(@TempDir Path temp) throws Exception {
    /* Contract: updateRepo throws and excpetion when the sha is only 0's */
    // Create Dictionary
    Path newDir = temp.resolve("newDirectory");
    Files.createDirectory(newDir);
    assertTrue(Files.exists(newDir));

    // Clone the repo
    String testRepo = "testRepo";
    RepoSetup.cloneRepo(
        newDir.toString(), testRepo, "git@github.com:daDevBoat/ContinuousIntegration.git");

    // Pull the newest changes and checkout sha:
    String sha = "0000000000000000000000000000000000000000";

    assertThrows(
        IllegalArgumentException.class,
        () -> RepoSetup.updateRepo(newDir.toString(), testRepo, sha));
  }
}
