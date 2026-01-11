package com.ubs.ExpenseManager.usecases.expense.observer;

import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseObserver {
    private final AlertUseCase alertUseCase;

    @Transactional
    @EventListener
    public void handleAlerts(AlertsCreatedEvent alertsCreatedEvent) {
        alertsCreatedEvent.getAlerts().forEach(alert ->
                alertUseCase.create(alert.getExpense().getId(), alert.getType(), alert.getMessage())
        );
    }
}