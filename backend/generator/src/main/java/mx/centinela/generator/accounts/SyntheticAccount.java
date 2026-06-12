package mx.centinela.generator.accounts;

import mx.centinela.domain.model.Clabe;

/**
 * A fictional account with a behavioral profile: how much it usually moves per transfer. Normal
 * traffic samples amounts around this mean so each account has a recognizable pattern — which is
 * exactly what makes anomalies detectable.
 *
 * @param typicalAmount mean transfer amount in pesos for this account's log-normal distribution
 */
public record SyntheticAccount(Clabe clabe, String holderName, double typicalAmount) {}
