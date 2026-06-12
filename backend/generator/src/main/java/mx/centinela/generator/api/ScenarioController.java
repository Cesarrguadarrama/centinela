package mx.centinela.generator.api;

import java.util.Map;
import mx.centinela.generator.attack.AttackInjector;
import mx.centinela.generator.attack.AttackScenario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Triggers attack scenario injections. Thin adapter — all behavior lives in AttackInjector. */
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

  private final AttackInjector injector;

  public ScenarioController(AttackInjector injector) {
    this.injector = injector;
  }

  @PostMapping("/{scenario}")
  public ResponseEntity<Map<String, String>> inject(@PathVariable String scenario) {
    AttackScenario parsed;
    try {
      parsed = AttackScenario.valueOf(scenario.toUpperCase());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "unknown scenario: " + scenario, "valid", validScenarios()));
    }
    injector.inject(parsed);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(Map.of("scenario", parsed.name(), "status", "injecting"));
  }

  private String validScenarios() {
    StringBuilder sb = new StringBuilder();
    for (AttackScenario s : AttackScenario.values()) {
      if (!sb.isEmpty()) {
        sb.append(", ");
      }
      sb.append(s.name().toLowerCase());
    }
    return sb.toString();
  }
}
