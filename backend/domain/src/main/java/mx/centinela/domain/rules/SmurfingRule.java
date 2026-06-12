package mx.centinela.domain.rules;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import mx.centinela.domain.activity.WindowSnapshot;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.ActivityWindowPort;

/**
 * Detects montos hormiga: a large amount fragmented into many small transfers so each one slips
 * under detection thresholds, while the sum is significant.
 *
 * <p>Params: {@code maxAmountPesos} (default 10,000 — what counts as "small"), {@code minCount}
 * (default 15), {@code minTotalPesos} (default 50,000), {@code windowMinutes} (default 10).
 */
public final class SmurfingRule implements FraudRule {

  private final RuleDefinition definition;
  private final ActivityWindowPort windows;
  private final Money smallLimit;
  private final int minCount;
  private final BigDecimal minTotal;
  private final Duration window;

  public SmurfingRule(RuleDefinition definition, ActivityWindowPort windows) {
    this.definition = definition;
    this.windows = windows;
    this.smallLimit = Money.of(definition.decimalParam("maxAmountPesos", "10000"));
    this.minCount = definition.intParam("minCount", 15);
    this.minTotal = definition.decimalParam("minTotalPesos", "50000");
    this.window = Duration.ofMinutes(definition.intParam("windowMinutes", 10));
  }

  @Override
  public RuleDefinition definition() {
    return definition;
  }

  @Override
  public Optional<RuleMatch> evaluate(Transaction tx) {
    WindowSnapshot small = windows.outgoing(tx.source(), window, tx.timestamp()).below(smallLimit);
    if (small.count() < minCount || small.total().compareTo(minTotal) < 0) {
      return Optional.empty();
    }
    String explanation =
        ("La cuenta %s envió %d transferencias menores a %s en los últimos %d minutos, "
                + "acumulando $%,.2f MXN. Fraccionar un monto grande en operaciones pequeñas "
                + "(montos hormiga) es un patrón típico para evadir umbrales de detección.")
            .formatted(
                tx.source().masked(), small.count(), smallLimit, window.toMinutes(), small.total());
    return Optional.of(RuleMatch.of(definition, explanation));
  }
}
