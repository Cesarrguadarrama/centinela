package mx.centinela.domain.port.out;

import java.time.Duration;
import java.time.Instant;
import mx.centinela.domain.activity.WindowSnapshot;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Transaction;

/**
 * Sliding windows of per-account activity, the hot-path state of the detection engine. Backed by
 * Redis sorted sets (phase 3); windows are queried by event time so reprocessing a backlog yields
 * the same results as live traffic.
 */
public interface ActivityWindowPort {

  /** Registers the transfer in its source's outgoing window and its destination's incoming one. */
  void register(Transaction transaction);

  /** Transfers sent by {@code account} within {@code window} ending at {@code asOf}. */
  WindowSnapshot outgoing(Clabe account, Duration window, Instant asOf);

  /** Transfers received by {@code account} within {@code window} ending at {@code asOf}. */
  WindowSnapshot incoming(Clabe account, Duration window, Instant asOf);
}
