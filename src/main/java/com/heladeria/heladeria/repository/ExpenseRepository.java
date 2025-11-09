package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.model.TypeExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.createdAt BETWEEN :startDate AND :endDate
    """)
    BigDecimal getTotalExpenses(
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate")LocalDateTime endDate
            );

    @Query("""
            SELECT COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE e.typeExpense = :typeExpense
            AND e.createdAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal getTotalExpensesByTypeExpense(
            @Param("typeExpense") TypeExpense typeExpense,
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate")LocalDateTime endDate
            );
}
