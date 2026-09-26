package com.shoptracker.expense.exception;

/** Wraps any low-level IO / POI failure while reading or writing the workbook. */
public class ExcelStorageException extends RuntimeException {
    public ExcelStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelStorageException(String message) {
        super(message);
    }
}
