package mx.centinela.bootstrap.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
  long countBySourceClabeAndTimestampAfter(String sourceClabe, Instant since);
}

interface RuleJpaRepository extends JpaRepository<RuleEntity, UUID> {
  List<RuleEntity> findByEnabledTrue();
}

interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID> {}
