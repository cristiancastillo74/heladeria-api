package com.heladeria.heladeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ProductQuantity {
    private String productCode;
    private int quantity;
}
