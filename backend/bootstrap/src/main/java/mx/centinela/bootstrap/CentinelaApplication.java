package mx.centinela.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point for the Centinela detection engine and REST API. */
@SpringBootApplication
@EnableScheduling
public class CentinelaApplication {

  public static void main(String[] args) {
    SpringApplication.run(CentinelaApplication.class, args);
  }
}
