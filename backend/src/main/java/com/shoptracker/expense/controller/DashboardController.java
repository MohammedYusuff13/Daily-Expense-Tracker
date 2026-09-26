package com.shoptracker.expense.controller;

import com.shoptracker.expense.dto.ApiResponse;
import com.shoptracker.expense.dto.DashboardSummary;
import com.shoptracker.expense.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardSummary> summary() {
        return ApiResponse.ok(dashboardService.build());
    }
}
