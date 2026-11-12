package com.heladeria.heladeria.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PurchaseOrderItemRequestDTO(
        @NotNull Long itemId,
        @NotNull @Positive Integer quantity,
        BigDecimal unitPrice
) {}