package mx.centinela.domain.rules;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import mx.centinela.domain.activity.WindowSnapshot;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.ActivityWindowPort;

/**
 * Detects mule accounts: funds converge from many distinct senders and leave again almost
 * immediately. Evaluated on outgoing transfers — the dispersal is the moment the pattern completes.
 *
 * <p>Params: {@code minIncoming} (default 8), {@code minDistinctSenders} (default 5), {@code
 * dispersalRatio} (default 0.5 — fraction of received funds already sent out), {@code
 * windowMinutes} (default 15).
 */
public final class MuleAccountRule implements FraudRule {

  private final RuleDefinition definition;
  private final ActivityWindowPort windows;
  private final int minIncoming;
  private final int minDistinctSenders;
  private final BigDecimal dispersalRatio;
  private final Duration window;

  public MuleAccountRule(RuleDefinition definition, ActivityWindowPort windows) {
    this.definition = definition;
    this.windows = windows;
    this.minIncoming = definition.intParam("minIncoming", 8);
    this.minDistinctSenders = definition.intParam("minDistinctSenders", 5);
    this.dispersalRatio = definition.decimalParam("dispersalRatio", "0.5");
    this.window = Duration.ofMinutes(definition.intParam("windowMinutes", 15));
  }

  @Override
  public RuleDefinition definition() {
    return definition;
  }

  @Override
  public Optional<RuleMatch> evaluate(Transaction tx) {
    WindowSnapshot incoming = windows.incoming(tx.source(), window, tx.timestamp());
    if (incoming.count() < minIncoming || incoming.distinctCounterparties() < minDistinctSenders) {
      return Optional.empty();
    }
    BigDecimal received = incoming.total();
    BigDecimal dispersed = windows.outgoing(tx.source(), window, tx.timestamp()).total();
    if (dispersed.compareTo(received.multiply(dispersalRatio)) < 0) {
      return Optional.empty();
    }
    int dispersedPct =
        dispersed
            .multiply(BigDecimal.valueOf(100))
            .divide(received, java.math.RoundingMode.DOWN)
            .intValue();
    String explanation =
        ("La cuenta %s recibió %d depósitos de %d remitentes distintos por $%,.2f MXN en los "
                + "últimos %d minutos y ya dispersó $%,.2f MXN (%d%% de lo recibido). Recibir de "
                + "muchas fuentes y vaciar la cuenta de inmediato es el comportamiento típico de "
                + "una cuenta mula.")
            .formatted(
                tx.source().masked(),
                incoming.count(),
                incoming.distinctCounterparties(),
                received,
                window.toMinutes(),
                dispersed,
                dispersedPct);
    return Optional.of(RuleMatch.of(definition, explanation));
  }
}
