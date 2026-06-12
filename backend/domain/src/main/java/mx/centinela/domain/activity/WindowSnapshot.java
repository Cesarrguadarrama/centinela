package mx.centinela.domain.activity;

import java.math.BigDecimal;
import java.util.List;
import mx.centinela.domain.model.Money;

/**
 * The transfers of one account inside a time window, with the aggregations rules ask for. Totals
 * are plain {@link BigDecimal} because an empty window sums to zero, which {@link Money} (strictly
 * positive) rightly refuses to represent.
 */
public record WindowSnapshot(List<WindowEntry> entries) {

  public static final WindowSnapshot EMPTY = new WindowSnapshot(List.of());

  public WindowSnapshot {
    entries = entries == null ? List.of() : List.copyOf(entries);
  }

  public int count() {
    return entries.size();
  }

  public BigDecimal total() {
    return entries.stream().map(e -> e.amount().amount()).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public long distinctCounterparties() {
    return entries.stream().map(WindowEntry::counterparty).distinct().count();
  }

  /** The sub-window of transfers strictly below {@code limit} — the "hormiga" slice. */
  public WindowSnapshot below(Money limit) {
    return new WindowSnapshot(entries.stream().filter(e -> e.amount().isLessThan(limit)).toList());
  }
}
