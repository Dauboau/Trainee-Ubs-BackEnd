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
public class DailyValidationStrategy implements SpendingValidationStrategy {
    private final AlertUseCase alertUseCase;


    @Override
    public SpendingType getType() { return SpendingType.DAILY; }

    @Override
    public void validate(Expense expense, SpendingSetting setting, List<Expense> approvedExpenses, BigDecimal amountConverted) {

        BigDecimal amoutApprovedToday = approvedExpenses.stream()
                .filter(x -> x.getDate().getDayOfMonth() == expense.getDate().getDayOfMonth())
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);;

        BigDecimal projectedTotal = amoutApprovedToday.add(amountConverted);
        BigDecimal remainingDailyBudget = setting.getBudget().subtract(amoutApprovedToday);

        //Validates if the amount itself exceeds in
        if (setting.getBudget().compareTo(amountConverted) < 0) {
            String message = String.format("The requested amount (%s) exceeds the daily budget of '%s' for category (%s)",
                    expense.getAmount(), setting.getBudget(), expense.getCategory());
            alertUseCase.create(expense.getId(), AlertType.CATEGORY_DAILY, message);
            return;
        }
        //Validates that the remaining budget for today is less than the projected amount
        if (remainingDailyBudget.compareTo(projectedTotal) < 0) {
            String message = String.format("The requested amount (%s) exceeds the remaining daily budget of '%s' for category (%s)",
                    expense.getAmount(), remainingDailyBudget, expense.getCategory());
           alertUseCase.create(expense.getId(), AlertType.CATEGORY_DAILY, message);
        }
    }
}
