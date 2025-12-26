package com.ubs.ExpenseManager.usecases.department.dto;

import java.math.BigDecimal;

public record DepartmentRequest(
    String name,
    BigDecimal monthlyBudget
) {}
