package mx.centinela.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void normalizesToCentPrecision() {
    assertThat(Money.of("49999.999").amount()).isEqualByComparingTo("50000.00");
    assertThat(Money.of("100.1").amount().scale()).isEqualTo(2);
  }

  @Test
  void rejectsZeroAndNegativeAmounts() {
    assertThatIllegalArgumentException().isThrownBy(() -> Money.of(BigDecimal.ZERO));
    assertThatIllegalArgumentException().isThrownBy(() -> Money.of("-1"));
  }

  @Test
  void comparesAmounts() {
    Money reportingThreshold = Money.pesos(50_000);
    Money justBelow = Money.of("49999.00");

    assertThat(justBelow.isLessThan(reportingThreshold)).isTrue();
    assertThat(reportingThreshold.isGreaterThanOrEqual(justBelow)).isTrue();
    assertThat(justBelow.plus(Money.of("1000")).isGreaterThanOrEqual(reportingThreshold)).isTrue();
  }

  @Test
  void equalityIsByNormalizedValue() {
    assertThat(Money.of("100")).isEqualTo(Money.of("100.00"));
  }
}
