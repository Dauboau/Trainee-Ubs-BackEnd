package com.ubs.ExpenseManager.usecases.employee.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ManagerReallocationRequest(
    @NotNull UUID currentManagerId,
    @NotNull UUID newManagerId
) {}
