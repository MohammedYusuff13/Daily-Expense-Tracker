package com.shoptracker.expense.service;

import com.shoptracker.expense.dto.BalanceSheet;
import com.shoptracker.expense.model.Buyer;
import com.shoptracker.expense.model.Transaction;
import com.shoptracker.expense.model.TransactionType;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BalanceSheetService {

    private final ExcelStorageService storage;

    public BalanceSheetService(ExcelStorageService storage) {
        this.storage = storage;
    }

    public BalanceSheet generate() {
        List<Transaction> txns = storage.getAllTransactions();
        List<Buyer> buyers = storage.getAllBuyers();

        double opening = buyers.stream().mapToDouble(Buyer::getOpeningBalance).sum();
        double income = txns.stream().filter(t -> t.getType() == TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount).sum();
        double expense = txns.stream().filter(t -> t.getType() == TransactionType.DEBIT)
                .mapToDouble(Transaction::getAmount).sum();
        double outstanding = buyers.stream().mapToDouble(Buyer::getCurrentBalance)
                .filter(v -> v > 0).sum();

        return BalanceSheet.builder()
                .generatedOn(LocalDate.now())
                .openingBalance(opening)
                .totalIncome(income)
                .totalExpense(expense)
                .currentBalance(income - expense)
                .outstandingCredit(outstanding)
                .build();
    }
}
