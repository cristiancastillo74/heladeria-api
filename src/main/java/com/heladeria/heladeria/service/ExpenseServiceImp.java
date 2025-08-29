package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Cylinder;
import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        if (expense.getId() == null) {
            // Es nuevo, asignar createdAt y updatedAt
            expense.setCreatedAt(LocalDateTime.now());
            expense.setUpdatedAt(LocalDateTime.now());
        } else {
            // Es update, solo actualizar updatedAt
            // Para conservar createdAt original, buscar el registro actual
            Expense original = expenseRepository.findById(expense.getId())
                    .orElseThrow(() -> new RuntimeException("Expense no encontrado"));

            expense.setCreatedAt(original.getCreatedAt()); // conservar createdAt
            expense.setUpdatedAt(LocalDateTime.now());
        }
        return expenseRepository.save(expense);
    }

    @Override
    public boolean eliminarExpense(Long id) {
        Optional<Expense> expense = expenseRepository.findById(id);
        if(expense.isPresent()){
            expenseRepository.delete(expense.get());
            return true;
        }
        return false;
    }
}
