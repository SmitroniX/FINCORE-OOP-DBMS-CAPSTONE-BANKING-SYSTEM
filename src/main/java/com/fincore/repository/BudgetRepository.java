package com.fincore.repository;

import com.fincore.model.Budget;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for Budget allocations and status metrics.
 */
public interface BudgetRepository extends CrudRepository<Budget, Long> {

    Optional<Budget> findByCategory(String category);

    List<Budget> getBudgetsWithSpending();
}
