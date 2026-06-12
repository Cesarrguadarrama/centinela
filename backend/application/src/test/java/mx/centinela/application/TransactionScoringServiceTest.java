package mx.centinela.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import mx.centinela.domain.activity.WindowSnapshot;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.AlertStatus;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.ScoredTransaction;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;
import mx.centinela.domain.port.out.ActivityWindowPort;
import mx.centinela.domain.port.out.AlertRepository;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.rules.RuleDefinition;
import mx.centinela.domain.rules.RuleType;
import org.junit.jupiter.api.Test;

class TransactionScoringServiceTest {

  private static final Instant NOW = Instant.parse("2026-06-12T18:00:00Z");

  private final List<ScoredTransaction> savedTransactions = new ArrayList<>();
  private final List<Alert> savedAlerts = new ArrayList<>();
  private final List<RuleDefinition> enabledRules = new ArrayList<>();
  private final List<Transaction> registeredInWindows = new ArrayList<>();

  private final ActivityWindowPort emptyWindows =
      new ActivityWindowPort() {
        @Override
        public void register(Transaction transaction) {
          registeredInWindows.add(transaction);
        }

        @Override
        public WindowSnapshot outgoing(Clabe account, Duration window, Instant asOf) {
          return WindowSnapshot.EMPTY;
        }

        @Override
        public WindowSnapshot incoming(Clabe account, Duration window, Instant asOf) {
          return WindowSnapshot.EMPTY;
        }
      };

  private final List<Alert> streamedAlerts = new ArrayList<>();

  private final RuleRepository ruleRepository =
      new RuleRepository() {
        @Override
        public List<RuleDefinition> findEnabled() {
          return enabledRules.stream().filter(RuleDefinition::enabled).toList();
        }

        @Override
        public List<RuleDefinition> findAll() {
          return List.copyOf(enabledRules);
        }

        @Override
        public Optional<RuleDefinition> findById(UUID id) {
          return enabledRules.stream().filter(r -> r.id().equals(id)).findFirst();
        }

        @Override
        public void save(RuleDefinition definition) {
          enabledRules.add(definition);
        }
      };

  private final AlertRepository alertRepository =
      new AlertRepository() {
        @Override
        public void save(Alert alert) {
          savedAlerts.add(alert);
        }

        @Override
        public Optional<Alert> findById(AlertId id) {
          return savedAlerts.stream().filter(a -> a.id().equals(id)).findFirst();
        }
      };

  private final TransactionScoringService service =
      new TransactionScoringService(
          savedTransactions::add,
          ruleRepository,
          alertRepository,
          emptyWindows,
          (alert, context) -> streamedAlerts.add(alert),
          new RuleFactory(emptyWindows),
          Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void persistsScoredTransactionEvenWhenNoRuleFires() {
    service.process(transactionOf("100.00"));

    assertThat(savedTransactions).hasSize(1);
    assertThat(savedTransactions.get(0).score().value()).isZero();
    assertThat(savedAlerts).isEmpty();
  }

  @Test
  void registersTransactionInWindowsBeforeEvaluating() {
    service.process(transactionOf("100.00"));

    assertThat(registeredInWindows).hasSize(1);
  }

  @Test
  void raisesAlertAndAccumulatesScorePerMatchingRule() {
    enabledRules.add(subThresholdRule(35));

    service.process(transactionOf("49999.00"));

    assertThat(savedAlerts).hasSize(1);
    Alert alert = savedAlerts.get(0);
    assertThat(alert.status()).isEqualTo(AlertStatus.NEW);
    assertThat(alert.severity()).isEqualTo(Severity.HIGH);
    assertThat(alert.createdAt()).isEqualTo(NOW);
    assertThat(alert.explanation()).isNotBlank();

    assertThat(savedTransactions.get(0).score().value()).isEqualTo(35);
    assertThat(savedTransactions.get(0).matches()).hasSize(1);
    assertThat(streamedAlerts).containsExactlyElementsOf(savedAlerts);
  }

  @Test
  void skipsDisabledRules() {
    enabledRules.add(
        new RuleDefinition(
            UUID.randomUUID(),
            RuleType.SUB_THRESHOLD_AMOUNT,
            "Apagada",
            "",
            false,
            Severity.HIGH,
            35,
            Map.of()));

    service.process(transactionOf("49999.00"));

    assertThat(savedAlerts).isEmpty();
    assertThat(savedTransactions.get(0).score().value()).isZero();
  }

  private RuleDefinition subThresholdRule(int weight) {
    return new RuleDefinition(
        UUID.randomUUID(),
        RuleType.SUB_THRESHOLD_AMOUNT,
        "Monto bajo umbral",
        "",
        true,
        Severity.HIGH,
        weight,
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
