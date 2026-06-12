package mx.centinela.domain.rules;

import java.time.Duration;
import java.util.Optional;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.ActivityWindowPort;

/**
 * Flags accounts firing too many transfers in a short window — automated dispersal, account
 * takeover or mule cash-out behavior.
 *
 * <p>Params: {@code maxTransfers} (default 10), {@code windowMinutes} (default 5) — fires when the
 * account exceeds {@code maxTransfers} within the window ending at the transaction's own timestamp
 * (event time, not ingestion time).
 */
public final class VelocityRule implements FraudRule {

  private final RuleDefinition definition;
  private final ActivityWindowPort windows;
  private final int maxTransfers;
  private final Duration window;

  public VelocityRule(RuleDefinition definition, ActivityWindowPort windows) {
    this.definition = definition;
    this.windows = windows;
    this.maxTransfers = definition.intParam("maxTransfers", 10);
    this.window = Duration.ofMinutes(definition.intParam("windowMinutes", 5));
  }

  @Override
  public RuleDefinition definition() {
    return definition;
  }

  @Override
  public Optional<RuleMatch> evaluate(Transaction tx) {
    int recent = windows.outgoing(tx.source(), window, tx.timestamp()).count();
    if (recent <= maxTransfers) {
      return Optional.empty();
    }
    String explanation =
        ("La cuenta %s acumuló %d transferencias salientes en los últimos %d minutos; "
                + "el límite configurado es %d. Este ritmo es consistente con dispersión "
                + "automatizada de fondos o una cuenta comprometida.")
            .formatted(tx.source().masked(), recent, window.toMinutes(), maxTransfers);
    return Optional.of(RuleMatch.of(definition, explanation));
  }
}
