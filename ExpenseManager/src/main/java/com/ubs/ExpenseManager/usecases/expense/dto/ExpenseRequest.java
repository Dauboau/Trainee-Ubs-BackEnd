package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

public record ExpenseRequest(
    UUID employeeId,
    String description,
    BigDecimal amount,
    ExpenseCategory category,
    OffsetDateTime expenseDate,
    String receiptUrl
) {}
