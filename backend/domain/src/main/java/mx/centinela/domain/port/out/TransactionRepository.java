package mx.centinela.domain.port.out;

import mx.centinela.domain.model.Transaction;

/** Persistence of observed transactions. */
public interface TransactionRepository {

  void save(Transaction transaction);
}
