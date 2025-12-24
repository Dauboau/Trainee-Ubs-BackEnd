package com.ubs.ExpenseManager.usecases.department.dto;

import java.math.BigDecimal;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

public record DepartmentResponse(
    String name,
    CurrencyCode currency,
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
