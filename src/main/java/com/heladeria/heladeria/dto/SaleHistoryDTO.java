package com.heladeria.heladeria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleHistoryDTO(
        Long id,
        LocalDateTime createdAt,
        String username,
        String branchName,
        BigDecimal totalAmount,
        BigDecimal paymentAmount,
        BigDecimal changeAmount,
        List<SaleItemDetailDTO> items
) {}