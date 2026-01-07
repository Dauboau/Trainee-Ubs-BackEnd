package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

public interface ExpenseStrategy {
    boolean isKindOf(ExpenseCategory category);

    void execute(Expense expense);
}