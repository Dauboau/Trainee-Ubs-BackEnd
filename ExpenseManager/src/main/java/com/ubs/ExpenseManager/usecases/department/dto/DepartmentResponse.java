package com.ubs.ExpenseManager.usecases.department.dto;

import java.math.BigDecimal;

import com.ubs.ExpenseManager.entities.department.Department;

public record DepartmentResponse(
    Long id,
    String name,
    BigDecimal monthlyBudget,
    Integer employeeCount
) {
    public static DepartmentResponse fromEntity(Department department) {
        return new DepartmentResponse(
            department.getId(),
            department.getName(),
            department.getMonthlyBudget(),
            department.getEmployees() != null ? department.getEmployees().size() : 0
        );
    }
}
