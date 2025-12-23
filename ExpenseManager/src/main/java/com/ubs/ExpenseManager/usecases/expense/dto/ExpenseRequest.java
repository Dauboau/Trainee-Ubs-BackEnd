package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

public record ExpenseRequest(
    UUID employeeId,
    String description,
    BigDecimal amount,
    ExpenseCategory category,
    LocalDate expenseDate,
    String receiptUrl
) {}
