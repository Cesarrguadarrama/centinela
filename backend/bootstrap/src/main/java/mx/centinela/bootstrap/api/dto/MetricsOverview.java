package mx.centinela.bootstrap.api.dto;

import java.util.Map;

/** Headline numbers for the dashboard. */
public record MetricsOverview(
    long totalTransactions,
    long totalAlerts,
    Map<String, Long> alertsBySeverity,
    Map<String, Long> alertsByStatus,
    double averageScore) {}
