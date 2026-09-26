package com.shoptracker.expense.controller;

import com.shoptracker.expense.dto.ApiResponse;
import com.shoptracker.expense.dto.TransactionRequest;
import com.shoptracker.expense.model.Transaction;
import com.shoptracker.expense.model.TransactionType;
import com.shoptracker.expense.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Transaction>> list() {
        return ApiResponse.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Transaction> get(@PathVariable String id) {
        return ApiResponse.ok(service.getById(id));
    }

    @GetMapping("/search")
    public ApiResponse<List<Transaction>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String buyer,
            @RequestParam(required = false) TransactionType type) {
        return ApiResponse.ok(service.searchAndFilter(q, from, to, buyer, type));
    }

    @PostMapping
    public ApiResponse<Transaction> create(@Valid @RequestBody TransactionRequest req) {
        return ApiResponse.ok("Transaction added", service.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Transaction> update(@PathVariable String id,
                                           @Valid @RequestBody TransactionRequest req) {
        return ApiResponse.ok("Transaction updated", service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.ok("Transaction deleted", null);
    }
}
