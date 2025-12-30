package com.ubs.ExpenseManager.usecases.department.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

public record CreateDepartmentRequest(
    String name,
    CurrencyCode currency
) {}
