package com.ubs.ExpenseManager.usecases.employee.dto;

import com.ubs.ExpenseManager.entities.employee.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEmployeeRequest(

    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotNull(message = "Manager id is required")
    UUID managerId,

    @NotBlank(message = "Password is required")
    String password,

    @NotBlank(message = "Department id is required")
    String departmentName,

    @NotBlank(message = "Position is required")
    String position,

    @NotNull(message = "Role is required")
    Role role
) {}
