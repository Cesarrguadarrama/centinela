package mx.centinela.domain.activity;

import java.time.Instant;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;

/** One transfer inside a sliding window: how much, with whom, when. */
public record WindowEntry(Money amount, Clabe counterparty, Instant at) {}
