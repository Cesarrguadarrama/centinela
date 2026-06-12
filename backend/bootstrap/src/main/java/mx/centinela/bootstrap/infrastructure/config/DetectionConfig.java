package mx.centinela.bootstrap.infrastructure.config;

import java.time.Clock;
import mx.centinela.application.RuleFactory;
import mx.centinela.application.TransactionScoringService;
import mx.centinela.domain.port.in.ProcessTransactionUseCase;
import mx.centinela.domain.port.out.AccountActivityPort;
import mx.centinela.domain.port.out.AlertRepository;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.port.out.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free application layer into Spring. The use case only sees domain ports —
 * which adapter implements them is decided here and nowhere else.
 */
@Configuration
public class DetectionConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  RuleFactory ruleFactory(AccountActivityPort accountActivity) {
    return new RuleFactory(accountActivity);
  }

  @Bean
  ProcessTransactionUseCase processTransactionUseCase(
      TransactionRepository transactions,
      RuleRepository rules,
      AlertRepository alerts,
      RuleFactory ruleFactory,
      Clock clock) {
    return new TransactionScoringService(transactions, rules, alerts, ruleFactory, clock);
  }
}
