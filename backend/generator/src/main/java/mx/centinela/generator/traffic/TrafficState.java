package mx.centinela.generator.traffic;

import java.util.concurrent.atomic.AtomicBoolean;
import mx.centinela.generator.config.GeneratorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Whether the normal-traffic loop is emitting. Toggled via the REST API. */
@Component
public class TrafficState {

  private static final Logger log = LoggerFactory.getLogger(TrafficState.class);

  private final AtomicBoolean running;
  private final GeneratorProperties properties;

  public TrafficState(GeneratorProperties properties) {
    this.properties = properties;
    this.running = new AtomicBoolean(properties.startsRunning());
  }

  public boolean isRunning() {
    return running.get();
  }

  public void start() {
    if (running.compareAndSet(false, true)) {
      log.info("Normal traffic STARTED ({} tps)", properties.transactionsPerSecond());
    }
  }

  public void stop() {
    if (running.compareAndSet(true, false)) {
      log.info("Normal traffic STOPPED");
    }
  }

  public int transactionsPerSecond() {
    return properties.transactionsPerSecond();
  }
}
