package mx.centinela.domain.rules;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import mx.centinela.domain.model.Transaction;

/**
 * Flags transfers in hours where legitimate traffic is rare. Evaluates the transaction's event time
 * in the configured timezone.
 *
 * <p>Params: {@code startHour} (default 0), {@code endHour} (default 5, exclusive), {@code
 * timezone} (default America/Mexico_City). Windows may wrap midnight, e.g. 22–6.
 */
public final class OffHoursRule implements FraudRule {

  private final RuleDefinition definition;
  private final int startHour;
  private final int endHour;
  private final ZoneId zone;

  public OffHoursRule(RuleDefinition definition) {
    this.definition = definition;
    this.startHour = definition.intParam("startHour", 0);
    this.endHour = definition.intParam("endHour", 5);
    this.zone = ZoneId.of(definition.stringParam("timezone", "America/Mexico_City"));
  }

  @Override
  public RuleDefinition definition() {
    return definition;
  }

  @Override
  public Optional<RuleMatch> evaluate(Transaction tx) {
    ZonedDateTime local = tx.timestamp().atZone(zone);
    int hour = local.getHour();
    boolean inWindow =
        startHour <= endHour
            ? hour >= startHour && hour < endHour
            : hour >= startHour || hour < endHour;
    if (!inWindow) {
      return Optional.empty();
    }
    String explanation =
        ("Transferencia realizada a las %02d:%02d (%s), dentro de la ventana atípica "
                + "%02d:00–%02d:00. La actividad legítima en este horario es poco frecuente.")
            .formatted(local.getHour(), local.getMinute(), zone.getId(), startHour, endHour);
    return Optional.of(RuleMatch.of(definition, explanation));
  }
}
