package mx.centinela.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.AlertStatus;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.TransactionId;
import mx.centinela.domain.port.out.AlertRepository;
import org.junit.jupiter.api.Test;

class AlertTriageServiceTest {

  private final Map<AlertId, Alert> store = new HashMap<>();

  private final AlertRepository repository =
      new AlertRepository() {
        @Override
        public void save(Alert alert) {
          store.put(alert.id(), alert);
        }

        @Override
        public Optional<Alert> findById(AlertId id) {
          return Optional.ofNullable(store.get(id));
        }
      };

  private final AlertTriageService service = new AlertTriageService(repository);

  @Test
  void transitionsAndPersists() {
    Alert alert = newAlert();
    store.put(alert.id(), alert);

    Alert result = service.triage(alert.id(), AlertStatus.FALSE_POSITIVE);

    assertThat(result.status()).isEqualTo(AlertStatus.FALSE_POSITIVE);
    assertThat(store.get(alert.id()).status()).isEqualTo(AlertStatus.FALSE_POSITIVE);
  }

  @Test
  void unknownAlertIs404Material() {
    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> service.triage(AlertId.newId(), AlertStatus.REVIEWED));
  }

  private Alert newAlert() {
    return new Alert(
        AlertId.newId(),
        TransactionId.newId(),
        UUID.randomUUID(),
        "Velocidad anómala",
        Severity.HIGH,
        "explicación de prueba",
        AlertStatus.NEW,
        Instant.parse("2026-06-12T18:00:00Z"));
  }
}
