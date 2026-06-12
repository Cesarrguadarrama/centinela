package mx.centinela.domain.model;

import java.util.List;
import java.util.Objects;
import mx.centinela.domain.rules.RuleMatch;

/** A transaction after rule evaluation: the observed fact plus its derived risk. */
public record ScoredTransaction(Transaction transaction, Score score, List<RuleMatch> matches) {

  public ScoredTransaction {
    Objects.requireNonNull(transaction, "transaction must not be null");
    Objects.requireNonNull(score, "score must not be null");
    matches = matches == null ? List.of() : List.copyOf(matches);
  }
}
