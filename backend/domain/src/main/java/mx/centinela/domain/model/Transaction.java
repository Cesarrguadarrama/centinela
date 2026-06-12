package mx.centinela.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * An interbank SPEI transfer as observed by the platform. Immutable: detection never mutates
 * transactions, it only derives signals and alerts from them.
 */
public record Transaction(
    TransactionId id,
    Clabe source,
    Clabe destination,
    Money amount,
    String concept,
    Instant timestamp) {

  public Transaction {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(destination, "destination must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(timestamp, "timestamp must not be null");
    if (source.equals(destination)) {
      throw new IllegalArgumentException("source and destination CLABE must differ");
    }
    concept = concept == null ? "" : concept.strip();
  }
}
