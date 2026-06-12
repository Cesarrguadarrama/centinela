package mx.centinela.bootstrap.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** One transaction in an account's history. */
public record TransactionView(
    String id,
    String sourceClabe,
    String destinationClabe,
    BigDecimal amount,
    String concept,
    Instant timestamp,
    int score) {}
