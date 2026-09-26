package com.shoptracker.expense.dto;

import com.shoptracker.expense.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** Everything the home dashboard needs, computed in one pass. */
@Data
@Builder
public class DashboardSummary {

    private double currentBalance;      // total credit - total debit
    private double totalCredit;         // all income
    private double totalDebit;          // all expense
    private double todayIncome;
    private double todayExpense;
    private double outstandingCredit;   // sum of positive buyer balances

    private List<Transaction> todayTransactions;
    private List<Transaction> recentTransactions;   // last 10

    /** month label ("2026-09") -> {income, expense}. */
    private List<MonthlyPoint> monthlyTrend;

    /** Aggregate income vs expense for the pie/bar chart. */
    private Map<String, Double> incomeVsExpense;

    @Data
    @Builder
    public static class MonthlyPoint {
        private String month;   // e.g. "Sep 2026"
        private double income;
        private double expense;
    }
}
