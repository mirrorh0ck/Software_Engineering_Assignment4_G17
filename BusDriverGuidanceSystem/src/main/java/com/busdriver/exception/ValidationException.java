package com.busdriver.exception;

/**
 * Custom unchecked exception thrown when a Driver / Bus business rule
 * (D1-D5 or B1-B5) is violated, or when an Add/Update/Retrieve operation
 * fails its preconditions.
 *
 * Using a dedicated exception type lets the JUnit 5 tests assert on
 * the exception type (e.g. via assertThrows) without coupling to
 * generic RuntimeException.
 */
public class ValidationException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Build a validation exception with a human-readable message that
     * states which business rule was violated.
     *
     * @param message the explanation of the rule violation
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Build a validation exception that wraps an underlying cause
     * (e.g. an IOException while reading the TXT file).
     *
     * @param message description of the failure
     * @param cause   the underlying exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
