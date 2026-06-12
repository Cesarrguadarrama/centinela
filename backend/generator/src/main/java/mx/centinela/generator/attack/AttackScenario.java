package mx.centinela.generator.attack;

/** Fraud patterns the generator can inject on demand to demonstrate detection. */
public enum AttackScenario {
  /** One account receives transfers from many sources and disperses the funds within seconds. */
  MULE,
  /** Many small transfers (montos hormiga) kept under reporting thresholds. */
  SMURFING,
  /** A burst of transfers from a single account within a very short window. */
  VELOCITY,
  /** Transfers with timestamps in the small hours, far from the account's usual schedule. */
  OFF_HOURS
}
