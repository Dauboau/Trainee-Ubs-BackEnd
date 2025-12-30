package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDepartmentRequest(
    @NotBlank
    String name,
    @NotNull
    CurrencyCode currency,
    @NotNull
    BigDecimal monthlyBudget
) {}
