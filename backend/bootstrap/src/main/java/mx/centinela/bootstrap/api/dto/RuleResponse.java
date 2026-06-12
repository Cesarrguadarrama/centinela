package mx.centinela.bootstrap.api.dto;

import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.rules.RuleDefinition;

/** A rule as exposed by the API. */
public record RuleResponse(
    UUID id,
    String type,
    String name,
    String description,
    boolean enabled,
    String severity,
    int weight,
    Map<String, Object> params) {

  public static RuleResponse from(RuleDefinition d) {
    return new RuleResponse(
        d.id(),
        d.type().name(),
        d.name(),
        d.description(),
        d.enabled(),
        d.severity().name(),
        d.weight(),
        d.params());
  }
}
