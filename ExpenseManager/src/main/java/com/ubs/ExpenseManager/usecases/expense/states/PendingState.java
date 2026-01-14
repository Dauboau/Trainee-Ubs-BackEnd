package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.exception.UnauthorizedException;
import com.ubs.ExpenseManager.usecases.expense.ExpenseProcessor;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
public class PendingState implements ExpenseState {
    private final ExpenseProcessor expenseProcessor;

    @Override
    public void approve(Expense expense, Employee employee, boolean hasActiveAlerts) {
        Employee directManager = expense.getEmployee().getManager();
        if (directManager == null || !directManager.getId().equals(employee.getId())) {
            throw new UnauthorizedException("Only the direct manager can approve this expense");
        }

        expense.setManagerDecisionDate(OffsetDateTime.now());
        expense.setManagerDecision(DecisionType.APPROVED);
        expenseProcessor.process(savedExpense);
        expense.setManager(employee);
    }

    @Override
    public void deny(Expense expense, Employee employee) {
        Employee directManager = expense.getEmployee().getManager();
        if (directManager == null || !directManager.getId().equals(employee.getId())) {
            throw new UnauthorizedException("Only the direct manager can reject this expense");
        }
        expense.setManagerDecisionDate(OffsetDateTime.now());
        expense.setManagerDecision(DecisionType.REJECTED);
        expense.setManager(employee);
    }
}
