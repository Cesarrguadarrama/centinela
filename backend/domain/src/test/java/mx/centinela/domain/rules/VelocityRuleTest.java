package mx.centinela.domain.rules;

import static mx.centinela.domain.Fixtures.transactionOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.AccountActivityPort;
import org.junit.jupiter.api.Test;

class VelocityRuleTest {

  private final RuleDefinition definition =
      new RuleDefinition(
          UUID.randomUUID(),
          RuleType.VELOCITY,
          "Velocidad anómala",
          true,
          Severity.HIGH,
          Map.of("maxTransfers", 10, "windowMinutes", 5));

  @Test
  void staysQuietAtTheLimit() {
    VelocityRule rule = new VelocityRule(definition, (source, since) -> 10);

    assertThat(rule.evaluate(transactionOf("1000"))).isEmpty();
  }

  @Test
  void firesAboveTheLimit() {
    VelocityRule rule = new VelocityRule(definition, (source, since) -> 12);

    var match = rule.evaluate(transactionOf("1000")).orElseThrow();
    assertThat(match.explanation()).contains("12 transferencias").contains("5 minutos");
  }

  @Test
  void usesEventTimeForTheWindow() {
    Instant eventTime = Instant.parse("2026-06-12T03:00:00Z");
    Transaction tx = transactionOf("1000", eventTime);

    var capturedSince = new Instant[1];
    AccountActivityPort capturing =
        (source, since) -> {
          capturedSince[0] = since;
          return 0;
        };

    new VelocityRule(definition, capturing).evaluate(tx);

    assertThat(capturedSince[0]).isEqualTo(eventTime.minusSeconds(300));
  }
}
