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
public class MonthlyValidationStrategy implements SpendingValidationStrategy {

    @Override
    public SpendingType getType() {
        return SpendingType.MONTHLY;
    }

    @Override
    public void validate(Expense expense, SpendingSetting setting, List<Expense> approvedExpenses,
                         BigDecimal amountConverted, List<Alert> alerts) {

        // Calculate total spent in this category for the month
        BigDecimal totalSpentInCategory = approvedExpenses.stream()
                .filter(x -> x.getCategory() == expense.getCategory())
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = totalSpentInCategory.add(amountConverted);
        BigDecimal remainingMonthlyBudget = setting.getBudget().subtract(totalSpentInCategory);

        // Condition 1: Projected total exceeds monthly budget
        if (setting.getBudget().compareTo(projectedTotal) < 0) {
            String message = String.format(
                    "The requested amount (%s) exceeds the available monthly budget '%s' for category (%s)",
                    expense.getAmount(),
                    remainingMonthlyBudget,
                    expense.getCategory()
            );
            Alert alert = new Alert(
                    expense.getId(),
                    expense,
                    AlertType.CATEGORY_MONTHLY,
                    message
            );
            alerts.add(alert);
        }

        // Condition 2: Amount itself exceeds monthly budget
        if (setting.getBudget().compareTo(amountConverted) < 0) {
            String message = String.format(
                    "The requested amount (%s) exceeds the monthly budget '%s' for category (%s)",
                    expense.getAmount(),
                    setting.getBudget(),
                    expense.getCategory()
            );
            Alert alert = new Alert(
                    expense.getId(),
                    expense,
                    AlertType.CATEGORY_MONTHLY,
                    message
            );
            alerts.add(alert);
        }
    }
}
