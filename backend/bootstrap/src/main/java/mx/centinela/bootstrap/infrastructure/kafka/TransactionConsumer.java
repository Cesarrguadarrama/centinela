package mx.centinela.bootstrap.infrastructure.kafka;

import mx.centinela.domain.port.in.ProcessTransactionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Inbound adapter: drains the transaction stream into the detection use case. */
@Component
class TransactionConsumer {

  private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

  private final ProcessTransactionUseCase processTransaction;

  TransactionConsumer(ProcessTransactionUseCase processTransaction) {
    this.processTransaction = processTransaction;
  }

  @KafkaListener(topics = "${centinela.kafka.topics.transactions}")
  void onTransaction(TransactionEventPayload payload) {
    log.debug("consuming tx {}", payload.id());
    processTransaction.process(payload.toDomain());
  }
}
