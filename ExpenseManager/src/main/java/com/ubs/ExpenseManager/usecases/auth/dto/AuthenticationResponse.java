package com.ubs.ExpenseManager.usecases.auth.dto;

import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeResponse;

public record AuthenticationResponse(
    String token,
    EmployeeResponse user
) {}
