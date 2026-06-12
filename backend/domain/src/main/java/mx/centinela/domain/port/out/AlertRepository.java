package mx.centinela.domain.port.out;

import java.util.Optional;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;

/** Persistence of raised alerts. */
public interface AlertRepository {

  void save(Alert alert);

  Optional<Alert> findById(AlertId id);
}
