package mx.centinela.bootstrap.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.AlertStatus;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.model.TransactionId;

@Entity
@Table(name = "alerts")
class AlertEntity {

  @Id private UUID id;

  @Column(name = "transaction_id", nullable = false)
  private UUID transactionId;

  @Column(name = "rule_id", nullable = false)
  private UUID ruleId;

  @Column(name = "rule_name", length = 120, nullable = false)
  private String ruleName;

  @Enumerated(EnumType.STRING)
  @Column(length = 16, nullable = false)
  private Severity severity;

  @Column(nullable = false)
  private String explanation;

  @Enumerated(EnumType.STRING)
  @Column(length = 24, nullable = false)
  private AlertStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AlertEntity() {}

  static AlertEntity from(Alert alert) {
    AlertEntity entity = new AlertEntity();
    entity.id = alert.id().value();
    entity.transactionId = alert.transactionId().value();
    entity.ruleId = alert.ruleId();
    entity.ruleName = alert.ruleName();
    entity.severity = alert.severity();
    entity.explanation = alert.explanation();
    entity.status = alert.status();
    entity.createdAt = alert.createdAt();
    return entity;
  }

  Alert toDomain() {
    return new Alert(
        new AlertId(id),
        new TransactionId(transactionId),
        ruleId,
        ruleName,
        severity,
        explanation,
        status,
        createdAt);
  }
}
