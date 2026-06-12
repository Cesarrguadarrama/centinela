package mx.centinela.domain.rules;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import mx.centinela.domain.model.Severity;

/**
 * A rule's runtime configuration as stored by operations — type selects the evaluation logic,
 * {@code params} tunes it, {@code weight} is its contribution to the composite score. Editing a row
 * in the rules table changes detection behavior without a deploy.
 */
public record RuleDefinition(
    UUID id,
    RuleType type,
    String name,
    boolean enabled,
    Severity severity,
    int weight,
    Map<String, Object> params) {

  public RuleDefinition {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(severity, "severity must not be null");
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("rule name must not be blank");
    }
    if (weight < 0 || weight > 100) {
      throw new IllegalArgumentException("rule weight must be within 0-100: " + weight);
    }
    params = params == null ? Map.of() : Map.copyOf(params);
  }

  public int intParam(String key, int defaultValue) {
    Object raw = params.get(key);
    if (raw == null) {
      return defaultValue;
    }
    if (raw instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(raw.toString());
  }

  public BigDecimal decimalParam(String key, String defaultValue) {
    Object raw = params.get(key);
    if (raw == null) {
      return new BigDecimal(defaultValue);
    }
    if (raw instanceof BigDecimal decimal) {
      return decimal;
    }
    return new BigDecimal(raw.toString());
  }

  public String stringParam(String key, String defaultValue) {
    Object raw = params.get(key);
    return raw == null ? defaultValue : raw.toString();
  }
}
