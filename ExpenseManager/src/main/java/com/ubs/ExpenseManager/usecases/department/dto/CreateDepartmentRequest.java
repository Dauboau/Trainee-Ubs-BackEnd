package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDepartmentRequest(

    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Currency is required")
    CurrencyCode currency
) {}
