package mx.centinela.bootstrap.infrastructure.queries;

import java.util.ArrayList;
import java.util.List;
import mx.centinela.bootstrap.api.dto.AlertView;
import mx.centinela.bootstrap.api.dto.PageView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Read side of alerts (CQRS-lite, see ADR-0007): joins straight to SQL projections the dashboard
 * can render, without round-tripping through domain objects that add nothing to a listing.
 */
@Service
public class AlertQueryService {

  private static final RowMapper<AlertView> MAPPER =
      (rs, rowNum) ->
          new AlertView(
              rs.getString("id"),
              rs.getString("transaction_id"),
              rs.getString("rule_name"),
              rs.getString("severity"),
              rs.getString("explanation"),
              rs.getString("status"),
              rs.getTimestamp("created_at").toInstant(),
              rs.getString("source_clabe"),
              rs.getString("destination_clabe"),
              rs.getBigDecimal("amount"),
              rs.getInt("score"));

  private static final String BASE_SELECT =
      """
      SELECT a.id, a.transaction_id, a.rule_name, a.severity, a.explanation, a.status,
             a.created_at, t.source_clabe, t.destination_clabe, t.amount, t.score
      FROM alerts a JOIN transactions t ON t.id = a.transaction_id
      """;

  private final JdbcTemplate jdbc;

  public AlertQueryService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public PageView<AlertView> search(
      String status, String severity, String clabe, int page, int size) {
    StringBuilder where = new StringBuilder(" WHERE 1=1");
    List<Object> args = new ArrayList<>();
    if (status != null && !status.isBlank()) {
      where.append(" AND a.status = ?");
      args.add(status.toUpperCase());
    }
    if (severity != null && !severity.isBlank()) {
      where.append(" AND a.severity = ?");
      args.add(severity.toUpperCase());
    }
    if (clabe != null && !clabe.isBlank()) {
      where.append(" AND (t.source_clabe = ? OR t.destination_clabe = ?)");
      args.add(clabe);
      args.add(clabe);
    }

    Long total =
        jdbc.queryForObject(
            "SELECT count(*) FROM alerts a JOIN transactions t ON t.id = a.transaction_id" + where,
            Long.class,
            args.toArray());

    args.add(size);
    args.add(page * size);
    List<AlertView> items =
        jdbc.query(
            BASE_SELECT + where + " ORDER BY a.created_at DESC LIMIT ? OFFSET ?",
            MAPPER,
            args.toArray());
    return new PageView<>(items, page, size, total == null ? 0 : total);
  }

  public AlertView byId(String alertId) {
    List<AlertView> found = jdbc.query(BASE_SELECT + " WHERE a.id = ?::uuid", MAPPER, alertId);
    if (found.isEmpty()) {
      throw new java.util.NoSuchElementException("alert not found: " + alertId);
    }
    return found.get(0);
  }
}
