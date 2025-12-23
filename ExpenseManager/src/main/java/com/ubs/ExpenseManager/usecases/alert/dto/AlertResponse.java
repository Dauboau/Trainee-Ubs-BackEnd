package com.ubs.ExpenseManager.usecases.alert.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertStatus;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;

public record AlertResponse(
    UUID id,
    UUID expenseId,
    AlertType type,
    String message,
    AlertStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AlertResponse fromEntity(Alert alert) {
        return new AlertResponse(
            alert.getId(),
            alert.getExpense() != null ? alert.getExpense().getId() : null,
            alert.getType(),
            alert.getMessage(),
            alert.getStatus(),
            alert.getCreatedAt(),
            alert.getUpdatedAt()
        );
    }
}
