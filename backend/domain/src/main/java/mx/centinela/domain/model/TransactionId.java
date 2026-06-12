package mx.centinela.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Unique identifier of a SPEI transaction inside the platform. */
public record TransactionId(UUID value) {

  public TransactionId {
    Objects.requireNonNull(value, "transaction id must not be null");
  }

  public static TransactionId newId() {
    return new TransactionId(UUID.randomUUID());
  }

  public static TransactionId of(String value) {
    return new TransactionId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
