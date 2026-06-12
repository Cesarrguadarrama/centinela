package mx.centinela.bootstrap.infrastructure.queries;

import java.util.List;
import mx.centinela.bootstrap.api.dto.AccountSummary;
import mx.centinela.bootstrap.api.dto.PageView;
import mx.centinela.bootstrap.api.dto.TransactionView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/** Account drill-down: what has this CLABE been doing and how risky does it look. */
@Service
public class AccountQueryService {

  private static final RowMapper<TransactionView> TX_MAPPER =
      (rs, i) ->
          new TransactionView(
              rs.getString("id"),
              rs.getString("source_clabe"),
              rs.getString("destination_clabe"),
              rs.getBigDecimal("amount"),
              rs.getString("concept"),
              rs.getTimestamp("ts").toInstant(),
              rs.getInt("score"));

  private final JdbcTemplate jdbc;

  public AccountQueryService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public AccountSummary summary(String clabe, int hours) {
    return jdbc.queryForObject(
        """
        SELECT
          coalesce(sum(CASE WHEN source_clabe = ? THEN 1 ELSE 0 END), 0)           AS sent_count,
          coalesce(sum(CASE WHEN source_clabe = ? THEN amount END), 0)             AS sent_total,
          coalesce(sum(CASE WHEN destination_clabe = ? THEN 1 ELSE 0 END), 0)      AS received_count,
          coalesce(sum(CASE WHEN destination_clabe = ? THEN amount END), 0)        AS received_total,
          coalesce(max(score), 0)                                                  AS max_score,
          (SELECT count(*) FROM alerts a JOIN transactions t2 ON t2.id = a.transaction_id
           WHERE (t2.source_clabe = ? OR t2.destination_clabe = ?)
             AND a.created_at > now() - make_interval(hours => ?))                 AS alert_count
        FROM transactions
        WHERE (source_clabe = ? OR destination_clabe = ?)
          AND ts > now() - make_interval(hours => ?)
        """,
        (rs, i) ->
            new AccountSummary(
                clabe,
                hours,
                rs.getLong("sent_count"),
                rs.getBigDecimal("sent_total"),
                rs.getLong("received_count"),
                rs.getBigDecimal("received_total"),
                rs.getLong("alert_count"),
                rs.getInt("max_score")),
        clabe,
        clabe,
        clabe,
        clabe,
        clabe,
        clabe,
        hours,
        clabe,
        clabe,
        hours);
  }

  public PageView<TransactionView> transactions(String clabe, int page, int size) {
    Long total =
        jdbc.queryForObject(
            "SELECT count(*) FROM transactions WHERE source_clabe = ? OR destination_clabe = ?",
            Long.class,
            clabe,
            clabe);
    List<TransactionView> items =
        jdbc.query(
            """
            SELECT id, source_clabe, destination_clabe, amount, concept, ts, score
            FROM transactions WHERE source_clabe = ? OR destination_clabe = ?
            ORDER BY ts DESC LIMIT ? OFFSET ?
            """,
            TX_MAPPER,
            clabe,
            clabe,
            size,
            page * size);
    return new PageView<>(items, page, size, total == null ? 0 : total);
  }
}
