package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TravelExpenseStrategy implements ExpenseStrategy{
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public boolean isKindOf(ExpenseCategory category) {
        return category == ExpenseCategory.TRAVEL;
    }

    @Override
    public void execute(Expense expense) {
        SpendingSetting spendingSetting = spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                expense.getDepartment().getName(),
                expense.getCategory());

        BigDecimal expenseAmountConverted = expense.getAmount().multiply(expense.getExchangeRate());
        Expense.Result interval = expense.getMonthlyInterval();

        List<Expense> approvedExpenses = expenseRepository
                .findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                        expense.getDepartment().getName(),
                        DecisionType.APPROVED,
                        interval.endOfMonth(),
                        interval.beginningOfMonth());

        BigDecimal totalAmountApproved =
                approvedExpenses.stream()
                        .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (spendingSetting.getType() == SpendingType.DAILY) {
            if(spendingSetting.getBudget().compareTo(expenseAmountConverted)  < 0){
                //TODO: trigger flag
                System.out.println("trigger flag, the amount exceeds daily budget");
            }

            BigDecimal remainingBudget = totalAmountApproved.subtract(expense.getDepartment().getMonthlyBudget());
            if(remainingBudget.compareTo(expenseAmountConverted) < 0){
                //TODO: trigger flag
                System.out.println("trigger flag, the amount exceeds the Department budget");
            }
        }
    }
}
