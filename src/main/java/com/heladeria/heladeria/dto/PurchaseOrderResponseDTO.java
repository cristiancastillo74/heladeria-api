package com.heladeria.heladeria.dto;

import com.heladeria.heladeria.model.PurchaseOrderType;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponseDTO(
        Long id,
        PurchaseOrderType type,
        String title,
        Long branchId,
        String branchName,
        boolean includePrices,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        List<PurchaseOrderItemResponseDTO> items
) {}