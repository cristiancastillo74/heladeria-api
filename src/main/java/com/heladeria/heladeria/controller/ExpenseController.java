package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.ExpenseRepository;
import com.heladeria.heladeria.repository.UserRepository;
import com.heladeria.heladeria.model.TypeExpense;
import com.heladeria.heladeria.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "*")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BranchRepository branchRepository;

    @GetMapping("/expenses")
    public List<Expense> obtenerExpenses(Authentication authentication){
        User actor = getActor(authentication);
        return isGlobalAdmin(actor)
                ? expenseService.listarExpenses()
                : expenseRepository.findByBranch_Id(requireActorBranch(actor).getId());
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<Expense> obtenerExpenseById(@PathVariable Long id, Authentication authentication){
        Expense expense = findManageableExpense(id, getActor(authentication));
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/expenses/typeExpenses")
    public TypeExpense[] getType(){
        return TypeExpense.values();
    }

    @PostMapping("/expenses")
    public Expense guardarExpense(@RequestBody Expense expense, Authentication authentication){
        User actor = getActor(authentication);
        if (expense.getId() != null && actor.getRole() == Role.EMPLOYEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Los empleados solo pueden registrar gastos nuevos");
        }
        if (expense.getId() != null) {
            findManageableExpense(expense.getId(), actor);
        }
        Branch branch = resolveBranch(actor, expense.getBranch() == null ? null : expense.getBranch().getId());
        expense.setUser(actor);
        expense.setBranch(branch);
        return expenseService.guardarExpense(expense);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication authentication){
        Expense expense = findManageableExpense(id, getActor(authentication));
        expenseRepository.delete(expense);
        return ResponseEntity.noContent().build();
    }

    private User getActor(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private boolean isGlobalAdmin(User user) {
        return user.getRole() == Role.ADMIN_GLOBAL;
    }

    private Branch requireActorBranch(User actor) {
        if (actor.getBranch() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no tiene sucursal asignada");
        }
        return actor.getBranch();
    }

    private Branch resolveBranch(User actor, Long requestedBranchId) {
        if (!isGlobalAdmin(actor)) return requireActorBranch(actor);
        if (requestedBranchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una sucursal para el gasto");
        }
        return branchRepository.findById(requestedBranchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal no encontrada"));
    }

    private Expense findManageableExpense(Long id, User actor) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gasto no encontrado"));
        if (!isGlobalAdmin(actor)) {
            Branch branch = requireActorBranch(actor);
            if (expense.getBranch() == null || !expense.getBranch().getId().equals(branch.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a gastos de otra sucursal");
            }
        }
        return expense;
    }

}
