package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateDepartmentRequest(
    @NotNull CurrencyCode currency,
    @NotNull @Positive BigDecimal monthlyBudget
) {}
