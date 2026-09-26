package com.shoptracker.expense.dto;

import com.shoptracker.expense.model.PaymentMode;
import com.shoptracker.expense.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/** Incoming payload for creating or updating a transaction. */
@Data
public class TransactionRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    /** Optional: link the transaction to a buyer. */
    private String buyerName;

    @NotNull(message = "Transaction type (CREDIT/DEBIT) is required")
    private TransactionType type;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    private String description;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;
}
