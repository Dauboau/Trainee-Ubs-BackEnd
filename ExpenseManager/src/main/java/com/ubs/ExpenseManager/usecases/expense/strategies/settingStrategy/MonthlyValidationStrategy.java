package com.ubs.ExpenseManager.usecases.expense.strategies.settingStrategy;

import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import com.ubs.ExpenseManager.usecases.expense.observer.ExpenseObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@AllArgsConstructor
public class MonthlyValidationStrategy implements SpendingValidationStrategy {
    private final AlertUseCase alertUseCase;
    private final ExpenseObserver expenseObserver;

    @Override
    public SpendingType getType() {
        return SpendingType.MONTHLY;
    }

    @Override
    public void validate(Expense expense, SpendingSetting setting, List<Expense> approvedExpenses, BigDecimal amountConverted) {


        // Sum of approved expenses by month
        BigDecimal totalSpentInCategory = approvedExpenses.stream()
                        .filter(x -> x.getCategory() == expense.getCategory())
                        .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = totalSpentInCategory.add(amountConverted);

        // Validates if the projected amount surpasses the monthly budget
        if (setting.getBudget().compareTo(projectedTotal) < 0) {
            BigDecimal remaining = setting.getBudget().subtract(totalSpentInCategory);
            String message = String.format("The requested amount (%s) exceeds the available monthly budget '%s' for category (%s)",
                    expense.getAmount(), remaining, expense.getCategory());
            expenseObserver.sendAlert(expense.getId(), AlertType.DEPARTMENT_MONTHLY, message);
        }
        //validates if the amountConverted surpasses the budget limit
        if (setting.getBudget().compareTo(amountConverted) < 0) {
            String message = String.format("The requested amount (%s) exceeds the budget '%s' for category (%s)",
                    expense.getAmount(), setting.getBudget(), expense.getCategory());
            expenseObserver.sendAlert(expense.getId(), AlertType.DEPARTMENT_MONTHLY, message);
        }
    }
}