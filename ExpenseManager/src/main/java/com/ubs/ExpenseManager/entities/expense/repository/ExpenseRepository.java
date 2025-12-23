package com.ubs.ExpenseManager.entities.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByEmployeeId(Long employeeId);
    List<Expense> findByStatus(ExpenseStatus status);
    List<Expense> findByEmployeeDepartmentId(Long departmentId);
}
