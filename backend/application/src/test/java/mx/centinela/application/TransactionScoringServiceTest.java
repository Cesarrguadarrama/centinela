package mx.centinela.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertStatus;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;
import mx.centinela.domain.rules.RuleDefinition;
import mx.centinela.domain.rules.RuleType;
import org.junit.jupiter.api.Test;

class TransactionScoringServiceTest {

  private static final Instant NOW = Instant.parse("2026-06-12T18:00:00Z");

  private final List<Transaction> savedTransactions = new ArrayList<>();
  private final List<Alert> savedAlerts = new ArrayList<>();
  private final List<RuleDefinition> enabledRules = new ArrayList<>();

  private final TransactionScoringService service =
      new TransactionScoringService(
          savedTransactions::add,
          () -> List.copyOf(enabledRules),
          savedAlerts::add,
          new RuleFactory((source, since) -> 0),
          Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void persistsTransactionEvenWhenNoRuleFires() {
    service.process(transactionOf("100.00"));

    assertThat(savedTransactions).hasSize(1);
    assertThat(savedAlerts).isEmpty();
  }

  @Test
  void raisesOneAlertPerMatchingRule() {
    enabledRules.add(subThresholdRule());

    service.process(transactionOf("49999.00"));

    assertThat(savedAlerts).hasSize(1);
    Alert alert = savedAlerts.get(0);
    assertThat(alert.status()).isEqualTo(AlertStatus.NEW);
    assertThat(alert.severity()).isEqualTo(Severity.HIGH);
    assertThat(alert.createdAt()).isEqualTo(NOW);
    assertThat(alert.transactionId()).isEqualTo(savedTransactions.get(0).id());
    assertThat(alert.explanation()).isNotBlank();
  }

  @Test
  void skipsDisabledRules() {
    enabledRules.add(
        new RuleDefinition(
            UUID.randomUUID(),
            RuleType.SUB_THRESHOLD_AMOUNT,
            "Apagada",
            false,
            Severity.HIGH,
            Map.of()));

    service.process(transactionOf("49999.00"));

    assertThat(savedAlerts).isEmpty();
  }

  private RuleDefinition subThresholdRule() {
    return new RuleDefinition(
        UUID.randomUUID(),
        RuleType.SUB_THRESHOLD_AMOUNT,
        "Monto bajo umbral",
        true,
        Severity.HIGH,
        Map.of("thresholdPesos", 50_000, "marginPesos", 5_000));
  }

  private Transaction transactionOf(String amount) {
    return new Transaction(
        TransactionId.newId(),
        Clabe.fromBaseDigits("01218000123456789"),
        Clabe.fromBaseDigits("00201077777777771"),
        Money.of(amount),
        "Pago factura",
        NOW);
  }
}
