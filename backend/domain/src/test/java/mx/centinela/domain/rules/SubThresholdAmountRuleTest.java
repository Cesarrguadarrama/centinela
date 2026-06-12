package mx.centinela.domain.rules;

import static mx.centinela.domain.Fixtures.transactionOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.model.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SubThresholdAmountRuleTest {

  private final RuleDefinition definition =
      new RuleDefinition(
          UUID.randomUUID(),
          RuleType.SUB_THRESHOLD_AMOUNT,
          "Monto bajo umbral de reporte",
          true,
          Severity.HIGH,
          35,
          Map.of("thresholdPesos", 50_000, "marginPesos", 5_000));

  private final SubThresholdAmountRule rule = new SubThresholdAmountRule(definition);

  @ParameterizedTest
  @CsvSource({
    "44999.99, false", // below the suspicious band
    "45000.00, true", // lower bound, inclusive
    "49999.00, true", // the classic structuring amount
    "49999.99, true", // one cent below threshold
    "50000.00, false", // at threshold: reported, not structured
    "50000.01, false"
  })
  void firesOnlyInsideTheSuspiciousBand(String amount, boolean expected) {
    assertThat(rule.evaluate(transactionOf(amount)).isPresent()).isEqualTo(expected);
  }

  @Test
  void explanationIsAnalystReadable() {
    var match = rule.evaluate(transactionOf("49999.00")).orElseThrow();

    assertThat(match.severity()).isEqualTo(Severity.HIGH);
    assertThat(match.explanation())
        .contains("$49,999.00")
        .contains("$50,000.00")
        .contains("estructuración");
  }
}
