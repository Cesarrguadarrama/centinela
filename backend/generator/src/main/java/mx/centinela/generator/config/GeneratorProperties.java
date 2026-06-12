package mx.centinela.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Generator tuning, bound from environment variables (see .env.example).
 *
 * @param mode {@code idle} (wait for API commands) or {@code normal} (start traffic on boot)
 * @param transactionsPerSecond mean rate of the Poisson-distributed normal traffic
 * @param accounts size of the synthetic account pool
 */
@ConfigurationProperties(prefix = "centinela.generator")
public record GeneratorProperties(String mode, int transactionsPerSecond, int accounts) {

  public boolean startsRunning() {
    return "normal".equalsIgnoreCase(mode);
  }
}
