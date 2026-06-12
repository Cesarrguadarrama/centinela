package mx.centinela.domain.port.out;

import mx.centinela.domain.model.Alert;

/** Persistence of raised alerts. */
public interface AlertRepository {

  void save(Alert alert);
}
