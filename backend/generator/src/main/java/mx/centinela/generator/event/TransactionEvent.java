package mx.centinela.generator.event;

import java.math.BigDecimal;
import java.time.Instant;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;

/**
 * Wire format of a SPEI transaction on the {@code spei.transactions} topic. This JSON schema —
 * documented in docs/events.md — is the contract with the detection engine.
 */
public record TransactionEvent(
    String id,
    String sourceClabe,
    String destinationClabe,
    BigDecimal amount,
    String currency,
    String concept,
    Instant timestamp) {

  public static TransactionEvent from(Transaction tx) {
    return new TransactionEvent(
        tx.id().toString(),
        tx.source().value(),
        tx.destination().value(),
        tx.amount().amount(),
        Money.CURRENCY,
        tx.concept(),
        tx.timestamp());
  }
}
