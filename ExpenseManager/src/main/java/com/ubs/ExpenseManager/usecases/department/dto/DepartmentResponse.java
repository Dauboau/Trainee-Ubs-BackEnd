package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

public record DepartmentResponse(
    String name,
    CurrencyCode currency,
    BigDecimal monthlyBudget,
    List<SpendingSettingResponse> spendingSettings
) {
    public static DepartmentResponse fromEntity(Department department) {
        return new DepartmentResponse(
            department.getName(),
            department.getCurrency(),
            department.getMonthlyBudget(),
            department.getSpendingSettings() != null ? department.getSpendingSettings().stream()
                .map(SpendingSettingResponse::fromEntity)
                .toList() : List.of()
        );
    }
    public static DepartmentResponse fromCreate(CreateDepartmentRequest request) {
        return new DepartmentResponse(
            request.name(),
            request.currency(),
            BigDecimal.ZERO,
            null
        );
    }
}
