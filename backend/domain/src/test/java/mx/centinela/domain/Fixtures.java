package mx.centinela.domain;

import java.time.Instant;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;

/** Shared test data builders. */
public final class Fixtures {

  public static final Clabe SOURCE = Clabe.fromBaseDigits("01218000123456789");
  public static final Clabe DESTINATION = Clabe.fromBaseDigits("00201077777777771");

  private Fixtures() {}

  public static Transaction transactionOf(String amount) {
    return transactionOf(amount, Instant.parse("2026-06-12T18:30:00Z"));
  }

  public static Transaction transactionOf(String amount, Instant timestamp) {
    return new Transaction(
        TransactionId.newId(), SOURCE, DESTINATION, Money.of(amount), "Pago factura", timestamp);
  }
}
