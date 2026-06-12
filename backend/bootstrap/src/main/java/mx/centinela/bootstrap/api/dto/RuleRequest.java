package mx.centinela.bootstrap.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.rules.RuleDefinition;
import mx.centinela.domain.rules.RuleType;

/** Create/update payload for a rule. */
public record RuleRequest(
    @NotNull RuleType type,
    @NotBlank String name,
    String description,
    boolean enabled,
    @NotNull Severity severity,
    @Min(0) @Max(100) int weight,
    Map<String, Object> params) {

  public RuleDefinition toDomain(UUID id) {
    return new RuleDefinition(id, type, name, description, enabled, severity, weight, params);
  }
}
