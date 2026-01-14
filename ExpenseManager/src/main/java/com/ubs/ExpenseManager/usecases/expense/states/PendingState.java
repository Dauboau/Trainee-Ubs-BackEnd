package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.exception.UnauthorizedException;

import java.time.OffsetDateTime;

public class PendingState implements ExpenseState {

    @Override
    public void approve(Expense expense, Employee employee, boolean hasActiveAlerts) {
        if (employee.getRole() != Role.MANAGER) {
            throw new UnauthorizedException("Only manager can approve this expense");
        }
        expense.setManagerDecisionDate(OffsetDateTime.now());
        expense.setManagerDecision(DecisionType.APPROVED);
        expense.setManager(employee);
    }

    @Override
    public void reject(Expense expense, Employee employee) {
        if (employee.getRole() != Role.MANAGER) {
            throw new UnauthorizedException("Only manager can reprove this expense");
        }
        expense.setManagerDecisionDate(OffsetDateTime.now());
        expense.setManagerDecision(DecisionType.REJECTED);
        expense.setManager(employee);
    }
}
