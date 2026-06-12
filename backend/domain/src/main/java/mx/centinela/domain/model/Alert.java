package mx.centinela.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import mx.centinela.domain.rules.RuleMatch;

/**
 * A fraud signal raised by one rule over one transaction. The {@code explanation} is written for a
 * human analyst — it must make sense without reading the rule's code.
 */
public record Alert(
    AlertId id,
    TransactionId transactionId,
    UUID ruleId,
    String ruleName,
    Severity severity,
    String explanation,
    AlertStatus status,
    Instant createdAt) {

  public Alert {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(transactionId, "transactionId must not be null");
    Objects.requireNonNull(ruleId, "ruleId must not be null");
    Objects.requireNonNull(severity, "severity must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    if (explanation == null || explanation.isBlank()) {
      throw new IllegalArgumentException("an alert without explanation is useless to an analyst");
    }
  }

  public static Alert raise(Transaction transaction, RuleMatch match, Instant now) {
    return new Alert(
        AlertId.newId(),
        transaction.id(),
        match.ruleId(),
        match.ruleName(),
        match.severity(),
        match.explanation(),
        AlertStatus.NEW,
        now);
  }

  /**
   * Analyst triage. An alert never returns to NEW — that state means "nobody has looked at this
   * yet", and unseeing is not a thing. REVIEWED and FALSE_POSITIVE can be corrected to each other.
   */
  public Alert triage(AlertStatus target) {
    if (target == AlertStatus.NEW) {
      throw new IllegalArgumentException("an alert cannot be returned to NEW");
    }
    return new Alert(id, transactionId, ruleId, ruleName, severity, explanation, target, createdAt);
  }
}
