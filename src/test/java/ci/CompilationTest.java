package ci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CompilationTest {

  private final CompilationService compilationService = new CompilationService();

  @Test
  public void testCompileWithNullDirectory() {
    /* Contract: compile() must throw IllegalArgumentException when projectDir is null,
     * as a null directory cannot represent a valide Gradle project. */
    assertThrows(IllegalArgumentException.class, () -> compilationService.compile(null));
  }

  @Test
  public void testCompileWithNonExistentDirectory() {
    /* Contract: compile() must throw IllegalArgumentException when projectDir points
     * to a path that does not exist on the filesystem. */
    File nonExistent = new File("/this/path/does/not/exist");
    assertThrows(IllegalArgumentException.class, () -> compilationService.compile(nonExistent));
  }

  @Test
  public void testCompileWithFile(@TempDir Path tempDir) throws IOException {
    /* Contract: compile() must throw IllegalArgumentException when projectDir is a file
     * rather than a directory, since a Gradle project requires a directory as its root. */
    File tempFile = tempDir.resolve("not-a-directory.txt").toFile();
    Files.writeString(tempFile.toPath(), "test content");

    assertThrows(IllegalArgumentException.class, () -> compilationService.compile(tempFile));
  }

  @Test
  public void testCompilationResultModel() {
    /* Contract: CompilationResult must correctly report a successful build
     * where isSuccess() returns truue, getExitCode() returns 0,
     * and getOutput() returns the expected build output string. */
    CompilationService.CompilationResult result =
        new CompilationService.CompilationResult(true, "Build successful", 0);

    assertTrue(result.isSuccess());
    assertEquals("Build successful", result.getOutput());
    assertEquals(0, result.getExitCode());
  }

  @Test
  public void testCompilationResultFailure() {
    /* Contract: CompilationResult must correctly report a failed build,
     * where isSuccess() return false, getExitCode() returns a non-zero value, and
     * getOutput() contains the error message from the build process. */
    CompilationService.CompilationResult result =
        new CompilationService.CompilationResult(false, "Error: compilation failed", 127);

    assertFalse(result.isSuccess());
    assertEquals(127, result.getExitCode());
    assertTrue(result.getOutput().contains("compilation failed"));
  }

  @Test
  public void testCompilationResultOutputIsNeverNull() {
    /* Contract: CompilationResult must always provide a non-null output string,
     * regardless of wether the build succeeded or failed.  */
    CompilationService.CompilationResult success =
        new CompilationService.CompilationResult(true, "output", 0);
    CompilationService.CompilationResult failure =
        new CompilationService.CompilationResult(false, "error", 1);

    assertNotNull(success.getOutput());
    assertNotNull(failure.getOutput());
  }
}
