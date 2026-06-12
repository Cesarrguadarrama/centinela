package mx.centinela.domain.port.out;

import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.ScoredTransaction;

/**
 * Live notification of raised alerts (SSE to the dashboard). Best-effort: a failed delivery must
 * never break the detection pipeline — alerts are already durable in the repository.
 */
public interface AlertStreamPort {

  void publish(Alert alert, ScoredTransaction context);
}
