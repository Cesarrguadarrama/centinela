package mx.centinela.bootstrap.infrastructure.sse;

import java.math.BigDecimal;
import java.time.Instant;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.ScoredTransaction;

/** Wire format of one alert on the SSE stream — everything the live feed renders. */
public record AlertStreamEvent(
    String alertId,
    String transactionId,
    String ruleName,
    String severity,
    String explanation,
    String sourceClabe,
    String destinationClabe,
    BigDecimal amount,
    int transactionScore,
    Instant createdAt) {

  static AlertStreamEvent from(Alert alert, ScoredTransaction context) {
    return new AlertStreamEvent(
        alert.id().toString(),
        alert.transactionId().toString(),
        alert.ruleName(),
        alert.severity().name(),
        alert.explanation(),
        context.transaction().source().value(),
        context.transaction().destination().value(),
        context.transaction().amount().amount(),
        context.score().value(),
        alert.createdAt());
  }
}
