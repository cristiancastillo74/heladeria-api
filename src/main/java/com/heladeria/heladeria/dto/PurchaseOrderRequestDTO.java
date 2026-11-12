package com.heladeria.heladeria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PurchaseOrderRequestDTO(
        @NotNull Long branchId,
        String title,
        boolean includePrices,
        @NotNull @Valid List<PurchaseOrderItemRequestDTO> items
) {}