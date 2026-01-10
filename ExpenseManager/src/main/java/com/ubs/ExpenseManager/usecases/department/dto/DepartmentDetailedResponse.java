package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record DepartmentDetailedResponse(
    String name,
    CurrencyCode currency,
    BigDecimal monthlyBudget,
    List<SpendingSettingResponse> spendingSettings
) {
    public static DepartmentDetailedResponse fromEntity(Department department) {
        return new DepartmentDetailedResponse(
            department.getName(),
            department.getCurrency(),
            department.getMonthlyBudget(),
            department.getSpendingSettings() != null ? department.getSpendingSettings().stream()
                .map(SpendingSettingResponse::fromEntity)
                .toList() : new ArrayList<>()
        );
    }
}
