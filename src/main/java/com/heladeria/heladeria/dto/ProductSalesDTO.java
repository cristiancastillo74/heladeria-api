package com.heladeria.heladeria.dto;

import java.math.BigDecimal;

public record ProductSalesDTO(String product, Long quantity, BigDecimal revenue) {}