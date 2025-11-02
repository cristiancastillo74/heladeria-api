package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.CylinderInventoryReportDTO;
import com.heladeria.heladeria.dto.ProductInventoryReportDTO;
import com.heladeria.heladeria.dto.SalesReportDTO;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import com.heladeria.heladeria.repository.SaleItemRepository;
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


    @GetMapping("/inventory/products")
    public List<ProductInventoryReportDTO> getProductInventoryReport() {
        return productInventoryReportServiceImp.getCurrentInventoryReport();
    }

    @GetMapping("/inventory/cylinders")
    public List<CylinderInventoryReportDTO> getCylinderInventoryReport() {
        return cylinderInventoryRepository.getCylinderInventoryReport();
    }



}
