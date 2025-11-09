package com.heladeria.heladeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BalanceReportDTO {
    private BigDecimal totalSale;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private BigDecimal fixedExpenses;
    private BigDecimal variableExpenses;

}
