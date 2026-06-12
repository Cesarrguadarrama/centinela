package mx.centinela.bootstrap.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

  private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

  /**
   * Two quick retries for transient failures (DB hiccup), then log-and-skip. A malformed event must
   * never wedge the partition — losing one synthetic record beats stalling the stream.
   */
  @Bean
  DefaultErrorHandler kafkaErrorHandler() {
    DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000L, 2));
    handler.setRetryListeners(
        (record, ex, attempt) ->
            log.warn(
                "retry {} for offset {} in {}: {}",
                attempt,
                record.offset(),
                record.topic(),
                ex.getMessage()));
    return handler;
  }
}
