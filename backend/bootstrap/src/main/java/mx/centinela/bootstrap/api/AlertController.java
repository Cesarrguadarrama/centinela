package mx.centinela.bootstrap.api;

import mx.centinela.bootstrap.api.dto.AlertView;
import mx.centinela.bootstrap.api.dto.PageView;
import mx.centinela.bootstrap.infrastructure.queries.AlertQueryService;
import mx.centinela.domain.model.AlertId;
import mx.centinela.domain.model.AlertStatus;
import mx.centinela.domain.port.in.TriageAlertUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
class AlertController {

  private final AlertQueryService queries;
  private final TriageAlertUseCase triage;

  AlertController(AlertQueryService queries, TriageAlertUseCase triage) {
    this.queries = queries;
    this.triage = triage;
  }

  @GetMapping
  PageView<AlertView> search(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String severity,
      @RequestParam(required = false) String clabe,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return queries.search(status, severity, clabe, page, Math.min(size, 200));
  }

  @GetMapping("/{id}")
  AlertView byId(@PathVariable String id) {
    return queries.byId(id);
  }

  @PostMapping("/{id}/review")
  AlertView review(@PathVariable String id) {
    triage.triage(AlertId.of(id), AlertStatus.REVIEWED);
    return queries.byId(id);
  }

  @PostMapping("/{id}/false-positive")
  AlertView falsePositive(@PathVariable String id) {
    triage.triage(AlertId.of(id), AlertStatus.FALSE_POSITIVE);
    return queries.byId(id);
  }
}
