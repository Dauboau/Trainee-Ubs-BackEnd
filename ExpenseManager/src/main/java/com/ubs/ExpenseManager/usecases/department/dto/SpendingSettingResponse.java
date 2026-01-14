package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import java.math.BigDecimal;

public record SpendingSettingResponse(
    ExpenseCategory category,
    SpendingType type,
    BigDecimal budget
) {
    public static SpendingSettingResponse fromEntity(SpendingSetting spendingSetting) {
        return new SpendingSettingResponse(
            spendingSetting.getCategory(),
            spendingSetting.getType(),
            spendingSetting.getBudget()
        );
    }
}
