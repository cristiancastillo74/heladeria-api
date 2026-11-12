package com.heladeria.heladeria.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
        BigDecimal salesToday,
        BigDecimal expensesToday,
        List<DailySalesDTO> weeklySales,
        List<ProductSalesDTO> bestSellingProducts,
        List<ProductSalesDTO> leastSellingProducts,
        List<FlavorSalesDTO> bestSellingFlavors,
        List<InventoryAlertDTO> inventoryAlerts
) {}