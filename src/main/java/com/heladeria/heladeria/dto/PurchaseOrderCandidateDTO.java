package com.heladeria.heladeria.dto;

import java.math.BigDecimal;

public record PurchaseOrderCandidateDTO(
        Long id,
        String name,
        Double currentStock,
        String unit,
        BigDecimal unitPrice
) {}