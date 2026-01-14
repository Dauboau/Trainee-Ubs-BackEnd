package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.expense.Expense;

import org.springframework.stereotype.Component;

@Component
public class ExpenseStateFactory {

    public ExpenseState from(Expense expense) {
        return switch (expense.getStatus()) {
            case PENDING -> new PendingState();
            case APPROVED_BY_MANAGER -> new ApprovedByManagerState();
            case APPROVED_BY_FINANCE -> new ApprovedByFinanceState();
            case REJECTED -> new RejectedState();
        };
    }
}
