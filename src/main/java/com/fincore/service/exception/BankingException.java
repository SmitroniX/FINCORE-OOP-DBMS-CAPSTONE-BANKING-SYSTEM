package com.fincore.service.exception;

/**
 * Base custom runtime exception for banking system domain errors.
 */
public class BankingException extends RuntimeException {
    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
