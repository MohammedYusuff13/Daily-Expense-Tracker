package com.shoptracker.expense.exception;

/** Thrown when trying to create a buyer whose name already exists. */
public class DuplicateBuyerException extends RuntimeException {
    public DuplicateBuyerException(String message) {
        super(message);
    }
}
