package com.ubs.ExpenseManager.entities.expense.repository;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ubs.ExpenseManager.entities.expense.Expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByEmployeeId(UUID employeeId);

    List<Expense> findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(String departmentName, DecisionType financeDecision, OffsetDateTime financeDecisionDateAfter, OffsetDateTime financeDecisionDateBefore);
}
