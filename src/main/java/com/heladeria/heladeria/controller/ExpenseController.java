package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "http://localhost:3001")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/expenses")
    public List<Expense> obtenerExpenses(){
        var expenses = expenseService.listarExpenses();
        return expenses;
    }
}
