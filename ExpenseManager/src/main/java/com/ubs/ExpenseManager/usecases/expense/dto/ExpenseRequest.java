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

    @NotNull(message = "Employee ID is required")
    UUID employeeId,

    @NotBlank(message = "Department name is required")
    String departmentName,

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
