package ci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CiApplication is the main entry point for the Continuous 
 * Integration Spring Boot application
 */
@SpringBootApplication
@EnableAsync
public class CiApplication {
  
  /**
   * Main method that starts the Sprint Boot CI application
   * 
   * @param args command line arguments passed to the
   * application
   */
  public static void main(String[] args) {
    SpringApplication.run(CiApplication.class, args);
  }
}
