package com.fincore.repository;

import com.fincore.model.Account;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for Polymorphic Account entities.
 * Supports standalone and transactional database operations.
 */
public interface AccountRepository extends CrudRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    /**
     * Transaction-scoped update of account balance.
     */
    boolean updateBalance(Connection conn, String accountNumber, double newBalance);

    /**
     * Transaction-scoped account retrieval.
     */
    Optional<Account> findByAccountNumber(Connection conn, String accountNumber);
}
