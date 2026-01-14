package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.exception.UnauthorizedException;

import java.time.OffsetDateTime;

public class ApprovedByManagerState implements ExpenseState {

    @Override
    public void approve(Expense expense, Employee employee, boolean hasActiveAlerts) {
        if (employee.getRole() != Role.FINANCE) {
            throw new UnauthorizedException("Only finance can approve this expense");
        }
        expense.setFinanceDecisionDate(OffsetDateTime.now());
        expense.setFinanceDecision(DecisionType.APPROVED);
        expense.setFinance(employee);

        if (hasActiveAlerts) {
            expense.setRevision(true);
        }
    }

    @Override
    public void reject(Expense expense, Employee employee) {
        if (employee.getRole() != Role.FINANCE) {
            throw new UnauthorizedException("Only finance can deny this expense");
        }
        expense.setFinanceDecisionDate(OffsetDateTime.now());
        expense.setFinanceDecision(DecisionType.REJECTED);
        expense.setFinance(employee);
    }
}
