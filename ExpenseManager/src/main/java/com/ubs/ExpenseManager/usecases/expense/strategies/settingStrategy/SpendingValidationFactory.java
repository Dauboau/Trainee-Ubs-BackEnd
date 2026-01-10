package com.ubs.ExpenseManager.usecases.expense.strategies.settingStrategy;

import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SpendingValidationFactory {
    private final Map<SpendingType, SpendingValidationStrategy> strategies;

    @Autowired
    public SpendingValidationFactory(List<SpendingValidationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SpendingValidationStrategy::getType, s -> s));
    }

    public SpendingValidationStrategy getStrategy(SpendingType type) {
        return strategies.get(type);
    }
}
