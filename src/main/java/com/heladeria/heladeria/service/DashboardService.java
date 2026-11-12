package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.DailySalesDTO;
import com.heladeria.heladeria.dto.DashboardDTO;
import com.heladeria.heladeria.dto.InventoryAlertDTO;
import com.heladeria.heladeria.dto.FlavorSalesDTO;
import com.heladeria.heladeria.dto.ProductSalesDTO;
import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import com.heladeria.heladeria.repository.ExpenseRepository;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import com.heladeria.heladeria.repository.ProductRepository;
import com.heladeria.heladeria.repository.SaleItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final SaleItemRepository saleItemRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final CylinderInventoryRepository cylinderInventoryRepository;
        private final ProductRepository productRepository;

    public DashboardService(
            SaleItemRepository saleItemRepository,
            ExpenseRepository expenseRepository,
            ProductInventoryRepository productInventoryRepository,
            CylinderInventoryRepository cylinderInventoryRepository,
            ProductRepository productRepository) {
        this.saleItemRepository = saleItemRepository;
        this.expenseRepository = expenseRepository;
        this.productInventoryRepository = productInventoryRepository;
        this.cylinderInventoryRepository = cylinderInventoryRepository;
        this.productRepository = productRepository;
    }

    public DashboardDTO getDashboard(Long branchId) {
        LocalDate today = LocalDate.now();
        BigDecimal salesToday = saleItemRepository.totalSalesBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay(), branchId);
        BigDecimal expensesToday = expenseRepository.totalExpensesBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay(), branchId);

        List<DailySalesDTO> weeklySales = new ArrayList<>();
        LocalDate firstDay = today.minusDays(6);
        for (int day = 0; day < 7; day++) {
            LocalDate date = firstDay.plusDays(day);
            BigDecimal total = saleItemRepository.totalSalesBetween(
                    date.atStartOfDay(), date.plusDays(1).atStartOfDay(), branchId);
            weeklySales.add(new DailySalesDTO(date, total));
        }

        LocalDateTimeRange month = new LocalDateTimeRange(today.minusDays(29).atStartOfDay(), today.plusDays(1).atStartOfDay());
        List<ProductSalesDTO> soldProducts = new ArrayList<>(
                saleItemRepository.getProductSales(month.start(), month.end(), branchId));
        for (var product : productRepository.findAll()) {
            if (soldProducts.stream().noneMatch(sold -> sold.product().equals(product.getName()))) {
                soldProducts.add(new ProductSalesDTO(product.getName(), 0L, BigDecimal.ZERO));
            }
        }
        List<ProductSalesDTO> leastSelling = soldProducts.stream()
                .sorted(Comparator.comparing(ProductSalesDTO::quantity))
                .limit(5).toList();
        List<ProductSalesDTO> bestSelling = soldProducts.stream().limit(5).toList();
        List<FlavorSalesDTO> bestSellingFlavors = saleItemRepository.getFlavorSales(
                month.start(), month.end(), branchId, PageRequest.of(0, 5));

        List<InventoryAlertDTO> alerts = new ArrayList<>();
        List<ProductInventory> products = branchId == null
                ? productInventoryRepository.findAll()
                : productInventoryRepository.findByBranch_Id(branchId);
        for (ProductInventory inventory : products) {
            if (inventory.getProduct() == null || inventory.getStock() == null) continue;
            int threshold = inventory.getProduct().getLowStockThreshold() == null
                    ? 0 : inventory.getProduct().getLowStockThreshold();
            if (threshold > 0 && inventory.getStock() <= threshold) {
                alerts.add(new InventoryAlertDTO(
                        inventory.getProduct().getName(),
                        inventory.getBranch() == null ? "Sin sucursal" : inventory.getBranch().getName(),
                        inventory.getStock().doubleValue(), threshold, "unidades", "PRODUCT"));
            }
        }

        List<CylinderInventory> cylinders = branchId == null
                ? cylinderInventoryRepository.findAll()
                : cylinderInventoryRepository.findByBranch_Id(branchId);
        for (CylinderInventory inventory : cylinders) {
            if (inventory.getCylinder() == null || inventory.getFraction() == null) continue;
            double threshold = inventory.getCylinder().getLowFractionThreshold() == null
                    ? 0.2 : inventory.getCylinder().getLowFractionThreshold();
            if (threshold > 0 && inventory.getFraction() <= threshold) {
                alerts.add(new InventoryAlertDTO(
                        inventory.getCylinder().getFlavor(),
                        inventory.getBranch() == null ? "Sin sucursal" : inventory.getBranch().getName(),
                        inventory.getFraction() * 100, threshold * 100, "%", "CYLINDER"));
            }
        }

        return new DashboardDTO(salesToday, expensesToday, weeklySales,
                bestSelling, leastSelling, bestSellingFlavors, alerts.stream().limit(20).toList());
    }

    private record LocalDateTimeRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {}
}