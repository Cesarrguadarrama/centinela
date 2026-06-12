package mx.centinela.domain.model;

import java.util.Collection;
import mx.centinela.domain.rules.RuleMatch;

/**
 * Composite risk score of a transaction, 0–100. The sum of the weights of every rule that fired,
 * capped at 100 — one strong signal or several weak ones can both push a transaction into review.
 */
public record Score(int value) {

  public static final Score ZERO = new Score(0);
  private static final int MAX = 100;

  public Score {
    if (value < 0 || value > MAX) {
      throw new IllegalArgumentException("score must be within 0-100: " + value);
    }
  }

  public static Score fromMatches(Collection<RuleMatch> matches) {
    int sum = matches.stream().mapToInt(RuleMatch::weight).sum();
    return new Score(Math.min(sum, MAX));
  }
}
