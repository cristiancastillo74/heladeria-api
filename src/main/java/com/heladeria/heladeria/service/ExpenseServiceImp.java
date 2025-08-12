package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImp implements ExpenseService{

    private final ExpenseRepository expenseRepository;

    public ExpenseServiceImp(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public List<Expense> listarExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public Expense obtenerExpensePorId(Long idExpense) {
        return expenseRepository.findById(idExpense).orElse(null);
    }

    @Override
    public Expense guardarExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public void eliminarExpense(Long idExpense) {
        expenseRepository.deleteById(idExpense);
    }
}
