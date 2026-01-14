package com.ubs.ExpenseManager.usecases.expense.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.web.multipart.MultipartFile;

public record ExpenseRequest(

    String description,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Currency is required")
    CurrencyCode currency,

    @NotNull(message = "Expense category is required")
    ExpenseCategory category,

    @NotNull(message = "Expense date is required")
    OffsetDateTime expenseDate,

    @NotNull(message = "Receipt image is required")
    MultipartFile receiptImage

) {}
