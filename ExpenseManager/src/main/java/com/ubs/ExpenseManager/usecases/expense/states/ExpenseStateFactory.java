package com.ubs.ExpenseManager.usecases.expense.states;

import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.usecases.expense.ExpenseProcessor;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseStateFactory {

    private final ExpenseProcessor expenseProcessor;

    public ExpenseState from(Expense expense) {
        return switch (expense.getStatus()) {
            case PENDING -> new PendingState(expenseProcessor);
            case APPROVED_BY_MANAGER -> new ApprovedByManagerState();
            case APPROVED_BY_FINANCE -> new ApprovedByFinanceState();
            case REJECTED -> new RejectedState();
        };
    }
}

