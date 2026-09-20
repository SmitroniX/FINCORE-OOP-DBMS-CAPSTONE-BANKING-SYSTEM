package com.fincore.repository;

import com.fincore.model.FinancialRecord;

import java.util.List;
import java.util.Map;

/**
 * Data access contract for Income and Expense records.
 * Provides complete CRUD operations and analytical aggregation methods.
 */
public interface FinancialRecordRepository extends CrudRepository<FinancialRecord, Long> {

    List<FinancialRecord> findByType(FinancialRecord.Type type);

    List<FinancialRecord> findByCategory(String category);

    double getTotalIncome();

    double getTotalExpenses();

    Map<String, Double> getCategoryBreakdown(FinancialRecord.Type type);
}
