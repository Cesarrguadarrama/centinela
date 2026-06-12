package mx.centinela.bootstrap.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mx.centinela.bootstrap.api.dto.RuleRequest;
import mx.centinela.bootstrap.api.dto.RuleResponse;
import mx.centinela.domain.port.in.ManageRulesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rule administration. There is intentionally no DELETE: a rule that has raised alerts is part of
 * the audit trail — disable it instead.
 */
@RestController
@RequestMapping("/api/rules")
class RuleController {

  private final ManageRulesUseCase rules;

  RuleController(ManageRulesUseCase rules) {
    this.rules = rules;
  }

  @GetMapping
  List<RuleResponse> all() {
    return rules.all().stream().map(RuleResponse::from).toList();
  }

  @GetMapping("/{id}")
  RuleResponse byId(@PathVariable UUID id) {
    return RuleResponse.from(rules.get(id));
  }

  @PostMapping
  ResponseEntity<RuleResponse> create(@Valid @RequestBody RuleRequest request) {
    RuleResponse created = RuleResponse.from(rules.create(request.toDomain(UUID.randomUUID())));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  RuleResponse update(@PathVariable UUID id, @Valid @RequestBody RuleRequest request) {
    return RuleResponse.from(rules.update(request.toDomain(id)));
  }
}
