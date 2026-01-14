package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.exception.BusinessRuleException;

public class RejectedState implements ExpenseState {

    @Override
    public void approve(Expense expense, Employee employee, boolean hasActiveAlerts) {
        throw new BusinessRuleException("Rejected expenses cannot be approved");
    }

    @Override
    public void reject(Expense expense, Employee employee) {
        throw new BusinessRuleException("Expense has already been rejected");
    }
}
