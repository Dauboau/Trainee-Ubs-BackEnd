package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.usecases.expense.observer.AlertsCreatedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DailyValidationStrategy implements SpendingValidationStrategy {

    @Override
    public SpendingType getType() {
        return SpendingType.DAILY;
    }

    @Override
    public void validate(Expense expense, SpendingSetting setting, List<Expense> approvedExpenses,
                         BigDecimal amountConverted, AlertsCreatedEvent alertsCreatedEvent) {

        // Calculate today's approved expenses
        BigDecimal amountApprovedToday = approvedExpenses.stream()
                .filter(x -> x.getDate().getDayOfMonth() == expense.getDate().getDayOfMonth())
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = amountApprovedToday.add(amountConverted);
        BigDecimal remainingDailyBudget = setting.getBudget().subtract(amountApprovedToday);

        boolean exceedsLimit = setting.getBudget().compareTo(amountConverted) < 0;
        boolean exceedsRemaining = remainingDailyBudget.compareTo(amountConverted) < 0;

        if (exceedsLimit || exceedsRemaining) {
            // We choose the most relevant message.
            // If the single item is too big (exceedsLimit), that is the primary error.
            String message = exceedsLimit
                    ? String.format(
                    "Daily Limit Exceeded: The requested amount (%s %s) is greater than the total daily allowance of %s %s set for '%s'.",
                    expense.getAmount(), expense.getCurrency(), setting.getBudget(), expense.getDepartment().getCurrency(), expense.getCategory())
                    : String.format(
                    "Daily Budget Exhausted: This expense of %s %s would exceed your remaining budget for today. Remaining: %s %s. Category: %s.",
                    expense.getAmount(), expense.getCurrency(), remainingDailyBudget, expense.getDepartment().getCurrency(), expense.getCategory());

            // Create exactly ONE alert of type CATEGORY_DAILY
            Alert alert = new Alert(
                    expense,
                    AlertType.CATEGORY_DAILY,
                    message
            );
            alertsCreatedEvent.add(alert);
        }
    }
}