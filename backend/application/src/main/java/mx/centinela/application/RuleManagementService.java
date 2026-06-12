package mx.centinela.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import mx.centinela.domain.port.in.ManageRulesUseCase;
import mx.centinela.domain.port.out.RuleRepository;
import mx.centinela.domain.rules.RuleDefinition;

/**
 * CRUD over rule definitions. Rules are never deleted — a rule that has raised alerts is part of
 * the audit trail; disabling it stops the engine from evaluating it.
 */
public class RuleManagementService implements ManageRulesUseCase {

  private final RuleRepository rules;

  public RuleManagementService(RuleRepository rules) {
    this.rules = rules;
  }

  @Override
  public List<RuleDefinition> all() {
    return rules.findAll();
  }

  @Override
  public RuleDefinition get(UUID id) {
    return rules
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("rule not found: " + id));
  }

  @Override
  public RuleDefinition create(RuleDefinition draft) {
    rules.save(draft);
    return draft;
  }

  @Override
  public RuleDefinition update(RuleDefinition definition) {
    get(definition.id()); // 404 fast if it does not exist
    rules.save(definition);
    return definition;
  }
}
