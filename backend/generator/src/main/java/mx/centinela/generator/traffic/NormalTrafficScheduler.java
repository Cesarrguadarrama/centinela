package mx.centinela.generator.traffic;

import java.util.concurrent.ThreadLocalRandom;
import mx.centinela.generator.publish.TransactionPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Emits normal traffic once per second. The per-tick count is Poisson-sampled around the configured
 * TPS so the stream has natural jitter instead of a metronome-like rate.
 */
@Component
public class NormalTrafficScheduler {

  private final TrafficState state;
  private final TransactionFactory factory;
  private final TransactionPublisher publisher;

  public NormalTrafficScheduler(
      TrafficState state, TransactionFactory factory, TransactionPublisher publisher) {
    this.state = state;
    this.factory = factory;
    this.publisher = publisher;
  }

  @Scheduled(fixedDelay = 1000)
  void tick() {
    if (!state.isRunning()) {
      return;
    }
    int count = poisson(state.transactionsPerSecond());
    for (int i = 0; i < count; i++) {
      publisher.publish(factory.normal());
    }
  }

  /** Knuth's algorithm — fine for small lambda values like ours. */
  private int poisson(double lambda) {
    double l = Math.exp(-lambda);
    double p = 1.0;
    int k = 0;
    do {
      k++;
      p *= ThreadLocalRandom.current().nextDouble();
    } while (p > l);
    return k - 1;
  }
}
