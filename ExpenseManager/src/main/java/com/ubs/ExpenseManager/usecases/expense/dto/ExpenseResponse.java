package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

public record ExpenseResponse(
    UUID id,
    UUID employeeId,
    String employeeName,
    OffsetDateTime date,
    ExpenseCategory category,
    BigDecimal amount,
    CurrencyCode currency,
    String description,
    String receiptUrl,
    Instant createdAt
) {
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getEmployee() != null ? expense.getEmployee().getId() : null,
            expense.getEmployee() != null ? expense.getEmployee().getName() : null,
            expense.getDate(),
            expense.getCategory(),
            expense.getAmount(),
            expense.getCurrency(),
            expense.getDescription(),
            expense.getReceiptUrl(),
            expense.getCreatedAt()
        );
    }
}
