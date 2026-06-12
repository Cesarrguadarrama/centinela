package mx.centinela.generator.accounts;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import mx.centinela.domain.model.Clabe;
import mx.centinela.generator.config.GeneratorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Builds and serves the pool of fictional accounts with structurally valid CLABEs. */
@Component
public class AccountPool {

  private static final Logger log = LoggerFactory.getLogger(AccountPool.class);

  // Real institution codes so generated CLABEs look plausible (accounts are fictional)
  private static final String[] BANK_CODES = {
    "002", "012", "014", "021", "030", "036", "044", "058", "072", "137", "646"
  };
  private static final String[] PLAZA_CODES = {"180", "010", "320", "580", "914"};

  private static final String[] FIRST_NAMES = {
    "María", "José", "Guadalupe", "Juan", "Verónica", "Luis", "Fernanda", "Carlos",
    "Ana", "Miguel", "Sofía", "Jorge", "Daniela", "Ricardo", "Alejandra", "Pedro"
  };
  private static final String[] LAST_NAMES = {
    "Hernández", "García", "Martínez", "López", "González", "Pérez", "Rodríguez",
    "Sánchez", "Ramírez", "Cruz", "Flores", "Gómez", "Torres", "Vázquez"
  };

  private final GeneratorProperties properties;
  private final List<SyntheticAccount> accounts = new ArrayList<>();

  public AccountPool(GeneratorProperties properties) {
    this.properties = properties;
  }

  @PostConstruct
  void populate() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < properties.accounts(); i++) {
      accounts.add(newAccount(random));
    }
    log.info("Synthetic account pool ready: {} accounts", accounts.size());
  }

  private SyntheticAccount newAccount(ThreadLocalRandom random) {
    String bank = BANK_CODES[random.nextInt(BANK_CODES.length)];
    String plaza = PLAZA_CODES[random.nextInt(PLAZA_CODES.length)];
    String account = "%011d".formatted(random.nextLong(0, 100_000_000_000L));
    Clabe clabe = Clabe.fromBaseDigits(bank + plaza + account);

    String holder =
        FIRST_NAMES[random.nextInt(FIRST_NAMES.length)]
            + " "
            + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    // Typical per-transfer amount between $500 and ~$25,000, skewed towards small amounts
    double typicalAmount = 500 + Math.pow(random.nextDouble(), 2) * 24_500;
    return new SyntheticAccount(clabe, holder, typicalAmount);
  }

  public SyntheticAccount random() {
    return accounts.get(ThreadLocalRandom.current().nextInt(accounts.size()));
  }

  /** Two distinct accounts: a source and a destination. */
  public SyntheticAccount[] randomPair() {
    SyntheticAccount source = random();
    SyntheticAccount destination = random();
    while (destination.clabe().equals(source.clabe())) {
      destination = random();
    }
    return new SyntheticAccount[] {source, destination};
  }

  public List<SyntheticAccount> randomDistinct(int count, SyntheticAccount excluded) {
    List<SyntheticAccount> result = new ArrayList<>();
    while (result.size() < count) {
      SyntheticAccount candidate = random();
      if (!candidate.clabe().equals(excluded.clabe())
          && result.stream().noneMatch(a -> a.clabe().equals(candidate.clabe()))) {
        result.add(candidate);
      }
    }
    return result;
  }
}
