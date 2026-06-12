package mx.centinela.bootstrap.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import mx.centinela.domain.model.Severity;
import mx.centinela.domain.rules.RuleDefinition;
import mx.centinela.domain.rules.RuleType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rules")
class RuleEntity {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(length = 40, nullable = false)
  private RuleType type;

  @Column(length = 120, nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private boolean enabled;

  @Enumerated(EnumType.STRING)
  @Column(length = 16, nullable = false)
  private Severity severity;

  @Column(nullable = false)
  private int weight;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private Map<String, Object> params;

  protected RuleEntity() {}

  static RuleEntity from(RuleDefinition definition) {
    RuleEntity entity = new RuleEntity();
    entity.id = definition.id();
    entity.type = definition.type();
    entity.name = definition.name();
    entity.description = definition.description();
    entity.enabled = definition.enabled();
    entity.severity = definition.severity();
    entity.weight = definition.weight();
    entity.params = definition.params();
    return entity;
  }

  RuleDefinition toDomain() {
    return new RuleDefinition(id, type, name, description, enabled, severity, weight, params);
  }
}
