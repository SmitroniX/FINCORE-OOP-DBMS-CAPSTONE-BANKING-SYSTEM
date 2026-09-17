package com.fincore.repository;

import com.fincore.model.Transaction;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for immutable Transaction ledger entries.
 */
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findByAccountNumber(String accountNumber);

    List<Transaction> findRecentTransactions(int limit);

    /**
     * Transaction-scoped atomic ledger entry creation.
     */
    Transaction save(Connection conn, Transaction transaction);

    Optional<Transaction> findByTransactionId(String transactionId);
}
