package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.DashboardDTO;
import com.heladeria.heladeria.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDTO getDashboard(@RequestParam(value = "branchId", required = false) Long branchId) {
        return dashboardService.getDashboard(branchId);
    }
}