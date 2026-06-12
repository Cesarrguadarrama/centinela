package mx.centinela.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mx.centinela.domain.rules.RuleDefinition;

/** Source of the rule configuration operations maintains. */
public interface RuleRepository {

  /** The rules the engine evaluates — hot path, may be served from a short-lived cache. */
  List<RuleDefinition> findEnabled();

  List<RuleDefinition> findAll();

  Optional<RuleDefinition> findById(UUID id);

  void save(RuleDefinition definition);
}
