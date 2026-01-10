package com.ubs.ExpenseManager.usecases.expense.strategies.categoryStrategy;

import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/// NOTE: per-trip budget aggregation across legs; travel-class policy (economy/business by seniority/amount);
/// hotel nightly cap per city; car rental vs mileage comparison; enforce booking windows and auto-escalate exceptions;
/// reconcile travel advances against final expenses.

@Component
@RequiredArgsConstructor
public class TravelExpenseStrategy implements ExpenseStrategy{
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean isKindOf(ExpenseCategory category) {
        return category == ExpenseCategory.TRAVEL;
    }

    @Override
    public void calculateLimits(Expense expense) {

        eventPublisher.publishEvent(expense);
    }
}
