package com.ubs.ExpenseManager.usecases.expense.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpenseRequest(
    @NotNull
    UUID employeeId,
    @NotBlank
    String departmentName,
    String description,
    @NotNull
    @Positive
    BigDecimal amount,
    @NotNull
    CurrencyCode currency,
    @NotNull
    ExpenseCategory category,
    @NotNull
    OffsetDateTime expenseDate,
    @NotNull
    MultipartFile receiptImage
) {}
