package com.shoptracker.expense.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/** Real-time balance sheet snapshot. */
@Data
@Builder
public class BalanceSheet {
    private LocalDate generatedOn;
    private double openingBalance;   // sum of all buyer opening balances
    private double totalIncome;      // total credit
    private double totalExpense;     // total debit
    private double currentBalance;   // income - expense
    private double outstandingCredit;
}
