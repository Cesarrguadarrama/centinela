package mx.centinela.bootstrap.api;

import java.util.List;
import mx.centinela.bootstrap.api.dto.MetricsOverview;
import mx.centinela.bootstrap.api.dto.MinuteBucket;
import mx.centinela.bootstrap.api.dto.ScoreBucket;
import mx.centinela.bootstrap.infrastructure.queries.MetricsQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
class MetricsController {

  private final MetricsQueryService metrics;

  MetricsController(MetricsQueryService metrics) {
    this.metrics = metrics;
  }

  @GetMapping("/overview")
  MetricsOverview overview() {
    return metrics.overview();
  }

  @GetMapping("/transactions-per-minute")
  List<MinuteBucket> perMinute(@RequestParam(defaultValue = "30") int minutes) {
    return metrics.perMinute(Math.min(Math.max(minutes, 1), 240));
  }

  @GetMapping("/score-distribution")
  List<ScoreBucket> scoreDistribution() {
    return metrics.scoreDistribution();
  }
}
