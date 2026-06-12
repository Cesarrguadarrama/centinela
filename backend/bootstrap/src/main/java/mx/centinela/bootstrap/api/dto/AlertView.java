package mx.centinela.bootstrap.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** One alert as the dashboard lists it — alert fields plus its transaction's context. */
public record AlertView(
    String id,
    String transactionId,
    String ruleName,
    String severity,
    String explanation,
    String status,
    Instant createdAt,
    String sourceClabe,
    String destinationClabe,
    BigDecimal amount,
    int transactionScore) {}
