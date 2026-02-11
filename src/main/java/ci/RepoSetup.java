package ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.tomcat.util.http.fileupload.FileUtils;

/**
 * RepoSetup class for managing Git repostiory operations.
 * 
 * <p>This class contains static methods to initialize and
 * maintain Git repositories, which include creating 
 * directories, cloning repositories from GitHub, and 
 * updating repositories to specific commits. 
 */
public class RepoSetup {

  /**
   * Create a directory at the location of repo_path -> Can be changed in the
   * application.propoerties file
   *
   * @param repoParentDir Path to the parent directory, where we want to store the test repo
   * @throws IOException when the directory cant be created
   * @throws IOException when the file located under the location is not a directory
   */
  public static void createDir(String repoParentDir) throws IOException {
    File dir = new File(repoParentDir);

    if (!dir.exists()) {

      // trying to create a directory in the repo_path location
      boolean created = dir.mkdirs();

      if (!created) {
        throw new IOException("Cannot create a directory at : " + repoParentDir);
      }
    } else if (!dir.isDirectory()) {
      throw new IOException("Location exists, but is not a directory : " + repoParentDir);
    }
  }

  /**
   * Remove a directory at the location of repo_path -> Can be changed in the
   * application.propoerties file
   *
   * @param repoParentDir Path to the parent directory, where we want to store the test repo
   * @throws IOException in case deletion is unsuccessful or dir does not exist
   */
  public static void removeDir(String repoParentDir) throws IOException {
    File dir = new File(repoParentDir);

    if (!dir.exists()) {
      throw new IOException("Directoty does not exist at: " + repoParentDir);
    }

    FileUtils.forceDelete(dir);
  }

  /**
   * Checks if the repo already exists, if not we clone the repo
   *
   * @param repoParentDir Path to the parent directory, where we want to store the test repo
   * @param repoID The ID of the test repo - what it should be named like
   * @param repoSsh The SSH to the repo, so it can be cloned from GitHub
   * @throws IllegalStateException If the GitHub repo could not be cloned
   */
  public static void cloneRepo(String repoParentDir, String repoID, String repoSsh) {

    Path repoPath = Paths.get(repoParentDir, repoID);
    Path gitDir = repoPath.resolve(".git");

    boolean repoExists = Files.isDirectory(gitDir);

    if (!repoExists) {
      ProcessBuilder builder = new ProcessBuilder("git", "clone", repoSsh, repoID);
      builder.directory(new File(repoParentDir));
      builder.redirectErrorStream(true);

      try {
        Process p = builder.start();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
          String line;
          while (true) {
            line = r.readLine();
            if (line == null) {
              break;
            }
            System.out.println(line);
          }
        }
        int exit = p.waitFor();
        if (exit != 0) {
          throw new IllegalStateException("git clone failed with exit code " + exit);
        }

      } catch (IOException e) {
        throw new IllegalStateException("Could not run clone the git repository", e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for git clone", e);
      }
    }
  }

  /**
   * Fetches the newest changes from the repo, then checks out the branch on which the push
   * happened.
   *
   * @param repoParentDir Path to the parent directory, where we want to store the test repo
   * @param repoID The ID of the test repo - what it should be named like
   * @param sha The commit sha from the http payload
   * @throws IllegalArgumentException when the sha is null, blank or only 0's
   * @throws IllegalStateException When one of the git commands could not be executed
   */
  public static void updateRepo(String repoParentDir, String repoID, String sha) {
    if (sha == null || sha.isBlank() || sha.equals("0000000000000000000000000000000000000000")) {
      throw new IllegalArgumentException("No or invalid sha: " + sha);
    }

    String repo_path = Paths.get(repoParentDir, repoID).toString();

    ProcessBuilder builder = new ProcessBuilder("git", "fetch", "--all", "--prune");
    builder.directory(new File(repo_path));
    builder.redirectErrorStream(true);

    try {
      Process p = builder.start();

      try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while (true) {
          line = r.readLine();
          if (line == null) {
            break;
          }
          System.out.println(line);
        }
      }

      int exit = p.waitFor();
      if (exit != 0) {
        throw new IllegalStateException(
            "git fetch failed with exit code " + exit + " on commit (sha=" + sha + ")");
      }

    } catch (IOException e) {
      throw new IllegalStateException("Could not run git fetch for commit sha: " + sha, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for git fetch on sha: " + sha, e);
    }

    ProcessBuilder builder2 = new ProcessBuilder("git", "checkout", "-f", sha);
    builder2.directory(new File(repo_path));
    builder2.redirectErrorStream(true);

    try {
      Process p2 = builder2.start();

      try (BufferedReader r = new BufferedReader(new InputStreamReader(p2.getInputStream()))) {
        String line;
        while ((line = r.readLine()) != null) {
          System.out.println(line);
        }
      }

      int exit = p2.waitFor();
      if (exit != 0) {
        throw new IllegalStateException(
            "git checkout failed with exit code " + exit + " (sha=" + sha + ")");
      }

    } catch (IOException e) {
      throw new IllegalStateException("Could not run git checkout (sha=" + sha + ")", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
          "Interrupted while waiting for git checkout (sha=" + sha + ")", e);
    }
  }
}
