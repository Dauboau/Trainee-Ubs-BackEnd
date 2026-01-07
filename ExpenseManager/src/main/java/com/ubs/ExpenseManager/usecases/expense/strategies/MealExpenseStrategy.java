package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.exception.BusinessRuleException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MealExpenseStrategy implements ExpenseStrategy {
    private final SpendingSettingRepository SpendingSettingRepository;

    @Override
    public boolean isKindOf(ExpenseCategory category) {
        return category == ExpenseCategory.MEAL;
    }

    @Override
    public void execute(Expense expense) {
        SpendingSetting spendingSetting = SpendingSettingRepository.filterByDepartmentAndCategory(
                expense.getDepartment().getName(),
                expense.getCategory()).orElseThrow(() -> new ResourceNotFoundException("No spending settings found"));

        if (expense.getAmount().compareTo(spendingSetting.getBudget()) > 0) {
            //TODO: should we throw an exception here??
            throw new BusinessRuleException("Amount exceeds defined budget of " + spendingSetting.getBudget());
        }

        //TODO: calculate the expense meal with the respective type and save it or change the current workflow state of approvals.

    }
}
