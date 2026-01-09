package com.ubs.ExpenseManager.usecases.expense.observer;

import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ExpenseObserver {
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final AlertUseCase alertUseCase;

    @EventListener
    @Transactional
    public void alertObserver(Expense expense) {
        SpendingSetting spendingSetting = spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                expense.getDepartment().getName(),
                expense.getCategory());

        if (spendingSetting == null) {
            String message = "The category " + expense.getCategory() + " is not configured.";
            alertUseCase.create(expense.getId(), AlertType.CATEGORY_DAILY, message);
            return;
        }

        BigDecimal expenseAmountConverted = expense.getAmount().multiply(expense.getExchangeRate());
        Expense.@NonNull Result interval = expense.getMonthlyInterval();

        List<Expense> approvedExpenses = expenseRepository
                .findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                        expense.getDepartment().getName(),
                        DecisionType.APPROVED,
                        interval.endOfMonth(),
                        interval.beginningOfMonth());

        BigDecimal totalAmountApprovedForDepartment =
                approvedExpenses.stream()
                        .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (spendingSetting.getType() == SpendingType.DAILY) {
            if (spendingSetting.getBudget().compareTo(expenseAmountConverted) < 0) {
                String message = "The requested amount ("+ expense.getAmount() +") exceeds the daily budget of '"+ spendingSetting.getBudget() +"' for category ("+ expense.getCategory() +")";
                alertUseCase.create(expense.getId(), AlertType.CATEGORY_DAILY, message);
            }
        }

        if (spendingSetting.getType() == SpendingType.MONTHLY) {
            BigDecimal totalAmountSpentForCategory = approvedExpenses.stream()
                    .filter(x -> x.getCategory() == expense.getCategory())
                    .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            if (spendingSetting.getBudget().compareTo(totalAmountSpentForCategory) < 0) {
                BigDecimal remainingAmount = spendingSetting.getBudget().subtract(totalAmountSpentForCategory);
                String message = "The requested amount ("+ expense.getAmount() +") exceeds the available monthly budget '"+ remainingAmount +"' for category ("+ expense.getCategory() +")";
                alertUseCase.create(expense.getId(), AlertType.CATEGORY_MONTHLY, message);
            }
        }

        BigDecimal remainingDepartmentBudget = expense.getDepartment().getMonthlyBudget().subtract(totalAmountApprovedForDepartment);
        if (remainingDepartmentBudget.compareTo(expenseAmountConverted) < 0) {
            String message = "The amount("+ expense.getAmount() +") exceeds the available budget of: " + remainingDepartmentBudget;
            alertUseCase.create(expense.getId(), AlertType.DEPARTMENT_MONTHLY, message);
        }
    }
}
