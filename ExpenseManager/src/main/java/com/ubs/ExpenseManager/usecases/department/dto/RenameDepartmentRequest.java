package com.ubs.ExpenseManager.usecases.department.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameDepartmentRequest(
    @NotBlank
    String newName
) {}
