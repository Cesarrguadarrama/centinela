package mx.centinela.bootstrap.infrastructure.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;

/**
 * Inbound mirror of the {@code spei.transactions} JSON contract (docs/events.md). Mapping to the
 * domain validates everything — bad CLABEs or amounts are rejected here, at the boundary.
 */
public record TransactionEventPayload(
    String id,
    String sourceClabe,
    String destinationClabe,
    BigDecimal amount,
    String currency,
    String concept,
    Instant timestamp) {

  public Transaction toDomain() {
    return new Transaction(
        TransactionId.of(id),
        Clabe.of(sourceClabe),
        Clabe.of(destinationClabe),
        Money.of(amount),
        concept,
        timestamp);
  }
}
