package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/// NOTE: supplier whitelist/blacklist checks; recurring subscription detection (flag if similar monthly);
/// capitalization threshold (route to procurement if above X); split invoices across projects/departments.

@Component
@AllArgsConstructor
public class OtherStrategy implements ExpenseStrategy{
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    public boolean isKindOf(ExpenseCategory category) {
        return category == ExpenseCategory.OTHER;
    }

    @Override
    public void calculateLimits(Expense expense) {

        eventPublisher.publishEvent(expense);
    }
}
