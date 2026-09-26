package com.shoptracker.expense.dto;

import com.shoptracker.expense.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Generic report payload used by daily / weekly / monthly / buyer reports. */
@Data
@Builder
public class ReportData {
    private String title;
    private String period;
    private double totalIncome;
    private double totalExpense;
    private double net;
    private List<Transaction> transactions;
}
