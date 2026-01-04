package com.ubs.ExpenseManager.usecases.employee.dto;

import com.ubs.ExpenseManager.entities.employee.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EmployeeRequest(
    @NotBlank String name,
    @NotBlank String email,
    @NotNull UUID managerId,
    @NotBlank String password,
    @NotBlank String departmentId,
    @NotBlank String position,
    @NotNull Role role
) {}