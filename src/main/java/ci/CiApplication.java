package ci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CiApplication {
  public static void main(String[] args) {
    SpringApplication.run(CiApplication.class, args);
  }
}
