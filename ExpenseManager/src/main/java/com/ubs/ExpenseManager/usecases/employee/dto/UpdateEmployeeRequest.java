package com.ubs.ExpenseManager.usecases.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateEmployeeRequest(

    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotNull(message = "Manager id is required")
    UUID managerId,

    @NotBlank(message = "Department id is required")
    String departmentName,

    @NotBlank(message = "Position is required")
    String position,

    @NotNull(message = "Active status is required")
    Boolean active
) {}
