package mx.centinela.application;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.Score;
import mx.centinela.domain.model.ScoredTransaction;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.in.ProcessTransactionUseCase;
import mx.centinela.domain.port.out.ActivityWindowPort;
import mx.centinela.domain.port.out.AlertRepository;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.port.out.TransactionRepository;
import mx.centinela.domain.rules.RuleMatch;

/**
 * The detection pipeline: register the transfer in the sliding windows, evaluate every enabled
 * rule, derive the composite score, persist the scored transaction and raise one alert per match.
 * Registration happens first so window-based rules see the current transfer too.
 */
public class TransactionScoringService implements ProcessTransactionUseCase {

  private final TransactionRepository transactions;
  private final RuleRepository rules;
  private final AlertRepository alerts;
  private final ActivityWindowPort activityWindows;
  private final RuleFactory ruleFactory;
  private final Clock clock;

  public TransactionScoringService(
      TransactionRepository transactions,
      RuleRepository rules,
      AlertRepository alerts,
      ActivityWindowPort activityWindows,
      RuleFactory ruleFactory,
      Clock clock) {
    this.transactions = transactions;
    this.rules = rules;
    this.alerts = alerts;
    this.activityWindows = activityWindows;
    this.ruleFactory = ruleFactory;
    this.clock = clock;
  }

  @Override
  public void process(Transaction transaction) {
    activityWindows.register(transaction);

    List<RuleMatch> matches =
        ruleFactory.from(rules.findEnabled()).stream()
            .map(rule -> rule.evaluate(transaction))
            .flatMap(Optional::stream)
            .toList();

    transactions.save(new ScoredTransaction(transaction, Score.fromMatches(matches), matches));
    matches.forEach(match -> alerts.save(Alert.raise(transaction, match, clock.instant())));
  }
}
