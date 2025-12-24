package com.ubs.ExpenseManager.entities.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ubs.ExpenseManager.entities.expense.Expense;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByEmployeeId(UUID employeeId);
    List<Expense> findByEmployeeDepartmentName(String departmentName);
}
