package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SpendingSettingRequest(

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be a positive value")
    BigDecimal budget,

    @NotNull(message = "Category is required")
    ExpenseCategory category,

    @NotNull(message = "Type is required")
    SpendingType type
) {
    public SpendingSettingId toId(String departmentName) {
        return new SpendingSettingId(departmentName, category(), type());
    }
}
