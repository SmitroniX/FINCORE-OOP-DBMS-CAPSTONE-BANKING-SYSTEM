package com.fincore;

import com.fincore.model.*;
import com.fincore.service.exception.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests OOP Inheritance, Abstraction, and Polymorphism across Account hierarchies.
 */
public class AccountPolymorphismTest {

    @Test
    @DisplayName("SavingsAccount enforces minimum balance constraint")
    void testSavingsAccountMinimumBalance() {
        SavingsAccount sa = new SavingsAccount("SAV-TEST", 1L, 200.0, 5.0, "ACTIVE", null);
        sa.setMinimumBalance(50.0);

        // Can withdraw within allowance
        assertTrue(sa.canWithdraw(100.0));
        sa.withdraw(100.0);
        assertEquals(100.0, sa.getBalance(), 0.001);

        // Cannot withdraw if it breaches minimum balance ($50)
        assertFalse(sa.canWithdraw(60.0));
        assertThrows(InsufficientFundsException.class, () -> sa.withdraw(60.0));
    }

    @Test
    @DisplayName("CheckingAccount supports overdraft credit limit")
    void testCheckingAccountOverdraft() {
        CheckingAccount ca = new CheckingAccount("CHK-TEST", 1L, 100.0, 500.0, "ACTIVE", null);

        // Can withdraw up to balance + overdraft limit ($600)
        assertTrue(ca.canWithdraw(550.0));
        ca.withdraw(550.0);
        assertEquals(-450.0, ca.getBalance(), 0.001);

        // Exceeding overdraft limit must be rejected
        assertFalse(ca.canWithdraw(60.0));
        assertThrows(InsufficientFundsException.class, () -> ca.withdraw(60.0));
    }

    @Test
    @DisplayName("Polymorphic monthly adjustment calculates interest for Savings and fee for Checking")
    void testPolymorphicAdjustment() {
        Account savings = new SavingsAccount("SAV-TEST", 1L, 12000.0, 6.0, "ACTIVE", null);
        Account checking = new CheckingAccount("CHK-TEST", 1L, 500.0, 1000.0, "ACTIVE", null);

        // 12,000 at 6% annual = $720/yr -> $60/mo
        assertEquals(60.0, savings.calculateMonthlyInterestOrFee(), 0.01);

        // Checking has negative monthly maintenance fee
        assertTrue(checking.calculateMonthlyInterestOrFee() < 0);
    }
}
