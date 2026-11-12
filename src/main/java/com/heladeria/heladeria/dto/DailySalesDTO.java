package com.heladeria.heladeria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesDTO(LocalDate date, BigDecimal total) {}