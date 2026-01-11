package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.usecases.expense.observer.AlertsCreatedEvent;

import java.math.BigDecimal;
import java.util.List;

public interface SpendingValidationStrategy {
    SpendingType getType();
    void validate(Expense expense, SpendingSetting setting, List<Expense> approvedExpenses, BigDecimal amountConverted, AlertsCreatedEvent alertsCreatedEvent);
}
