package mx.centinela.bootstrap.infrastructure.queries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.centinela.bootstrap.api.dto.MetricsOverview;
import mx.centinela.bootstrap.api.dto.MinuteBucket;
import mx.centinela.bootstrap.api.dto.ScoreBucket;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Aggregations behind the dashboard charts. */
@Service
public class MetricsQueryService {

  private final JdbcTemplate jdbc;

  public MetricsQueryService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public MetricsOverview overview() {
    Long transactions = jdbc.queryForObject("SELECT count(*) FROM transactions", Long.class);
    Long alerts = jdbc.queryForObject("SELECT count(*) FROM alerts", Long.class);
    Double avgScore =
        jdbc.queryForObject("SELECT coalesce(avg(score), 0) FROM transactions", Double.class);

    Map<String, Long> bySeverity = new LinkedHashMap<>();
    jdbc.query(
        "SELECT severity, count(*) AS c FROM alerts GROUP BY severity ORDER BY c DESC",
        rs -> {
          bySeverity.put(rs.getString("severity"), rs.getLong("c"));
        });
    Map<String, Long> byStatus = new LinkedHashMap<>();
    jdbc.query(
        "SELECT status, count(*) AS c FROM alerts GROUP BY status",
        rs -> {
          byStatus.put(rs.getString("status"), rs.getLong("c"));
        });

    return new MetricsOverview(
        transactions == null ? 0 : transactions,
        alerts == null ? 0 : alerts,
        bySeverity,
        byStatus,
        avgScore == null ? 0 : Math.round(avgScore * 100) / 100.0);
  }

  /** Transactions and alerts per minute over the trailing window — the dashboard's pulse line. */
  public List<MinuteBucket> perMinute(int minutes) {
    return jdbc.query(
        """
        SELECT minute,
               coalesce(tx.c, 0)     AS transactions,
               coalesce(alerts.c, 0) AS alerts
        FROM generate_series(
               date_trunc('minute', now()) - make_interval(mins => ? - 1),
               date_trunc('minute', now()),
               interval '1 minute') AS minute
        LEFT JOIN (SELECT date_trunc('minute', created_at) m, count(*) c
                   FROM transactions GROUP BY 1) tx ON tx.m = minute
        LEFT JOIN (SELECT date_trunc('minute', created_at) m, count(*) c
                   FROM alerts GROUP BY 1) alerts ON alerts.m = minute
        ORDER BY minute
        """,
        (rs, i) ->
            new MinuteBucket(
                rs.getTimestamp("minute").toInstant(),
                rs.getLong("transactions"),
                rs.getLong("alerts")),
        minutes);
  }

  /** Score histogram in buckets of 10 (0-9, 10-19, …, 90-100). */
  public List<ScoreBucket> scoreDistribution() {
    return jdbc.query(
        """
        SELECT least(score / 10 * 10, 90) AS bucket, count(*) AS c
        FROM transactions WHERE score > 0 GROUP BY 1 ORDER BY 1
        """,
        (rs, i) -> new ScoreBucket(rs.getInt("bucket"), rs.getLong("c")));
  }
}
