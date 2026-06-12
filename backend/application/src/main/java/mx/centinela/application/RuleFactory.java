package mx.centinela.application;

import java.util.List;
import mx.centinela.domain.port.out.AccountActivityPort;
import mx.centinela.domain.rules.FraudRule;
import mx.centinela.domain.rules.OffHoursRule;
import mx.centinela.domain.rules.RuleDefinition;
import mx.centinela.domain.rules.SubThresholdAmountRule;
import mx.centinela.domain.rules.VelocityRule;

/**
 * Instantiates executable rules from their stored definitions, wiring in whatever ports each rule
 * type needs. Adding a fraud pattern means a new domain rule class plus one case here.
 */
public class RuleFactory {

  private final AccountActivityPort accountActivity;

  public RuleFactory(AccountActivityPort accountActivity) {
    this.accountActivity = accountActivity;
  }

  public List<FraudRule> from(List<RuleDefinition> definitions) {
    return definitions.stream().filter(RuleDefinition::enabled).map(this::instantiate).toList();
  }

  private FraudRule instantiate(RuleDefinition definition) {
    return switch (definition.type()) {
      case SUB_THRESHOLD_AMOUNT -> new SubThresholdAmountRule(definition);
      case VELOCITY -> new VelocityRule(definition, accountActivity);
      case OFF_HOURS -> new OffHoursRule(definition);
    };
  }
}
