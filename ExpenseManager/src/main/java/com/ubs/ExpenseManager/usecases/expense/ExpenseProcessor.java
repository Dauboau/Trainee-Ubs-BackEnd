package com.ubs.ExpenseManager.usecases.expense;

import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.usecases.expense.strategies.ExpenseStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExpenseProcessor {
    private final List<ExpenseStrategy> expenseStrategies;

    public void process(Expense expense) {
        try {
            ExpenseStrategy selectedStrategy = expenseStrategies.stream()
                    .filter(strategy -> strategy.isKindOf(expense.getCategory()))
                    .findFirst().orElseThrow(() -> new NotImplementedException("Strategy for " + expense.getCategory() + " category is not implemented"));

            selectedStrategy.execute(expense);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}