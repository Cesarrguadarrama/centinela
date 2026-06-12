package mx.centinela.domain.model;

import static mx.centinela.domain.Fixtures.transactionOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.UUID;
import mx.centinela.domain.rules.RuleMatch;
import org.junit.jupiter.api.Test;

class AlertTest {

  private final Alert alert =
      Alert.raise(
          transactionOf("49999.00"),
          new RuleMatch(UUID.randomUUID(), "Monto bajo umbral", Severity.HIGH, 35, "explicación"),
          Instant.parse("2026-06-12T18:00:00Z"));

  @Test
  void newAlertsStartUntriaged() {
    assertThat(alert.status()).isEqualTo(AlertStatus.NEW);
  }

  @Test
  void analystCanReviewAndReclassify() {
    Alert reviewed = alert.triage(AlertStatus.REVIEWED);
    assertThat(reviewed.status()).isEqualTo(AlertStatus.REVIEWED);

    Alert reconsidered = reviewed.triage(AlertStatus.FALSE_POSITIVE);
    assertThat(reconsidered.status()).isEqualTo(AlertStatus.FALSE_POSITIVE);
    // identity and content stay intact through transitions
    assertThat(reconsidered.id()).isEqualTo(alert.id());
    assertThat(reconsidered.explanation()).isEqualTo(alert.explanation());
  }

  @Test
  void cannotReturnToNew() {
    Alert reviewed = alert.triage(AlertStatus.REVIEWED);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> reviewed.triage(AlertStatus.NEW))
        .withMessageContaining("NEW");
  }

  @Test
  void refusesBlankExplanation() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new Alert(
                    AlertId.newId(),
                    TransactionId.newId(),
                    UUID.randomUUID(),
                    "regla",
                    Severity.LOW,
                    "  ",
                    AlertStatus.NEW,
                    Instant.now()))
        .withMessageContaining("explanation");
  }
}
