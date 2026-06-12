package mx.centinela.bootstrap.api.dto;

import java.math.BigDecimal;

/** Risk-oriented digest of one account over the trailing window. */
public record AccountSummary(
    String clabe,
    int windowHours,
    long sentCount,
    BigDecimal sentTotal,
    long receivedCount,
    BigDecimal receivedTotal,
    long alertCount,
    int maxScore) {}
