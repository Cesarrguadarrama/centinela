package mx.centinela.bootstrap.infrastructure.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.AccountActivityPort;
import mx.centinela.domain.port.out.AlertRepository;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.port.out.TransactionRepository;
import mx.centinela.domain.rules.RuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Postgres-backed implementations of the domain's outbound ports. */
@Component
class PersistenceAdapters
    implements TransactionRepository, AlertRepository, RuleRepository, AccountActivityPort {

  private static final Logger log = LoggerFactory.getLogger(PersistenceAdapters.class);

  /**
   * Rules change rarely but are read on every transaction — a short TTL cache keeps rule edits
   * effective within seconds without one extra query per event.
   */
  private static final Duration RULES_CACHE_TTL = Duration.ofSeconds(15);

  private final TransactionJpaRepository transactions;
  private final AlertJpaRepository alerts;
  private final RuleJpaRepository rules;

  private volatile CachedRules cachedRules;

  PersistenceAdapters(
      TransactionJpaRepository transactions, AlertJpaRepository alerts, RuleJpaRepository rules) {
    this.transactions = transactions;
    this.alerts = alerts;
    this.rules = rules;
  }

  @Override
  public void save(Transaction transaction) {
    transactions.save(TransactionEntity.from(transaction));
  }

  @Override
  public void save(Alert alert) {
    alerts.save(AlertEntity.from(alert));
    log.info(
        "ALERT [{}] {} — tx {} — {}",
        alert.severity(),
        alert.ruleName(),
        alert.transactionId(),
        alert.explanation());
  }

  @Override
  public List<RuleDefinition> findEnabled() {
    CachedRules cached = cachedRules;
    if (cached != null && cached.loadedAt().plus(RULES_CACHE_TTL).isAfter(Instant.now())) {
      return cached.definitions();
    }
    List<RuleDefinition> fresh =
        rules.findByEnabledTrue().stream().map(RuleEntity::toDomain).toList();
    cachedRules = new CachedRules(fresh, Instant.now());
    return fresh;
  }

  @Override
  public long countTransfersFrom(Clabe source, Instant since) {
    return transactions.countBySourceClabeAndTimestampAfter(source.value(), since);
  }

  private record CachedRules(List<RuleDefinition> definitions, Instant loadedAt) {}
}
