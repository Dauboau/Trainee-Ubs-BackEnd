package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

///NOTE: Possible Improvements per-diem by location/grade; daily cap & per-item caps; require itemized receipt when amount > threshold;
/// block alcohol or classify separately; allow partial personal split (share calculation); VAT/tax reclaim metadata extraction from receipt.

@Component
@RequiredArgsConstructor
public class MealExpenseStrategy implements ExpenseStrategy {
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean isKindOf(ExpenseCategory category) {
        return category == ExpenseCategory.MEAL;
    }

    @Override
    public void calculateLimits(Expense expense) {

        eventPublisher.publishEvent(expense);

    }
}
