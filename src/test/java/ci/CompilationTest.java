package ci;

import static org.junit.jupiter.api.Assertions.*;

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
    void testCompileWithNullDirectory() {
        /* Contract: Checks if the project directory is null */
        assertThrows(IllegalArgumentException.class, () -> compilationService.compile(null));
    }

    @Test
    void testCompileWithNonExistentDirectory() {
        /* Contract: Checks if the project directory does not exist */
        File nonExistent = new File("/this/path/does/not/exist");
        assertThrows(IllegalArgumentException.class, () -> compilationService.compile(nonExistent));
    }

    @Test
    void testCompileWithFile(@TempDir Path tempDir) throws IOException {
        /* Contract: Check if the project directory is a file (not a directory) */
        File tempFile = tempDir.resolve("not-a-directory.txt").toFile();
        Files.writeString(tempFile.toPath(), "test content");

        assertThrows(IllegalArgumentException.class, () -> compilationService.compile(tempFile));
    }

    @Test
    void testCompilationResultModel() {
        /* Contract:  Checks if the compilation goes successful, checking the success, the output and the exit code */
        CompilationService.CompilationResult result =
            new CompilationService.CompilationResult(true, "Build successful", 0);

        assertTrue(result.isSuccess());
        assertEquals("Build successful", result.getOutput());
        assertEquals(0, result.getExitCode());
    }

    @Test
    void testCompilationResultFailure() {
        /* Contract: Checks if the compilation fails, checking the success, the output and the exit code */
        CompilationService.CompilationResult result =
            new CompilationService.CompilationResult(false, "Error: compilation failed", 127);

        assertFalse(result.isSuccess());
        assertEquals(127, result.getExitCode());
        assertTrue(result.getOutput().contains("compilation failed"));
  }
}
