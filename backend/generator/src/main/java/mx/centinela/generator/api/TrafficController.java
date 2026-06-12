package mx.centinela.generator.api;

import java.util.Map;
import mx.centinela.generator.traffic.TrafficState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Starts/stops the normal traffic loop. Thin adapter over TrafficState. */
@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

  private final TrafficState state;

  public TrafficController(TrafficState state) {
    this.state = state;
  }

  @GetMapping
  public Map<String, Object> status() {
    return Map.of("running", state.isRunning(), "tps", state.transactionsPerSecond());
  }

  @PostMapping("/start")
  public Map<String, Object> start() {
    state.start();
    return status();
  }

  @PostMapping("/stop")
  public Map<String, Object> stop() {
    state.stop();
    return status();
  }
}
