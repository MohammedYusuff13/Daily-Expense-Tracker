package com.shoptracker.expense.exception;

/** Thrown when a buyer or transaction id cannot be found. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
