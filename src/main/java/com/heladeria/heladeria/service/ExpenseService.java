package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Expense;

import java.util.List;

public interface ExpenseService {
    public List<Expense> listarExpenses();
    public Expense obtenerExpensePorId(Long idExpense);
    public Expense guardarExpense(Expense expense);
    public void eliminarExpense(Long idExpense);
}
