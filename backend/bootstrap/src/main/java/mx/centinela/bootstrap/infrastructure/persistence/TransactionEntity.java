package mx.centinela.bootstrap.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.ScoredTransaction;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;

@Entity
@Table(name = "transactions")
class TransactionEntity {

  @Id private UUID id;

  @Column(name = "source_clabe", length = 18, nullable = false)
  private String sourceClabe;

  @Column(name = "destination_clabe", length = 18, nullable = false)
  private String destinationClabe;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Column(length = 3, nullable = false)
  private String currency;

  @Column(nullable = false)
  private String concept;

  @Column(name = "ts", nullable = false)
  private Instant timestamp;

  @Column(nullable = false)
  private int score;

  protected TransactionEntity() {}

  static TransactionEntity from(ScoredTransaction scored) {
    Transaction tx = scored.transaction();
    TransactionEntity entity = new TransactionEntity();
    entity.id = tx.id().value();
    entity.sourceClabe = tx.source().value();
    entity.destinationClabe = tx.destination().value();
    entity.amount = tx.amount().amount();
    entity.currency = Money.CURRENCY;
    entity.concept = tx.concept();
    entity.timestamp = tx.timestamp();
    entity.score = scored.score().value();
    return entity;
  }

  Transaction toDomain() {
    return new Transaction(
        new TransactionId(id),
        Clabe.of(sourceClabe),
        Clabe.of(destinationClabe),
        Money.of(amount),
        concept,
        timestamp);
  }
}
