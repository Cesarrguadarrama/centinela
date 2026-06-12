package mx.centinela.domain.port.in;

import mx.centinela.domain.model.Transaction;

/** Entry point of the detection pipeline: persist, evaluate rules, raise alerts. */
public interface ProcessTransactionUseCase {

  void process(Transaction transaction);
}
