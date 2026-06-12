package mx.centinela.domain.port.out;

import java.util.List;
import mx.centinela.domain.rules.RuleDefinition;

/** Source of the rule configuration operations maintains. */
public interface RuleRepository {

  List<RuleDefinition> findEnabled();
}
