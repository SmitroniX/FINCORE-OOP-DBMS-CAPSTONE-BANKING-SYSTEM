package com.fincore.repository;

import com.fincore.model.Customer;
import com.fincore.model.CustomerSummaryDTO;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for Customer entities, including advanced analytical JOIN queries.
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByEmail(String email);

    /**
     * Advanced DBMS query using JOIN, COUNT, SUM, and GROUP BY to generate customer portfolios.
     */
    List<CustomerSummaryDTO> getCustomerPortfolioSummaries();
}
