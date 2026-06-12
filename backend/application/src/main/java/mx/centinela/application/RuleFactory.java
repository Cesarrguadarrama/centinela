package mx.centinela.application;

import java.util.List;
import mx.centinela.domain.port.out.ActivityWindowPort;
import mx.centinela.domain.rules.FraudRule;
import mx.centinela.domain.rules.MuleAccountRule;
import mx.centinela.domain.rules.OffHoursRule;
import mx.centinela.domain.rules.RuleDefinition;
import mx.centinela.domain.rules.SmurfingRule;
import mx.centinela.domain.rules.SubThresholdAmountRule;
import mx.centinela.domain.rules.VelocityRule;

/**
 * Instantiates executable rules from their stored definitions, wiring in whatever ports each rule
 * type needs. Adding a fraud pattern means a new domain rule class plus one case here.
 */
public class RuleFactory {

  private final ActivityWindowPort activityWindows;

  public RuleFactory(ActivityWindowPort activityWindows) {
    this.activityWindows = activityWindows;
  }

  public List<FraudRule> from(List<RuleDefinition> definitions) {
    return definitions.stream().filter(RuleDefinition::enabled).map(this::instantiate).toList();
  }

  private FraudRule instantiate(RuleDefinition definition) {
    return switch (definition.type()) {
      case SUB_THRESHOLD_AMOUNT -> new SubThresholdAmountRule(definition);
      case VELOCITY -> new VelocityRule(definition, activityWindows);
      case OFF_HOURS -> new OffHoursRule(definition);
      case MULE_ACCOUNT -> new MuleAccountRule(definition, activityWindows);
      case SMURFING -> new SmurfingRule(definition, activityWindows);
    };
  }
}
