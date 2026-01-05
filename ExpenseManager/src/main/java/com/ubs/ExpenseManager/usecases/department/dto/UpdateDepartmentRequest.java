package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateDepartmentRequest(

    @NotNull(message = "Currency is required")
    CurrencyCode currency,

    @NotNull(message = "Monthly budget is required")
    @Positive(message = "Monthly budget must be a positive value")
    BigDecimal monthlyBudget
) {}
