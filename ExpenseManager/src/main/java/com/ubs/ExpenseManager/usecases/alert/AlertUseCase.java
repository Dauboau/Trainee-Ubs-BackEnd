package com.ubs.ExpenseManager.usecases.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ubs.ExpenseManager.usecases.alert.dto.AlertResponse;
import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertStatus;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.alert.repository.AlertRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertUseCase {

    private final AlertRepository alertRepository;
    private final ExpenseRepository expenseRepository;

    public AlertResponse create(UUID expenseId, AlertType type, String message) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

        Alert alert = new Alert();
        alert.setExpense(expense);
        alert.setType(type);
        alert.setMessage(message);

        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll() {
        return alertRepository.findAll().stream()
            .map(AlertResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findByExpenseId(UUID expenseId) {
        return alertRepository.findByExpenseId(expenseId).stream()
            .map(AlertResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status).stream()
            .map(AlertResponse::fromEntity)
            .toList();
    }

    public AlertResponse resolve(UUID id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado"));

        alert.setStatus(AlertStatus.RESOLVED);
        return AlertResponse.fromEntity(alertRepository.save(alert));
    }
}
