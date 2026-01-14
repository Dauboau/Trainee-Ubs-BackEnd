package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.exception.BusinessRuleException;

public class ApprovedByFinanceState implements ExpenseState {

    @Override
    public void approve(Expense expense, Employee employee, boolean hasActiveAlerts) {
        throw new BusinessRuleException(
            "Expense already approved by finance and cannot be approved again");
    }

    @Override
    public void reject(Expense expense, Employee employee) {
        throw new BusinessRuleException(
            "Expense already approved by finance and cannot be rejected");
    }
}
