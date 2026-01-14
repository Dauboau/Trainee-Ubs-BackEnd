package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;

public interface ExpenseState {
    void approve(Expense expense, Employee employee, boolean hasActiveAlerts);
    void reject(Expense expense, Employee employee);
}
