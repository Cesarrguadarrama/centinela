package mx.centinela.domain.rules;

import java.util.Optional;
import mx.centinela.domain.model.Transaction;

/** A configured fraud rule, ready to evaluate transactions. */
public interface FraudRule {

  RuleDefinition definition();

  /** Empty when the transaction looks fine to this rule. */
  Optional<RuleMatch> evaluate(Transaction transaction);
}
