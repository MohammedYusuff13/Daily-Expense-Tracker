package com.shoptracker.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Daily Expense Tracker backend.
 *
 * This service stores everything in a single Excel workbook (via Apache POI)
 * so it can run without any database. The workbook is loaded at startup and
 * written back to disk immediately after every mutation.
 */
@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
