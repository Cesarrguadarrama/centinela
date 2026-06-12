package mx.centinela.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Unique identifier of a fraud alert. */
public record AlertId(UUID value) {

  public AlertId {
    Objects.requireNonNull(value, "alert id must not be null");
  }

  public static AlertId newId() {
    return new AlertId(UUID.randomUUID());
  }

  public static AlertId of(String value) {
    return new AlertId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
