package com.ubs.ExpenseManager.usecases.expense.observer;

import com.ubs.ExpenseManager.entities.alert.Alert;
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

    @EventListener
    @Transactional
    public void handleAlerts(List<Alert> alerts) {
        alerts.forEach(alert ->
                alertUseCase.create(alert.getId(), alert.getType(), alert.getMessage())
        );
    }
}