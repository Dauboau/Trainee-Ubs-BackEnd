package com.ubs.ExpenseManager.usecases.expense.strategies.categoryStrategy;

import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/// NOTE: Mileage reimbursement formula (km * rate), with odometer validation or GPS distance if available;
/// public-transport passes treated as subscription vs one-off; max per-ride limits and daily transport cap;
/// allow pooling multi-leg fares into one claim.

@Component
@AllArgsConstructor
public class TransportStrategy implements ExpenseStrategy{
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean isKindOf(ExpenseCategory category) {
        return category == ExpenseCategory.TRANSPORT;
    }

    public void calculateLimits(Expense expense) {

        eventPublisher.publishEvent(expense);
    }
}
