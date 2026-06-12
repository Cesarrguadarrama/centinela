package mx.centinela.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private static final Clabe SOURCE = Clabe.fromBaseDigits("01218000123456789");
  private static final Clabe DESTINATION = Clabe.fromBaseDigits("00201077777777771");

  @Test
  void rejectsTransferToSameAccount() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new Transaction(
                    TransactionId.newId(),
                    SOURCE,
                    SOURCE,
                    Money.pesos(100),
                    "self transfer",
                    Instant.now()))
        .withMessageContaining("must differ");
  }

  @Test
  void normalizesNullConceptToEmpty() {
    Transaction tx =
        new Transaction(
            TransactionId.newId(), SOURCE, DESTINATION, Money.pesos(100), null, Instant.now());

    assertThat(tx.concept()).isEmpty();
  }
}
