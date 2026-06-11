package mx.centinela.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Entry point for the synthetic SPEI transaction generator. Produces realistic traffic — and
 * injectable fraud scenarios — to Kafka so the detection engine has something to chew on.
 */
@SpringBootApplication
public class GeneratorApplication {

  private static final Logger log = LoggerFactory.getLogger(GeneratorApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(GeneratorApplication.class, args);
  }

  @Bean
  CommandLineRunner announceMode(@Value("${centinela.generator.mode}") String mode) {
    return args ->
        log.info(
            "Transaction generator started in '{}' mode — publishing logic arrives in phase 1",
            mode);
  }
}
