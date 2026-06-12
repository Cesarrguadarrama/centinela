package mx.centinela.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.activity.InMemoryActivityWindow;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;
import org.junit.jupiter.api.Test;

class MuleAccountRuleTest {

  private static final Instant BASE = Instant.parse("2026-06-12T18:30:00Z");
  private static final Clabe MULE = Clabe.fromBaseDigits("05818099999999999");

  private final RuleDefinition definition =
      new RuleDefinition(
          UUID.randomUUID(),
          RuleType.MULE_ACCOUNT,
          "Cuenta mula",
          true,
          Severity.CRITICAL,
          60,
          Map.of(
              "minIncoming", 8,
              "minDistinctSenders", 5,
              "dispersalRatio", 0.5,
              "windowMinutes", 15));

  private final InMemoryActivityWindow windows = new InMemoryActivityWindow();
  private final MuleAccountRule rule = new MuleAccountRule(definition, windows);

  @Test
  void firesWhenFundsConvergeAndDisperseQuickly() {
    receiveFromDistinctSenders(8, "20000"); // $160,000 in
    Transaction firstPayout = payout("50000", 1); // $50k out — below 50% ratio
    windows.register(firstPayout);
    Transaction secondPayout = payout("50000", 2); // cumulative $100k >= 50% of $160k
    windows.register(secondPayout);

    assertThat(rule.evaluate(firstPayout)).isEmpty();
    var match = rule.evaluate(secondPayout).orElseThrow();
    assertThat(match.severity()).isEqualTo(Severity.CRITICAL);
    assertThat(match.explanation())
        .contains("8 depósitos")
        .contains("8 remitentes distintos")
        .contains("cuenta mula");
  }

  @Test
  void staysQuietWhenSendersAreNotDiverse() {
    // 8 deposits but all from the same 2 senders — a normal collector, not a mule
    Clabe senderA = Clabe.fromBaseDigits("01218000000000001");
    Clabe senderB = Clabe.fromBaseDigits("01218000000000002");
    for (int i = 0; i < 8; i++) {
      windows.register(
          new Transaction(
              TransactionId.newId(),
              i % 2 == 0 ? senderA : senderB,
              MULE,
              Money.of("20000"),
              "depósito",
              BASE.minusSeconds(60 + i)));
    }
    Transaction payout = payout("160000", 1);
    windows.register(payout);

    assertThat(rule.evaluate(payout)).isEmpty();
  }

  @Test
  void staysQuietWhenFundsAreNotDispersed() {
    receiveFromDistinctSenders(8, "20000");
    Transaction smallPayout = payout("5000", 1); // 3% of what came in
    windows.register(smallPayout);

    assertThat(rule.evaluate(smallPayout)).isEmpty();
  }

  private void receiveFromDistinctSenders(int count, String amount) {
    for (int i = 0; i < count; i++) {
      Clabe sender = Clabe.fromBaseDigits("012180000000%05d".formatted(i + 100));
      windows.register(
          new Transaction(
              TransactionId.newId(),
              sender,
              MULE,
              Money.of(amount),
              "depósito",
              BASE.minusSeconds(120 + i)));
    }
  }

  private Transaction payout(String amount, int sequence) {
    Clabe payee = Clabe.fromBaseDigits("072180000000%05d".formatted(sequence));
    return new Transaction(
        TransactionId.newId(), MULE, payee, Money.of(amount), "pago", BASE.plusSeconds(sequence));
  }
}
