package mx.centinela.domain.port.in;

import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.AlertStatus;

/** Analyst actions over an alert's lifecycle. */
public interface TriageAlertUseCase {

  /** Transitions the alert to {@code target}; NEW is not a valid target. */
  Alert triage(AlertId id, AlertStatus target);
}
