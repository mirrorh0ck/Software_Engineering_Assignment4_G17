package com.busdriver.exception;

// Our own exception for business rule violations.
// Used whenever a Driver rule (D1-D5) or a Bus rule (B1-B5) is broken.
// Extends RuntimeException so we don't have to declare throws everywhere
// (covered in week 5 - unchecked exceptions).
public class ValidationException extends RuntimeException {

    // Just to silence the IDE warning about serialisation
    private static final long serialVersionUID = 1L;

    // basic message-only constructor
    public ValidationException(String message) {
        super(message);
    }

    // wrap another exception when we need to (e.g. IOException from file load)
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
