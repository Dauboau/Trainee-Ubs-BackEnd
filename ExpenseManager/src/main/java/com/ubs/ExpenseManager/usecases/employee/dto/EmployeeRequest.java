package com.ubs.ExpenseManager.usecases.employee.dto;

import com.ubs.ExpenseManager.entities.employee.enums.Role;

public record EmployeeRequest(
    String name,
    String email,
    String password,
    String departmentId,
    Role role
) {}
