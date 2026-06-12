package mx.centinela.bootstrap.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {}

interface RuleJpaRepository extends JpaRepository<RuleEntity, UUID> {
  List<RuleEntity> findByEnabledTrue();
}

interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID> {}
