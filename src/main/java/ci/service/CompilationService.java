package ci.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service class for compiling the Java project using Gradle.
 *
 * <p>This CompilationService class executes Gradle build commands on project directories and
 * captures the build output and exit status. It uses the Gradle wrapper (gradlew) to ensure
 * consistent build execution.
 */
@Service
public class CompilationService {

  /**
   * Compiles a Java project using Gradle by executing the {@code gradlew build} command
   *
   * @param projectDir the directory containing the Gradle project to compile.
   * @return a {@link CompilationResult} containing the build status, output, and exit code.
   * @throws IOException if an I/O error occurs while reading the process output.
   * @throws InterruptedException if the current thread is interrupted while waiting for the build
   *     process to compile.
   * @throws IllegalArgumentException if projectDir is null, does not exists, or it is not a
   *     directory.
   */
  public CompilationResult compile(File projectDir) throws IOException, InterruptedException {
    if (projectDir == null) {
      throw new IllegalArgumentException("Project directory cannot be null.");
    }

    if (!projectDir.exists() || !projectDir.isDirectory()) {
      throw new IllegalArgumentException(
          "Project directory does not exist, or is not a directory: " + projectDir);
    }

    System.out.println("[COMPILATION] Starting Compilation for: " + projectDir.getAbsolutePath());
    System.out.println("[COMPILATION] Executing ./gradlew build");

    ProcessBuilder pb = new ProcessBuilder("bash", "-lc", "./gradlew build");
    pb.directory(projectDir);
    pb.redirectErrorStream(true);

    Process p = pb.start();

    List<String> output;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      output = reader.lines().collect(Collectors.toCollection(ArrayList::new));
    }

    int exitCode = p.waitFor();
    boolean success = (exitCode == 0);

    System.out.println("[COMPILATION] Exit code: " + exitCode);

    if (success) {
      System.out.println("[COMPILATION] Build successful");
      output.add("Build successful");
    } else {
      System.out.println("[COMPILATION] Build failed");
      output.add("Compilation failed (exit " + exitCode + ")");
    }

    return new CompilationResult(success, output, exitCode);
  }

  /**
   * CompilationResult is a class that represents the result of a Gradle build compilation.
   *
   * <p>The result contains whether the compilation was successful, the complete build output, and
   * the process exit code.
   */
  public static class CompilationResult {

    private final boolean success;
    private final List<String> output;
    private final int exitCode;

    /**
     * CompilationResult class creates a new compilation result.
     *
     * @param success {@code true} if the compilation succeded (exit code 0). {@code false}
     *     otherwise
     * @param output the complete build output (stdout and stderr combined).
     * @param exitCode the process exit code; 0 indicates success, non-zero indicates failure.
     */
    public CompilationResult(boolean success, List<String> output, int exitCode) {
      this.success = success;
      this.output = output;
      this.exitCode = exitCode;
    }

    /**
     * Returns wether the compilation was successful.
     *
     * @return {@code true} if the build is completed successfully (exit code 0), {@code false}
     *     ottherwaise
     */
    public boolean isSuccess() {
      return success;
    }

    /**
     * Returns the complete build output.
     *
     * @return the complete build output as a string, never {@code null}
     */
    public List<String> getOutput() {
      return output;
    }

    /**
     * Returns the process exit code.
     *
     * @return the exit code from the Gradle build process.
     */
    public int getExitCode() {
      return exitCode;
    }
  }
}
