package com.ubs.ExpenseManager.usecases.alert;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertStatus;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.alert.repository.AlertRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.alert.dto.AlertResponse;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertUseCase {

    private final AlertRepository alertRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public AlertResponse create(UUID expenseId, AlertType type, String message) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Alert alert = new Alert();
        alert.setExpense(expense);
        alert.setType(type);
        alert.setMessage(message);

        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status).stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AlertResponse resolve(UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new ConflictException("Alert is already resolved");
        }

        alert.setStatus(AlertStatus.RESOLVED);

        Expense expense = alert.getExpense();
        expense.setRevision(false);

        return AlertResponse.fromEntity(alertRepository.save(alert));
    }
}