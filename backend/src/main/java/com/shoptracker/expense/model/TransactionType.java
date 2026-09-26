package com.shoptracker.expense.model;

/**
 * Credit increases money owed to the shop / income received.
 * Debit represents an outgoing / expense.
 * The dashboard treats CREDIT as income and DEBIT as expense.
 */
public enum TransactionType {
    CREDIT,
    DEBIT
}
