package com.ubs.ExpenseManager.usecases.expense.observer;

import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import com.ubs.ExpenseManager.usecases.expense.strategies.settingStrategy.SpendingValidationFactory;
import com.ubs.ExpenseManager.usecases.expense.strategies.settingStrategy.SpendingValidationStrategy;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.GenerationType.UUID;


@Service
@AllArgsConstructor
public class ExpenseObserver {
    private final SpendingSettingRepository spendingSettingRepository;
    private final ExpenseRepository expenseRepository;
    private final AlertUseCase alertUseCase;
    private final SpendingValidationFactory validationFactory;

    @EventListener
    @Transactional
    public void alertObserver(Expense expense) {
        // 1. Busca todas as configurações (DAILY, MONTHLY) para a categoria
        List<SpendingSetting> settings = spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                expense.getDepartment().getName(),
                expense.getCategory());

        // Se não houver configuração, gera alerta e encerra
        if (settings == null || settings.isEmpty()) {
            String message = "The category " + expense.getCategory() + " is not configured.";
            alertUseCase.create(expense.getId(), AlertType.CATEGORY_DAILY, message);
            return;
        }

        // 2. Prepara os dados básicos para os cálculos
        BigDecimal expenseAmountConverted = expense.getAmount().multiply(expense.getExchangeRate());
        Expense.Result interval = expense.getMonthlyInterval();

        // Busca despesas aprovadas do mês (uma única ida ao banco)
        List<Expense> approvedExpenses = expenseRepository
                .findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                        expense.getDepartment().getName(),
                        DecisionType.APPROVED,
                        interval.beginningOfMonth(),
                        interval.endOfMonth());

        settings.forEach(setting -> {
            SpendingValidationStrategy strategy = validationFactory.getStrategy(setting.getId().getType());
            if (strategy != null) {
                strategy.validate(expense, setting, approvedExpenses, expenseAmountConverted);
            }
        });

        // 4. Validação do Orçamento do Departamento (Independente de categoria)
        BigDecimal totalAmountApprovedForDepartment = approvedExpenses.stream()
                .map(x -> x.getAmount().multiply(x.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingDepartmentBudget = expense.getDepartment().getMonthlyBudget().subtract(totalAmountApprovedForDepartment);

        if (remainingDepartmentBudget.compareTo(expenseAmountConverted) < 0) {
            String message = "The amount (" + expense.getAmount() + ") exceeds the available department budget of: " + remainingDepartmentBudget;
            this.sendAlert(expense.getId(), AlertType.DEPARTMENT_MONTHLY, message);
        }
    }
    public void sendAlert(UUID expenseId, AlertType alert, String message){
        alertUseCase.create(expenseId, alert, message);
    }
}

