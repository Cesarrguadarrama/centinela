package mx.centinela.domain.rules;

import static mx.centinela.domain.Fixtures.DESTINATION;
import static mx.centinela.domain.Fixtures.SOURCE;
import static mx.centinela.domain.Fixtures.transactionOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.activity.InMemoryActivityWindow;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;
import org.junit.jupiter.api.Test;

class VelocityRuleTest {

  private static final Instant BASE = Instant.parse("2026-06-12T18:30:00Z");

  private final RuleDefinition definition =
      new RuleDefinition(
          UUID.randomUUID(),
          RuleType.VELOCITY,
          "Velocidad anómala",
          "",
          true,
          Severity.HIGH,
          40,
          Map.of("maxTransfers", 10, "windowMinutes", 5));

  private final InMemoryActivityWindow windows = new InMemoryActivityWindow();
  private final VelocityRule rule = new VelocityRule(definition, windows);

  @Test
  void staysQuietAtTheLimit() {
    registerBurst(10, BASE);

    assertThat(rule.evaluate(transactionOf("1000", BASE))).isEmpty();
  }

  @Test
  void firesAboveTheLimit() {
    registerBurst(12, BASE);

    var match = rule.evaluate(transactionOf("1000", BASE)).orElseThrow();
    assertThat(match.explanation()).contains("12 transferencias").contains("5 minutos");
    assertThat(match.weight()).isEqualTo(40);
  }

  @Test
  void ignoresTransfersOutsideTheEventTimeWindow() {
    registerBurst(12, BASE.minusSeconds(600)); // 10 min before: outside the 5-min window

    assertThat(rule.evaluate(transactionOf("1000", BASE))).isEmpty();
  }

  private void registerBurst(int count, Instant around) {
    for (int i = 0; i < count; i++) {
      Transaction tx =
          new Transaction(
              TransactionId.newId(),
              SOURCE,
              DESTINATION,
              Money.pesos(1000),
              "burst",
              around.minusSeconds(i));
      windows.register(tx);
    }
  }
}
