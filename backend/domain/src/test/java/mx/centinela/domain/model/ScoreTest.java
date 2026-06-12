package mx.centinela.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.UUID;
import mx.centinela.domain.rules.RuleMatch;
import org.junit.jupiter.api.Test;

class ScoreTest {

  @Test
  void sumsWeightsOfMatches() {
    Score score = Score.fromMatches(List.of(match(40), match(35)));

    assertThat(score.value()).isEqualTo(75);
  }

  @Test
  void capsAtOneHundred() {
    Score score = Score.fromMatches(List.of(match(60), match(45), match(40)));

    assertThat(score.value()).isEqualTo(100);
  }

  @Test
  void noMatchesMeansZeroRisk() {
    assertThat(Score.fromMatches(List.of())).isEqualTo(Score.ZERO);
  }

  @Test
  void rejectsOutOfRangeValues() {
    assertThatIllegalArgumentException().isThrownBy(() -> new Score(101));
    assertThatIllegalArgumentException().isThrownBy(() -> new Score(-1));
  }

  private RuleMatch match(int weight) {
    return new RuleMatch(UUID.randomUUID(), "regla", Severity.HIGH, weight, "explicación");
  }
}
