package com.ubs.ExpenseManager.usecases.department.dto;

import java.math.BigDecimal;

import com.ubs.ExpenseManager.entities.department.Department;

public record DepartmentResponse(
    String name,
    String currency,
    BigDecimal monthlyBudget
) {
    public static DepartmentResponse fromEntity(Department department) {
        return new DepartmentResponse(
            department.getName(),
            department.getCurrency(),
            department.getMonthlyBudget()
        );
    }
}
