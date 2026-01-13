package com.ubs.ExpenseManager.usecases.auth.dto;

import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeDetailedResponse;

public record AuthenticationResponse(
    String token,
    EmployeeDetailedResponse user
) {}
