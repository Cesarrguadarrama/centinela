package mx.centinela.domain.port.in;

import java.util.List;
import java.util.UUID;
import mx.centinela.domain.rules.RuleDefinition;

/** Operations side of the rule engine: what fires, how hard, with which parameters. */
public interface ManageRulesUseCase {

  List<RuleDefinition> all();

  RuleDefinition get(UUID id);

  RuleDefinition create(RuleDefinition draft);

  RuleDefinition update(RuleDefinition definition);
}
