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
public class MonthlyValidationStrategy implements SpendingValidationStrategy {

    @Override
    public SpendingType getType() {
        return SpendingType.MONTHLY;
    }

    @Override
    public void validate(Expense expense, SpendingSetting setting, List<Expense> approvedExpenses,
                         BigDecimal amountConverted, AlertsCreatedEvent alertsCreatedEvent) {

        // Calculate total spent in this category for the month
        BigDecimal totalSpentInCategory = approvedExpenses.stream()
                .filter(x -> x.getCategory() == expense.getCategory())
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = totalSpentInCategory.add(amountConverted);
        BigDecimal remainingMonthlyBudget = setting.getBudget().subtract(totalSpentInCategory);

        boolean exceedsIndividual = setting.getBudget().compareTo(amountConverted) < 0;
        boolean exceedsProjected = setting.getBudget().compareTo(projectedTotal) < 0;

        if (exceedsIndividual || exceedsProjected) {
            StringBuilder message = new StringBuilder();
            message.append(exceedsIndividual? String.format(
                    "Policy Violation: Individual expense (%s %s) exceeds the total monthly category limit of %s %s. ",
                    expense.getAmount(), expense.getCurrency(), setting.getBudget(), expense.getDepartment().getCurrency()):"");
            message.append(exceedsProjected? String.format(
                    "Budget Alert: This expense of %s %s exceeds the remaining monthly budget for '%s'. " +
                            "Available: %s %s. Total spent this month: %s %s.",
                    expense.getAmount(), expense.getCurrency(), expense.getCategory(),
                    remainingMonthlyBudget, expense.getDepartment().getCurrency(), totalSpentInCategory, expense.getDepartment().getCurrency()):"");

            Alert alert = new Alert(
                    expense,
                    AlertType.CATEGORY_MONTHLY,
                    message.toString()
            );
            alertsCreatedEvent.add(alert);
        }
    }
}
