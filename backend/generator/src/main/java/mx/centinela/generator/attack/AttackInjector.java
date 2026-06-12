package mx.centinela.generator.attack;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import mx.centinela.domain.model.Money;
import mx.centinela.generator.accounts.AccountPool;
import mx.centinela.generator.accounts.SyntheticAccount;
import mx.centinela.generator.publish.TransactionPublisher;
import mx.centinela.generator.traffic.TransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Emits labeled fraud bursts mixed into the normal stream. Each scenario logs a banner with the
 * involved accounts so a live demo can show cause (this burst) and effect (the alerts it raises).
 */
@Service
public class AttackInjector {

  private static final Logger log = LoggerFactory.getLogger(AttackInjector.class);
  private static final ZoneId MEXICO_CITY = ZoneId.of("America/Mexico_City");

  private final AccountPool pool;
  private final TransactionFactory factory;
  private final TransactionPublisher publisher;

  public AttackInjector(
      AccountPool pool, TransactionFactory factory, TransactionPublisher publisher) {
    this.pool = pool;
    this.factory = factory;
    this.publisher = publisher;
  }

  @Async
  public void inject(AttackScenario scenario) {
    log.warn(">>> ATTACK INJECTION START: {}", scenario);
    try {
      switch (scenario) {
        case MULE -> mule();
        case SMURFING -> smurfing();
        case VELOCITY -> velocity();
        case OFF_HOURS -> offHours();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Attack injection interrupted: {}", scenario);
      return;
    }
    log.warn("<<< ATTACK INJECTION DONE: {}", scenario);
  }

  /** 12 incoming transfers converge on one account, then ~90% is dispersed in 4 payouts. */
  private void mule() throws InterruptedException {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    SyntheticAccount mule = pool.random();
    List<SyntheticAccount> sources = pool.randomDistinct(12, mule);
    log.warn("mule account: {} ({})", mule.clabe().masked(), mule.holderName());

    double collected = 0;
    for (SyntheticAccount source : sources) {
      Money amount = Money.of(BigDecimal.valueOf(random.nextDouble(5_000, 45_000)));
      collected += amount.amount().doubleValue();
      publisher.publish(factory.between(source, mule, amount, Instant.now()));
      Thread.sleep(random.nextLong(100, 300));
    }

    // Rapid dispersal — the signature that distinguishes a mule from a normal collector
    List<SyntheticAccount> payees = pool.randomDistinct(4, mule);
    double dispersal = collected * 0.9 / payees.size();
    for (SyntheticAccount payee : payees) {
      publisher.publish(
          factory.between(mule, payee, Money.of(BigDecimal.valueOf(dispersal)), Instant.now()));
      Thread.sleep(random.nextLong(100, 200));
    }
  }

  /** 20 transfers of $2,000–$9,900 between the same pair — classic monto hormiga. */
  private void smurfing() throws InterruptedException {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    SyntheticAccount[] pair = pool.randomPair();
    log.warn("smurfing: {} -> {}", pair[0].clabe().masked(), pair[1].clabe().masked());

    for (int i = 0; i < 20; i++) {
      Money amount = Money.of(BigDecimal.valueOf(random.nextDouble(2_000, 9_900)));
      publisher.publish(factory.between(pair[0], pair[1], amount, Instant.now()));
      Thread.sleep(random.nextLong(150, 400));
    }
  }

  /** 25 transfers from one account to random destinations in under ~30 seconds. */
  private void velocity() throws InterruptedException {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    SyntheticAccount source = pool.random();
    log.warn("velocity burst from: {}", source.clabe().masked());

    for (int i = 0; i < 25; i++) {
      SyntheticAccount destination = pool.randomDistinct(1, source).get(0);
      Money amount = factory.sampleAmount(source.typicalAmount());
      publisher.publish(factory.between(source, destination, amount, Instant.now()));
      Thread.sleep(random.nextLong(50, 150));
    }
  }

  /** 5 plausible-looking transfers stamped at ~03:00 Mexico City time. */
  private void offHours() throws InterruptedException {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Instant smallHours =
        LocalDate.now(MEXICO_CITY)
            .atTime(LocalTime.of(3, random.nextInt(60)))
            .atZone(MEXICO_CITY)
            .toInstant();

    for (int i = 0; i < 5; i++) {
      SyntheticAccount[] pair = pool.randomPair();
      Money amount = factory.sampleAmount(pair[0].typicalAmount());
      publisher.publish(factory.between(pair[0], pair[1], amount, smallHours.plusSeconds(i * 40L)));
      Thread.sleep(random.nextLong(100, 250));
    }
  }
}
