package mx.centinela.domain.rules;

import java.util.UUID;
import mx.centinela.domain.model.Severity;

/** The outcome of a rule that fired: which rule, how severe, why, and its score contribution. */
public record RuleMatch(
    UUID ruleId, String ruleName, Severity severity, int weight, String explanation) {

  public static RuleMatch of(RuleDefinition definition, String explanation) {
    return new RuleMatch(
        definition.id(),
        definition.name(),
        definition.severity(),
        definition.weight(),
        explanation);
  }
}
