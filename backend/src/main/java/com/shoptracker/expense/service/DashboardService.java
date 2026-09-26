package com.shoptracker.expense.service;

import com.shoptracker.expense.dto.DashboardSummary;
import com.shoptracker.expense.model.Buyer;
import com.shoptracker.expense.model.Transaction;
import com.shoptracker.expense.model.TransactionType;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    private final ExcelStorageService storage;

    public DashboardService(ExcelStorageService storage) {
        this.storage = storage;
    }

    public DashboardSummary build() {
        List<Transaction> all = storage.getAllTransactions();
        List<Buyer> buyers = storage.getAllBuyers();
        LocalDate today = LocalDate.now();

        double totalCredit = sum(all, TransactionType.CREDIT);
        double totalDebit = sum(all, TransactionType.DEBIT);

        double todayIncome = all.stream()
                .filter(t -> today.equals(t.getDate()) && t.getType() == TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount).sum();
        double todayExpense = all.stream()
                .filter(t -> today.equals(t.getDate()) && t.getType() == TransactionType.DEBIT)
                .mapToDouble(Transaction::getAmount).sum();

        double outstanding = buyers.stream()
                .mapToDouble(Buyer::getCurrentBalance)
                .filter(v -> v > 0)
                .sum();

        List<Transaction> todayTxns = all.stream()
                .filter(t -> today.equals(t.getDate()))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .toList();

        List<Transaction> recent = all.stream()
                .sorted(Comparator.comparing(Transaction::getDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();

        Map<String, Double> incomeVsExpense = new LinkedHashMap<>();
        incomeVsExpense.put("income", totalCredit);
        incomeVsExpense.put("expense", totalDebit);

        return DashboardSummary.builder()
                .currentBalance(totalCredit - totalDebit)
                .totalCredit(totalCredit)
                .totalDebit(totalDebit)
                .todayIncome(todayIncome)
                .todayExpense(todayExpense)
                .outstandingCredit(outstanding)
                .todayTransactions(todayTxns)
                .recentTransactions(recent)
                .incomeVsExpense(incomeVsExpense)
                .monthlyTrend(buildMonthlyTrend(all))
                .build();
    }

    /** Last 6 months, oldest first, so charts read left-to-right. */
    private List<DashboardSummary.MonthlyPoint> buildMonthlyTrend(List<Transaction> all) {
        Map<YearMonth, double[]> byMonth = new HashMap<>();
        for (Transaction t : all) {
            if (t.getDate() == null) continue;
            YearMonth ym = YearMonth.from(t.getDate());
            double[] pair = byMonth.computeIfAbsent(ym, k -> new double[2]);
            if (t.getType() == TransactionType.CREDIT) pair[0] += t.getAmount();
            else pair[1] += t.getAmount();
        }

        List<DashboardSummary.MonthlyPoint> points = new ArrayList<>();
        YearMonth cursor = YearMonth.now().minusMonths(5);
        for (int i = 0; i < 6; i++) {
            double[] pair = byMonth.getOrDefault(cursor, new double[2]);
            points.add(DashboardSummary.MonthlyPoint.builder()
                    .month(cursor.atDay(1).format(MONTH_LABEL))
                    .income(pair[0])
                    .expense(pair[1])
                    .build());
            cursor = cursor.plusMonths(1);
        }
        return points;
    }

    private double sum(List<Transaction> txns, TransactionType type) {
        return txns.stream()
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
