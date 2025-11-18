package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.model.Status;
import com.heladeria.heladeria.model.TypeExpense;
import com.heladeria.heladeria.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "*")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/expenses")
    public List<Expense> obtenerExpenses(){
        var expenses = expenseService.listarExpenses();
        return expenses;
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<Expense> obtenerExpenseById(@PathVariable Long id){
        Expense expense = expenseService.obtenerExpensePorId(id);
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/expenses/typeExpenses")
    public TypeExpense[] getType(){
        return TypeExpense.values();
    }

    @PostMapping("/expenses")
    public Expense guardarExpense(@RequestBody Expense expense){
        return expenseService.guardarExpense(expense);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id){
        boolean eliminado = expenseService.eliminarExpense(id);
        if (eliminado) {
            return ResponseEntity.noContent().build(); // 204 si se borró
        } else {
            return ResponseEntity.notFound().build(); // 404 si no existe
        }
    }


}
