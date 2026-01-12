package com.ubs.ExpenseManager.usecases.department.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameDepartmentRequest(

    @NotBlank(message = "New name is required")
    String newName
) {}
