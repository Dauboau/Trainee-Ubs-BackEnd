package com.ubs.ExpenseManager.usecases.alert.dto;

import java.time.LocalDateTime;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;

public record AlertResponse(
    Long id,
    Long departmentId,
    String departmentName,
    AlertType type,
    String message,
    Boolean isRead,
    LocalDateTime createdAt
) {
    public static AlertResponse fromEntity(Alert alert) {
        return new AlertResponse(
            alert.getId(),
            alert.getDepartment().getId(),
            alert.getDepartment().getName(),
            alert.getType(),
            alert.getMessage(),
            alert.getIsRead(),
            alert.getCreatedAt()
        );
    }
}
