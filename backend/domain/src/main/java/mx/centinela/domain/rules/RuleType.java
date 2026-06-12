package mx.centinela.domain.rules;

/** The fraud-pattern families the engine knows how to evaluate. */
public enum RuleType {
  /** Amount suspiciously close below a reporting threshold (structuring). */
  SUB_THRESHOLD_AMOUNT,
  /** Too many transfers from one account within a short window. */
  VELOCITY,
  /** Activity in hours where legitimate traffic is rare. */
  OFF_HOURS
}
