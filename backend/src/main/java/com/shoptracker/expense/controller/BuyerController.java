package com.shoptracker.expense.controller;

import com.shoptracker.expense.dto.ApiResponse;
import com.shoptracker.expense.dto.BuyerRequest;
import com.shoptracker.expense.model.Buyer;
import com.shoptracker.expense.service.BuyerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyers")
public class BuyerController {

    private final BuyerService service;

    public BuyerController(BuyerService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Buyer>> list() {
        return ApiResponse.ok(service.getAll());
    }

    @GetMapping("/search")
    public ApiResponse<List<Buyer>> search(@RequestParam(defaultValue = "") String q) {
        return ApiResponse.ok(service.search(q));
    }

    @GetMapping("/{name}")
    public ApiResponse<Buyer> get(@PathVariable String name) {
        return ApiResponse.ok(service.getByName(name));
    }

    @PostMapping
    public ApiResponse<Buyer> create(@Valid @RequestBody BuyerRequest req) {
        return ApiResponse.ok("Buyer created", service.create(req));
    }
}
