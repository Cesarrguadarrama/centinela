package mx.centinela.application;

import java.time.Clock;
import java.util.List;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.in.ProcessTransactionUseCase;
import mx.centinela.domain.port.out.AlertRepository;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.port.out.TransactionRepository;
import mx.centinela.domain.rules.FraudRule;
import mx.centinela.domain.rules.RuleMatch;

/**
 * The detection pipeline: persist the transaction, evaluate every enabled rule, raise one alert per
 * match. Persisting first matters — velocity-style rules count the current transfer too, and an
 * alert must always reference a stored transaction.
 */
public class TransactionScoringService implements ProcessTransactionUseCase {

  private final TransactionRepository transactions;
  private final RuleRepository rules;
  private final AlertRepository alerts;
  private final RuleFactory ruleFactory;
  private final Clock clock;

  public TransactionScoringService(
      TransactionRepository transactions,
      RuleRepository rules,
      AlertRepository alerts,
      RuleFactory ruleFactory,
      Clock clock) {
    this.transactions = transactions;
    this.rules = rules;
    this.alerts = alerts;
    this.ruleFactory = ruleFactory;
    this.clock = clock;
  }

  @Override
  public void process(Transaction transaction) {
    transactions.save(transaction);

    List<FraudRule> activeRules = ruleFactory.from(rules.findEnabled());
    for (FraudRule rule : activeRules) {
      rule.evaluate(transaction).ifPresent(match -> raise(transaction, match));
    }
  }

  private void raise(Transaction transaction, RuleMatch match) {
    alerts.save(Alert.raise(transaction, match, clock.instant()));
  }
}
