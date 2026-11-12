package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.BalanceReportDTO;
import com.heladeria.heladeria.dto.CylinderInventoryReportDTO;
import com.heladeria.heladeria.dto.ProductInventoryReportDTO;
import com.heladeria.heladeria.dto.SalesReportDTO;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import com.heladeria.heladeria.repository.SaleItemRepository;
import com.heladeria.heladeria.service.BalanceReportService;
import com.heladeria.heladeria.service.ProductInventoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private ProductInventoryReportService productInventoryReportServiceImp;
    @Autowired
    private CylinderInventoryRepository cylinderInventoryRepository;
    @Autowired
    private BalanceReportService balanceReportService;



    @GetMapping("/sales")
    public Page<SalesReportDTO> getSalesReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return saleItemRepository.getSalesReport(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                branchId,
                pageable
        );
    }


    @GetMapping("/sales/total")
    public BigDecimal getTotalSalesAmount(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "branchId", required = false) Long branchId
    ) {
        return saleItemRepository.getTotalSalesAmount(startDate, endDate, branchId);
    }


    @GetMapping("/inventory/products")
    public List<ProductInventoryReportDTO> getProductInventoryReport(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        return productInventoryReportServiceImp.getCurrentInventoryReport(branchId);
    }

    @GetMapping("/inventory/cylinders")
    public List<CylinderInventoryReportDTO> getCylinderInventoryReport(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        return cylinderInventoryRepository.getCylinderInventoryReport(branchId);
    }

    @GetMapping("/balance")
    public BalanceReportDTO getBalanceReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "branchId", required = false) Long branchId
    ) {
        return balanceReportService.getBalance(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                branchId
        );
    }



}
