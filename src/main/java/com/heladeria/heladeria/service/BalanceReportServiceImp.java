package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.BalanceReportDTO;
import com.heladeria.heladeria.model.TypeExpense;
import com.heladeria.heladeria.repository.ExpenseRepository;
import com.heladeria.heladeria.repository.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BalanceReportServiceImp implements BalanceReportService{

    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private ExpenseRepository expenseRepository;

    public BalanceReportDTO getBalance(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalSales       = saleItemRepository.getTotalSalesAmount(startDate, endDate);
        BigDecimal totalExpenses    = expenseRepository.getTotalExpenses(startDate, endDate);
        BigDecimal fixedExpenses    = expenseRepository.getTotalExpensesByTypeExpense(TypeExpense.GASTO_FIJO, startDate, endDate);
        BigDecimal variableExpenses = expenseRepository.getTotalExpensesByTypeExpense(TypeExpense.GASTO_VARIABLE, startDate, endDate);

        if (totalSales       == null) totalSales        = BigDecimal.ZERO;
        if (totalExpenses    == null) totalExpenses     = BigDecimal.ZERO;
        if (fixedExpenses    == null) fixedExpenses     = BigDecimal.ZERO;
        if (variableExpenses == null) variableExpenses  = BigDecimal.ZERO;

        BigDecimal netBalance = totalSales.subtract(totalExpenses);

        return new BalanceReportDTO(totalSales, totalExpenses, netBalance, fixedExpenses, variableExpenses);
    }
}
