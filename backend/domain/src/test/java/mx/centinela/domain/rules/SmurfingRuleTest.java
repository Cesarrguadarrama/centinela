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

class SmurfingRuleTest {

  private static final Instant BASE = Instant.parse("2026-06-12T18:30:00Z");

  private final RuleDefinition definition =
      new RuleDefinition(
          UUID.randomUUID(),
          RuleType.SMURFING,
          "Montos hormiga",
          true,
          Severity.HIGH,
          45,
          Map.of(
              "maxAmountPesos", 10_000,
              "minCount", 15,
              "minTotalPesos", 50_000,
              "windowMinutes", 10));

  private final InMemoryActivityWindow windows = new InMemoryActivityWindow();
  private final SmurfingRule rule = new SmurfingRule(definition, windows);

  @Test
  void firesOnManySmallTransfersAddingUpToALargeAmount() {
    registerSmallTransfers(16, "4000"); // 16 x $4,000 = $64,000

    var match = rule.evaluate(transactionOf("4000", BASE)).orElseThrow();
    assertThat(match.explanation())
        .contains("16 transferencias")
        .contains("hormiga")
        .contains("$64,000.00");
  }

  @Test
  void staysQuietWhenTotalIsSmallEvenWithManyTransfers() {
    registerSmallTransfers(20, "500"); // 20 x $500 = $10,000 — many but immaterial

    assertThat(rule.evaluate(transactionOf("500", BASE))).isEmpty();
  }

  @Test
  void largeTransfersDoNotCountTowardsTheAntPattern() {
    registerSmallTransfers(10, "4000");
    registerSmallTransfers(6, "25000"); // big ones are not "hormiga"

    assertThat(rule.evaluate(transactionOf("4000", BASE))).isEmpty();
  }

  private void registerSmallTransfers(int count, String amount) {
    for (int i = 0; i < count; i++) {
      windows.register(
          new Transaction(
              TransactionId.newId(),
              SOURCE,
              DESTINATION,
              Money.of(amount),
              "pago",
              BASE.minusSeconds(i + 1)));
    }
  }
}
