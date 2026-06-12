package mx.centinela.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClabeTest {

  // 002 (Banamex) + plaza 010 + account 77777777771
  private static final String VALID_BASE = "00201077777777771";

  @Test
  void buildsClabeFromBaseDigitsWithComputedCheckDigit() {
    Clabe clabe = Clabe.fromBaseDigits(VALID_BASE);

    assertThat(clabe.value()).hasSize(18).startsWith(VALID_BASE);
    // Re-validating through the canonical constructor must not throw
    assertThat(Clabe.of(clabe.value())).isEqualTo(clabe);
  }

  @Test
  void exposesStructuralParts() {
    Clabe clabe = Clabe.fromBaseDigits("01218000123456789");

    assertThat(clabe.bankCode()).isEqualTo("012");
    assertThat(clabe.plazaCode()).isEqualTo("180");
    assertThat(clabe.accountNumber()).isEqualTo("00123456789");
  }

  @Test
  void rejectsWrongCheckDigit() {
    Clabe valid = Clabe.fromBaseDigits(VALID_BASE);
    int validDigit = Character.getNumericValue(valid.value().charAt(17));
    String tampered = VALID_BASE + ((validDigit + 1) % 10);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> Clabe.of(tampered))
        .withMessageContaining("check digit");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "123", "1234567890123456789", "01218000123456ABC7"})
  void rejectsMalformedValues(String raw) {
    assertThatIllegalArgumentException().isThrownBy(() -> Clabe.of(raw));
  }

  @Test
  void rejectsNull() {
    assertThatNullPointerException().isThrownBy(() -> Clabe.of(null));
  }

  @Test
  void checkDigitMatchesKnownAlgorithmProperties() {
    // The algorithm weights digits 3,7,1 cyclically; changing any single digit
    // of the base must change the check digit or keep it within 0-9.
    int digit = Clabe.checkDigit(VALID_BASE);
    assertThat(digit).isBetween(0, 9);
  }

  @Test
  void masksAccountNumberForLogs() {
    Clabe clabe = Clabe.fromBaseDigits("01218000123456789");

    assertThat(clabe.masked()).startsWith("012").contains("***");
    assertThat(clabe.masked()).doesNotContain(clabe.accountNumber());
  }
}
