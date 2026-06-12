package mx.centinela.domain.model;

import java.util.Objects;

/**
 * A CLABE (Clave Bancaria Estandarizada), the 18-digit standard for Mexican bank accounts used in
 * SPEI transfers: 3 digits bank code, 3 digits branch/plaza, 11 digits account number and 1 check
 * digit (weighted mod-10 over the first 17 digits, weights 3-7-1 repeating).
 */
public record Clabe(String value) {

  private static final int LENGTH = 18;
  private static final int[] WEIGHTS = {3, 7, 1};

  public Clabe {
    Objects.requireNonNull(value, "CLABE must not be null");
    if (!value.matches("\\d{18}")) {
      throw new IllegalArgumentException("CLABE must be exactly 18 digits: " + value);
    }
    int expected = checkDigit(value.substring(0, LENGTH - 1));
    int actual = Character.getNumericValue(value.charAt(LENGTH - 1));
    if (expected != actual) {
      throw new IllegalArgumentException(
          "Invalid CLABE check digit for %s: expected %d but was %d"
              .formatted(value, expected, actual));
    }
  }

  public static Clabe of(String value) {
    return new Clabe(value);
  }

  /** Builds a valid CLABE by appending the computed check digit to 17 base digits. */
  public static Clabe fromBaseDigits(String seventeenDigits) {
    Objects.requireNonNull(seventeenDigits, "base digits must not be null");
    if (!seventeenDigits.matches("\\d{17}")) {
      throw new IllegalArgumentException("CLABE base must be exactly 17 digits");
    }
    return new Clabe(seventeenDigits + checkDigit(seventeenDigits));
  }

  /** Weighted mod-10 algorithm defined by ABM/Banxico for CLABE validation. */
  static int checkDigit(String seventeenDigits) {
    int sum = 0;
    for (int i = 0; i < seventeenDigits.length(); i++) {
      int digit = Character.getNumericValue(seventeenDigits.charAt(i));
      sum += (digit * WEIGHTS[i % WEIGHTS.length]) % 10;
    }
    return (10 - (sum % 10)) % 10;
  }

  public String bankCode() {
    return value.substring(0, 3);
  }

  public String plazaCode() {
    return value.substring(3, 6);
  }

  public String accountNumber() {
    return value.substring(6, 17);
  }

  /** Masked form for logs and analyst-facing messages, e.g. {@code 012***********4567**}. */
  public String masked() {
    return bankCode() + "***********" + value.substring(13);
  }
}
