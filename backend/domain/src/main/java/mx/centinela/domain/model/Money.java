package mx.centinela.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** An MXN monetary amount with cent precision. SPEI operates exclusively in Mexican pesos. */
public record Money(BigDecimal amount) implements Comparable<Money> {

  public static final String CURRENCY = "MXN";

  public Money {
    Objects.requireNonNull(amount, "amount must not be null");
    if (amount.signum() <= 0) {
      throw new IllegalArgumentException("SPEI transfer amount must be positive: " + amount);
    }
    amount = amount.setScale(2, RoundingMode.HALF_UP);
  }

  public static Money of(BigDecimal amount) {
    return new Money(amount);
  }

  public static Money of(String amount) {
    return new Money(new BigDecimal(amount));
  }

  public static Money pesos(long pesos) {
    return new Money(BigDecimal.valueOf(pesos));
  }

  public boolean isGreaterThanOrEqual(Money other) {
    return amount.compareTo(other.amount) >= 0;
  }

  public boolean isLessThan(Money other) {
    return amount.compareTo(other.amount) < 0;
  }

  public Money plus(Money other) {
    return new Money(amount.add(other.amount));
  }

  @Override
  public int compareTo(Money other) {
    return amount.compareTo(other.amount);
  }

  @Override
  public String toString() {
    return "$%,.2f %s".formatted(amount, CURRENCY);
  }
}
