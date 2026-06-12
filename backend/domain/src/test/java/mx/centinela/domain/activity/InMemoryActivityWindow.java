package mx.centinela.domain.activity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.ActivityWindowPort;

/** In-memory windows for rule tests — same event-time semantics as the Redis adapter. */
public class InMemoryActivityWindow implements ActivityWindowPort {

  private final List<Transaction> registered = new ArrayList<>();

  @Override
  public void register(Transaction transaction) {
    registered.add(transaction);
  }

  @Override
  public WindowSnapshot outgoing(Clabe account, Duration window, Instant asOf) {
    return snapshot(account, window, asOf, true);
  }

  @Override
  public WindowSnapshot incoming(Clabe account, Duration window, Instant asOf) {
    return snapshot(account, window, asOf, false);
  }

  private WindowSnapshot snapshot(Clabe account, Duration window, Instant asOf, boolean out) {
    Instant since = asOf.minus(window);
    List<WindowEntry> entries =
        registered.stream()
            .filter(tx -> (out ? tx.source() : tx.destination()).equals(account))
            .filter(tx -> !tx.timestamp().isBefore(since) && !tx.timestamp().isAfter(asOf))
            .map(
                tx ->
                    new WindowEntry(
                        tx.amount(), out ? tx.destination() : tx.source(), tx.timestamp()))
            .toList();
    return new WindowSnapshot(entries);
  }
}
