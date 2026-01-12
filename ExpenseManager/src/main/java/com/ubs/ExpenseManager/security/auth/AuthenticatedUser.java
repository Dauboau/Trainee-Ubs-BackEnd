package com.ubs.ExpenseManager.security.auth;

import com.ubs.ExpenseManager.entities.employee.enums.Role;

import java.util.UUID;

public record AuthenticatedUser(
    UUID id,
    String email,
    Role role,
    String departmentId
) {}

