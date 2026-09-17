package com.fincore;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Customer;
import com.fincore.model.CustomerSummaryDTO;
import com.fincore.repository.CustomerRepository;
import com.fincore.repository.impl.JdbcCustomerRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests DBMS Repository CRUD operations and Analytical JOIN queries.
 */
public class RepositoryIntegrationTest {

    private static CustomerRepository customerRepo;

    @BeforeAll
    static void setup() {
        DatabaseManager db = DatabaseManager.getInstance();
        db.initializeDatabase();
        customerRepo = new JdbcCustomerRepository(db);
    }

    @Test
    @DisplayName("Customer CRUD lifecycle persists to database")
    void testCustomerCrud() {
        String unique = UUID.randomUUID().toString().substring(0, 6);
        Customer cust = new Customer("C-" + unique, "Integration User", unique + "@junit.org", "555-1234");

        Customer saved = customerRepo.save(cust);
        assertNotNull(saved.getId(), "Generated key should be populated");

        Optional<Customer> found = customerRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Integration User", found.get().getName());

        found.get().setName("Updated Name");
        assertTrue(customerRepo.update(found.get()));

        Customer reloaded = customerRepo.findById(saved.getId()).orElseThrow();
        assertEquals("Updated Name", reloaded.getName());
    }

    @Test
    @DisplayName("DBMS JOIN query returns portfolio analytics")
    void testCustomerPortfolioReport() {
        List<CustomerSummaryDTO> summaries = customerRepo.getCustomerPortfolioSummaries();
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty(), "Seed data should populate portfolio summaries");
        for (CustomerSummaryDTO dto : summaries) {
            assertNotNull(dto.getCustomerCode());
            assertNotNull(dto.getCustomerName());
            assertTrue(dto.getTotalAccounts() >= 0);
        }
    }
}
