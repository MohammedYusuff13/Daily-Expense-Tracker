package com.shoptracker.expense.controller;

import com.shoptracker.expense.dto.ApiResponse;
import com.shoptracker.expense.dto.OutstandingRow;
import com.shoptracker.expense.dto.ReportData;
import com.shoptracker.expense.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/daily")
    public ApiResponse<ReportData> daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(service.daily(date));
    }

    @GetMapping("/weekly")
    public ApiResponse<ReportData> weekly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(service.weekly(date));
    }

    @GetMapping("/monthly")
    public ApiResponse<ReportData> monthly(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ApiResponse.ok(service.monthly(month));
    }

    @GetMapping("/buyer/{name}")
    public ApiResponse<ReportData> buyer(@PathVariable String name) {
        return ApiResponse.ok(service.buyerWise(name));
    }

    @GetMapping("/outstanding")
    public ApiResponse<List<OutstandingRow>> outstanding() {
        return ApiResponse.ok(service.outstanding());
    }
}
