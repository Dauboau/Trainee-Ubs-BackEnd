package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.Expense;
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
                         BigDecimal amountConverted, List<Alert> alerts) {

        // Calculate today's approved expenses
        BigDecimal amountApprovedToday = approvedExpenses.stream()
                .filter(x -> x.getDate().getDayOfMonth() == expense.getDate().getDayOfMonth())
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = amountApprovedToday.add(amountConverted);
        BigDecimal remainingDailyBudget = setting.getBudget().subtract(amountApprovedToday);

        // Condition 1: Amount itself exceeds daily budget
        if (setting.getBudget().compareTo(amountConverted) < 0) {
            String message = String.format(
                    "The requested amount (%s) exceeds the daily budget of '%s' for category (%s)",
                    expense.getAmount(),
                    setting.getBudget(),
                    expense.getCategory()
            );
            Alert alert = new Alert(
                    expense.getId(),
                    expense,
                    AlertType.CATEGORY_DAILY,
                    message
            );
            alerts.add(alert);
        }

        // Condition 2: Projected total exceeds remaining daily budget
        if (remainingDailyBudget.compareTo(projectedTotal) < 0) {
            String message = String.format(
                    "The requested amount (%s) exceeds the remaining daily budget of '%s' for category (%s)",
                    expense.getAmount(),
                    remainingDailyBudget,
                    expense.getCategory()
            );
            Alert alert = new Alert(
                    expense.getId(),
                    expense,
                    AlertType.CATEGORY_DAILY,
                    message
            );
            alerts.add(alert);
        }
    }
}