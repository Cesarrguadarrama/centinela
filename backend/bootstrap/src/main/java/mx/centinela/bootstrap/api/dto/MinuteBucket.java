package mx.centinela.bootstrap.api.dto;

import java.time.Instant;

/** Transactions and alerts ingested during one minute. */
public record MinuteBucket(Instant minute, long transactions, long alerts) {}
