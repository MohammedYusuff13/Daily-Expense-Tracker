package com.shoptracker.expense.service;

import com.shoptracker.expense.dto.OutstandingRow;
import com.shoptracker.expense.dto.ReportData;
import com.shoptracker.expense.model.Buyer;
import com.shoptracker.expense.model.Transaction;
import com.shoptracker.expense.model.TransactionType;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    private final ExcelStorageService storage;

    public ReportService(ExcelStorageService storage) {
        this.storage = storage;
    }

    public ReportData daily(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        return buildRange("Daily Report", day.toString(), day, day);
    }

    public ReportData weekly(LocalDate anchor) {
        LocalDate a = anchor == null ? LocalDate.now() : anchor;
        LocalDate start = a.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        return buildRange("Weekly Report", start + " to " + end, start, end);
    }

    public ReportData monthly(YearMonth month) {
        YearMonth ym = month == null ? YearMonth.now() : month;
        return buildRange("Monthly Report", ym.toString(), ym.atDay(1), ym.atEndOfMonth());
    }

    public ReportData buyerWise(String buyerName) {
        List<Transaction> txns = storage.getAllTransactions().stream()
                .filter(t -> buyerName.equalsIgnoreCase(t.getBuyerName()))
                .sorted(Comparator.comparing(Transaction::getDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return summarize("Buyer Report: " + buyerName, buyerName, txns);
    }

    public List<OutstandingRow> outstanding() {
        return storage.getAllBuyers().stream()
                .filter(b -> b.getCurrentBalance() > 0)
                .sorted(Comparator.comparingDouble(Buyer::getCurrentBalance).reversed())
                .map(b -> new OutstandingRow(b.getName(), b.getMobile(), b.getCurrentBalance()))
                .toList();
    }

    private ReportData buildRange(String title, String period, LocalDate from, LocalDate to) {
        List<Transaction> txns = storage.getAllTransactions().stream()
                .filter(t -> t.getDate() != null
                        && !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
                .sorted(Comparator.comparing(Transaction::getDate))
                .toList();
        return summarize(title, period, txns);
    }

    private ReportData summarize(String title, String period, List<Transaction> txns) {
        double income = txns.stream().filter(t -> t.getType() == TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount).sum();
        double expense = txns.stream().filter(t -> t.getType() == TransactionType.DEBIT)
                .mapToDouble(Transaction::getAmount).sum();
        return ReportData.builder()
                .title(title)
                .period(period)
                .totalIncome(income)
                .totalExpense(expense)
                .net(income - expense)
                .transactions(txns)
                .build();
    }
}
