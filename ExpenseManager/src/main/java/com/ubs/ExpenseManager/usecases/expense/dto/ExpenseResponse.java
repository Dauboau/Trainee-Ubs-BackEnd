package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;

public record ExpenseResponse(
    Long id,
    Long employeeId,
    String employeeName,
    String description,
    BigDecimal amount,
    ExpenseCategory category,
    LocalDate expenseDate,
    ExpenseStatus status,
    String receiptUrl,
    LocalDateTime createdAt
) {
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getEmployee().getId(),
            expense.getEmployee().getName(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getCategory(),
            expense.getExpenseDate(),
            expense.getStatus(),
            expense.getReceiptUrl(),
            expense.getCreatedAt()
        );
    }
}
