package com.fincore.model;

/**
 * Enumeration of supported transaction types in the banking ledger.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_OUT,
    TRANSFER_IN,
    INTEREST,
    FEE
}
