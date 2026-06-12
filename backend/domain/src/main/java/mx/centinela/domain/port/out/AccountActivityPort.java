package mx.centinela.domain.port.out;

import java.time.Instant;
import mx.centinela.domain.model.Clabe;

/**
 * Recent-activity questions the rules need answered. Phase 2 implements this with an indexed
 * Postgres count; phase 3 swaps in Redis sliding windows behind the same port.
 */
public interface AccountActivityPort {

  /** Transfers sent by {@code source} since {@code since}, including the one being evaluated. */
  long countTransfersFrom(Clabe source, Instant since);
}
