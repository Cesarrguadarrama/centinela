package mx.centinela.bootstrap.infrastructure.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.ScoredTransaction;
import mx.centinela.domain.port.out.AlertRepository;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.port.out.TransactionRepository;
import mx.centinela.domain.rules.RuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Postgres-backed implementations of the domain's outbound ports. */
@Component
class PersistenceAdapters implements TransactionRepository, AlertRepository, RuleRepository {

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
  public void save(ScoredTransaction scored) {
    transactions.save(TransactionEntity.from(scored));
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
  public Optional<Alert> findById(AlertId id) {
    return alerts.findById(id.value()).map(AlertEntity::toDomain);
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
  public List<RuleDefinition> findAll() {
    return rules.findAll().stream().map(RuleEntity::toDomain).toList();
  }

  @Override
  public Optional<RuleDefinition> findById(UUID id) {
    return rules.findById(id).map(RuleEntity::toDomain);
  }

  @Override
  public void save(RuleDefinition definition) {
    rules.save(RuleEntity.from(definition));
    cachedRules = null; // rule edits must reach the engine immediately, not after the TTL
  }

  private record CachedRules(List<RuleDefinition> definitions, Instant loadedAt) {}
}
