package mx.centinela.domain.rules;

import static mx.centinela.domain.Fixtures.transactionOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.model.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OffHoursRuleTest {

  private RuleDefinition definition(Map<String, Object> params) {
    return new RuleDefinition(
        UUID.randomUUID(),
        RuleType.OFF_HOURS,
        "Horario atípico",
        "",
        true,
        Severity.MEDIUM,
        15,
        params);
  }

  @ParameterizedTest
  @CsvSource({
    // Mexico City is UTC-6 — 09:00Z = 03:00 local (inside 0-5), 18:00Z = 12:00 local
    "2026-06-12T09:00:00Z, true",
    "2026-06-12T05:59:00Z, false", // 23:59 local, one minute before the window opens
    "2026-06-12T06:00:00Z, true", // 00:00 local, start is inclusive
    "2026-06-12T18:00:00Z, false",
    "2026-06-12T11:00:00Z, false" // 05:00 local, end is exclusive
  })
  void evaluatesInConfiguredTimezone(String utcInstant, boolean expected) {
    OffHoursRule rule =
        new OffHoursRule(
            definition(Map.of("startHour", 0, "endHour", 5, "timezone", "America/Mexico_City")));

    assertThat(rule.evaluate(transactionOf("1000", Instant.parse(utcInstant))).isPresent())
        .isEqualTo(expected);
  }

  @Test
  void supportsWindowsWrappingMidnight() {
    OffHoursRule rule =
        new OffHoursRule(definition(Map.of("startHour", 22, "endHour", 6, "timezone", "UTC")));

    assertThat(rule.evaluate(transactionOf("1000", Instant.parse("2026-06-12T23:30:00Z"))))
        .isPresent();
    assertThat(rule.evaluate(transactionOf("1000", Instant.parse("2026-06-12T05:30:00Z"))))
        .isPresent();
    assertThat(rule.evaluate(transactionOf("1000", Instant.parse("2026-06-12T12:00:00Z"))))
        .isEmpty();
  }

  @Test
  void explanationShowsLocalTimeAndWindow() {
    OffHoursRule rule =
        new OffHoursRule(
            definition(Map.of("startHour", 0, "endHour", 5, "timezone", "America/Mexico_City")));

    var match =
        rule.evaluate(transactionOf("1000", Instant.parse("2026-06-12T09:14:00Z"))).orElseThrow();

    assertThat(match.explanation()).contains("03:14").contains("00:00–05:00");
  }
}
