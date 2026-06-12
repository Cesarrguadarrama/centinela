package mx.centinela.domain.port.out;

import mx.centinela.domain.model.ScoredTransaction;

/** Persistence of observed transactions together with their composite score. */
public interface TransactionRepository {

  void save(ScoredTransaction scored);
}
