package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
