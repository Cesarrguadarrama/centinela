package mx.centinela.generator.traffic;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.model.TransactionId;
import mx.centinela.generator.accounts.AccountPool;
import mx.centinela.generator.accounts.SyntheticAccount;
import org.springframework.stereotype.Component;

/** Builds domain transactions for normal traffic and for attack scenarios. */
@Component
public class TransactionFactory {

  private static final String[] CONCEPTS = {
    "Pago renta", "Nómina", "Transferencia familiar", "Pago servicios", "Abono tarjeta",
    "Pago proveedor", "Colegiatura", "Préstamo personal", "Pago factura", "Ahorro"
  };

  private final AccountPool pool;

  public TransactionFactory(AccountPool pool) {
    this.pool = pool;
  }

  /** A plausible everyday transfer between two random accounts. */
  public Transaction normal() {
    SyntheticAccount[] pair = pool.randomPair();
    return build(pair[0], pair[1], sampleAmount(pair[0].typicalAmount()), Instant.now());
  }

  public Transaction between(
      SyntheticAccount source, SyntheticAccount destination, Money amount, Instant timestamp) {
    return build(source, destination, amount, timestamp);
  }

  /**
   * Log-normal sample around the account's typical amount — most transfers cluster near the mean
   * with a realistic long tail, instead of implausible uniform noise.
   */
  public Money sampleAmount(double typicalAmount) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    double sigma = 0.5;
    double sample = typicalAmount * Math.exp(sigma * random.nextGaussian() - (sigma * sigma) / 2);
    double clamped = Math.max(50, Math.min(sample, 900_000));
    return Money.of(BigDecimal.valueOf(Math.round(clamped * 100) / 100.0));
  }

  private Transaction build(
      SyntheticAccount source, SyntheticAccount destination, Money amount, Instant timestamp) {
    String concept = CONCEPTS[ThreadLocalRandom.current().nextInt(CONCEPTS.length)];
    return new Transaction(
        TransactionId.newId(), source.clabe(), destination.clabe(), amount, concept, timestamp);
  }
}
