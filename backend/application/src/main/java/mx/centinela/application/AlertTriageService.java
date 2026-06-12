package mx.centinela.application;

import java.util.NoSuchElementException;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.AlertStatus;
import mx.centinela.domain.port.in.TriageAlertUseCase;
import mx.centinela.domain.port.out.AlertRepository;

/** Loads, transitions and persists alerts; the transition rules live in the Alert itself. */
public class AlertTriageService implements TriageAlertUseCase {

  private final AlertRepository alerts;

  public AlertTriageService(AlertRepository alerts) {
    this.alerts = alerts;
  }

  @Override
  public Alert triage(AlertId id, AlertStatus target) {
    Alert alert =
        alerts.findById(id).orElseThrow(() -> new NoSuchElementException("alert not found: " + id));
    Alert triaged = alert.triage(target);
    alerts.save(triaged);
    return triaged;
  }
}
