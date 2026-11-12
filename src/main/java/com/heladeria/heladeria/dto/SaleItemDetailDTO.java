package com.heladeria.heladeria.dto;

import java.math.BigDecimal;
import java.util.List;

public record SaleItemDetailDTO(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        List<SaleFlavorDetailDTO> flavors,
        boolean iceCream,
        boolean ignorePrice
) {}