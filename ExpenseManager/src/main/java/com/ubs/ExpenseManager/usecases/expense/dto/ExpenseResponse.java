package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;

public record ExpenseResponse(
    UUID id,
    UUID employeeId,
    String departmentName,
    OffsetDateTime date,
    ExpenseCategory category,
    BigDecimal amount,
    CurrencyCode currency,
    String description,
    String receiptUrl,
    Instant createdAt,
    ExpenseStatus status
) {
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getEmployee() != null ? expense.getEmployee().getId() : null,
            expense.getDepartment() != null ? expense.getDepartment().getName() : null,
            expense.getDate(),
            expense.getCategory(),
            expense.getAmount(),
            expense.getCurrency(),
            expense.getDescription(),
            expense.getReceiptUrl(),
            expense.getCreatedAt(),
            expense.getStatus()
        );
    }
}
