package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import java.math.BigDecimal;

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
    public static DepartmentResponse fromCreate(CreateDepartmentRequest request) {
        return new DepartmentResponse(
            request.name(),
            request.currency(),
            BigDecimal.ZERO
        );
    }
}
