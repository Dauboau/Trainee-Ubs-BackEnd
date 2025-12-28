package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

public record ExpenseRequest(
    UUID employeeId,
    String departmentName,
    String description,
    BigDecimal amount,
    CurrencyCode currency,
    ExpenseCategory category,
    OffsetDateTime expenseDate,
    String receiptUrl
) {}
