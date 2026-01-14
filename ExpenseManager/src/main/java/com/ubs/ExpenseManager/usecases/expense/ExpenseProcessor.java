package com.ubs.ExpenseManager.usecases.expense;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.usecases.expense.observer.AlertsCreatedEvent;
import com.ubs.ExpenseManager.usecases.expense.strategies.SpendingValidationFactory;
import com.ubs.ExpenseManager.usecases.expense.strategies.SpendingValidationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseProcessor {
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final SpendingValidationFactory validationFactory;
    private final ApplicationEventPublisher eventPublisher;

    public void process(Expense expense) {

        AlertsCreatedEvent alertsCreatedEvent = new AlertsCreatedEvent();

        // 1. Check if category has settings configured
        List<SpendingSetting> settings = spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                expense.getDepartment().getName(),
                expense.getCategory());

        // 2. Prepare data for validation
        BigDecimal expenseAmountConverted = expense.getAmount().multiply(expense.getExchangeRate());
        Expense.Result interval = expense.getMonthlyInterval();

        // 3. Fetch approved expenses for the month (single database call)
        List<Expense> approvedExpenses = expenseRepository
                .findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                        expense.getDepartment().getName(),
                        DecisionType.APPROVED,
                        interval.beginningOfMonth(),
                        interval.endOfMonth());

        if (settings == null || settings.isEmpty()) {
            createMissingConfigurationAlert(expense, alertsCreatedEvent);
            validateDepartmentBudget(expense, approvedExpenses, expenseAmountConverted, alertsCreatedEvent);
            // Publish and exit early - no point validating without settings
            if (!alertsCreatedEvent.getAlerts().isEmpty()) {
                eventPublisher.publishEvent(alertsCreatedEvent);
            }
            return;
        }

        // 4. Validate against each setting (DAILY, MONTHLY)
        settings.forEach(setting -> {
            SpendingValidationStrategy strategy = validationFactory.getStrategy(setting.getId().getType());
            if (strategy != null) {
                strategy.validate(expense, setting, approvedExpenses, expenseAmountConverted, alertsCreatedEvent);
            }
        });

        // 5. Validate department budget (only once, not in strategies)
        validateDepartmentBudget(expense, approvedExpenses, expenseAmountConverted, alertsCreatedEvent);

        // 6. Publish all alerts at once
        if (!alertsCreatedEvent.getAlerts().isEmpty()) {
            eventPublisher.publishEvent(alertsCreatedEvent);
        }
    }

    private static void createMissingConfigurationAlert(Expense expense, AlertsCreatedEvent alertsCreatedEvent) {
        String message = "The category " + expense.getCategory() + " is not configured.";
        Alert alert = new Alert(
                expense,
                AlertType.MISSING_CONFIGURATION,
                message
        );
        alertsCreatedEvent.add(alert);
    }

    private void validateDepartmentBudget(Expense expense, List<Expense> approvedExpenses,
                                          BigDecimal amountConverted, AlertsCreatedEvent alertsCreatedEvent) {
        BigDecimal totalAmountApprovedForDepartment = approvedExpenses.stream()
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingDepartmentBudget = expense.getDepartment().getMonthlyBudget()
                .subtract(totalAmountApprovedForDepartment);

        if (remainingDepartmentBudget.compareTo(amountConverted) < 0) {
            String message = String.format(
                    "Departmental Budget Overrun: The requested amount (%s %s) exceeds the total remaining " +
                            "monthly budget for the %s department. Available funds: %s %s.",
                    expense.getAmount(),
                    expense.getCurrency(),
                    expense.getDepartment().getName(),
                    remainingDepartmentBudget,
                    expense.getDepartment().getCurrency()
            );
            Alert alert = new Alert(
                    expense,
                    AlertType.DEPARTMENT_MONTHLY,
                    message
            );
            alertsCreatedEvent.add(alert);
        }
    }
}