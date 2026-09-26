package com.shoptracker.expense.service;

import com.shoptracker.expense.dto.TransactionRequest;
import com.shoptracker.expense.exception.ResourceNotFoundException;
import com.shoptracker.expense.model.Transaction;
import com.shoptracker.expense.model.TransactionType;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final ExcelStorageService storage;

    public TransactionService(ExcelStorageService storage) {
        this.storage = storage;
    }

    public List<Transaction> getAll() {
        return storage.getAllTransactions().stream()
                .sorted(Comparator.comparing(Transaction::getDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Transaction getById(String id) {
        return storage.findTransaction(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }

    public Transaction create(TransactionRequest req) {
        validateBuyer(req.getBuyerName());
        Transaction txn = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .date(req.getDate())
                .buyerName(normalizeBuyer(req.getBuyerName()))
                .type(req.getType())
                .amount(req.getAmount())
                .description(req.getDescription())
                .paymentMode(req.getPaymentMode())
                .build();
        return storage.addTransaction(txn);
    }

    public Transaction update(String id, TransactionRequest req) {
        getById(id); // ensures it exists
        validateBuyer(req.getBuyerName());
        Transaction updated = Transaction.builder()
                .id(id)
                .date(req.getDate())
                .buyerName(normalizeBuyer(req.getBuyerName()))
                .type(req.getType())
                .amount(req.getAmount())
                .description(req.getDescription())
                .paymentMode(req.getPaymentMode())
                .build();
        return storage.updateTransaction(updated);
    }

    public void delete(String id) {
        if (!storage.deleteTransaction(id)) {
            throw new ResourceNotFoundException("Transaction not found: " + id);
        }
    }

    /** Free-text search + optional filters. All filters are AND-combined. */
    public List<Transaction> searchAndFilter(String query, LocalDate from, LocalDate to,
                                              String buyer, TransactionType type) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return getAll().stream()
                .filter(t -> q.isEmpty()
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q))
                        || (t.getBuyerName() != null && t.getBuyerName().toLowerCase().contains(q)))
                .filter(t -> from == null || (t.getDate() != null && !t.getDate().isBefore(from)))
                .filter(t -> to == null || (t.getDate() != null && !t.getDate().isAfter(to)))
                .filter(t -> buyer == null || buyer.isBlank()
                        || (t.getBuyerName() != null && t.getBuyerName().equalsIgnoreCase(buyer)))
                .filter(t -> type == null || t.getType() == type)
                .toList();
    }

    private void validateBuyer(String buyerName) {
        if (buyerName != null && !buyerName.isBlank() && !storage.buyerExists(buyerName)) {
            throw new ResourceNotFoundException("Unknown buyer: " + buyerName
                    + ". Create the buyer first.");
        }
    }

    private String normalizeBuyer(String buyerName) {
        return (buyerName == null || buyerName.isBlank()) ? "" : buyerName.trim();
    }
}
