package com.ubs.ExpenseManager.usecases.expense.observer;

import com.ubs.ExpenseManager.entities.alert.Alert;

import java.util.ArrayList;
import java.util.List;


public class AlertsCreatedEvent {
    private final List<Alert> alerts;

    public AlertsCreatedEvent() {
        this.alerts = new ArrayList<Alert>();
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void add(Alert alert) {
        this.alerts.add(alert);
    }

}
