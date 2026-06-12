package mx.centinela.domain.rules;

import java.util.Optional;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;

/**
 * Flags amounts suspiciously close below a reporting threshold — the structuring pattern of keeping
 * each transfer just under the limit that would trigger mandatory reporting (e.g. $49,999 against a
 * $50,000 threshold).
 *
 * <p>Params: {@code thresholdPesos} (default 50,000), {@code marginPesos} (default 5,000) — fires
 * when {@code threshold - margin <= amount < threshold}.
 */
public final class SubThresholdAmountRule implements FraudRule {

  private final RuleDefinition definition;
  private final Money threshold;
  private final Money lowerBound;

  public SubThresholdAmountRule(RuleDefinition definition) {
    this.definition = definition;
    this.threshold = Money.of(definition.decimalParam("thresholdPesos", "50000"));
    this.lowerBound =
        Money.of(threshold.amount().subtract(definition.decimalParam("marginPesos", "5000")));
  }

  @Override
  public RuleDefinition definition() {
    return definition;
  }

  @Override
  public Optional<RuleMatch> evaluate(Transaction tx) {
    boolean inSuspiciousBand =
        tx.amount().isGreaterThanOrEqual(lowerBound) && tx.amount().isLessThan(threshold);
    if (!inSuspiciousBand) {
      return Optional.empty();
    }
    String explanation =
        ("Transferencia de %s apenas por debajo del umbral de reporte de %s. "
                + "Los montos en la banda %s–%s son consistentes con estructuración "
                + "para evadir reportes regulatorios.")
            .formatted(tx.amount(), threshold, lowerBound, threshold);
    return Optional.of(RuleMatch.of(definition, explanation));
  }
}
