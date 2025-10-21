package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.SalesReportDTO;
import com.heladeria.heladeria.repository.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@RestController
@RequestMapping("api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private SaleItemRepository saleItemRepository;


    @GetMapping("/sales")
    public Page<SalesReportDTO> getSalesReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return saleItemRepository.getSalesReport(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                pageable
        );
    }


    @GetMapping("/sales/total")
    public BigDecimal getTotalSalesAmount(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return saleItemRepository.getTotalSalesAmount(startDate, endDate);
    }


}
