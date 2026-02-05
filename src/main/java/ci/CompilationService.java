package ci;

import org.springframework.stereotype.Service;
import java.io.File;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.lang.InterruptedException;

import org.springframework.stereotype.Service;

@Service
public class CompilationService {

    public CompilationResult compile(File projectDir) throws IOException, InterruptedException {
        if(projectDir == null) {
            throw new IllegalArgumentException("Project directory cannot be null.");
        }

        if(!projectDir.exists() || !projectDir.isDirectory()){
            throw new IllegalArgumentException("Project directory does not exist, or is not a directory: " + projectDir);
        }

        System.out.println("[COMPILATION] Starting Compilation for: " + projectDir.getAbsolutePath());
        System.out.println("[COMPILATION] Executing ./gradlew build");

        ProcessBuilder pb = new ProcessBuilder("bash", "-lc", "./gradlew build");
        pb.directory(projectDir);
        pb.redirectErrorStream(true);

        Process p = pb.start();

        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        int exitCode = p.waitFor();
        boolean success = (exitCode == 0);

        System.out.println("[COMPILATION] Exit code: " + exitCode);
        if (success) {
            System.out.println("[COMPILATION] Build successful");
        } else {
           System.out.println("[COMPILATION] Build failed");
           System.out.println("[COMPILATION] Output:\n" + output); 
        }

        return new CompilationResult(success, output, exitCode);
    }

    public static class CompilationResult {

        private final boolean success;
        private final String output;
        private final int exitCode;

        public CompilationResult(boolean success, String output, int exitCode){
            this.success = success;
            this.output = output;
            this.exitCode = exitCode;
        }

        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public int getExitCode() { return exitCode; }
    }
}
