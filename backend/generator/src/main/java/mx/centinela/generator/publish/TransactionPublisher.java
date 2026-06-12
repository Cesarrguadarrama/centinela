package mx.centinela.generator.publish;

import mx.centinela.domain.model.Transaction;
import mx.centinela.generator.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes transactions to the {@code spei.transactions} topic, keyed by source CLABE so all
 * events of one account land in the same partition — preserving per-account ordering for the
 * sliding-window detection downstream.
 */
@Component
public class TransactionPublisher {

  private static final Logger log = LoggerFactory.getLogger(TransactionPublisher.class);

  private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
  private final String topic;

  public TransactionPublisher(
      KafkaTemplate<String, TransactionEvent> kafkaTemplate,
      @Value("${centinela.kafka.topics.transactions}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void publish(Transaction tx) {
    TransactionEvent event = TransactionEvent.from(tx);
    kafkaTemplate
        .send(topic, event.sourceClabe(), event)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error("Failed to publish tx {}: {}", event.id(), ex.getMessage());
              } else {
                log.info(
                    "published {} {} -> {} {} '{}'",
                    event.id().substring(0, 8),
                    tx.source().masked(),
                    tx.destination().masked(),
                    tx.amount(),
                    tx.concept());
              }
            });
  }
}
