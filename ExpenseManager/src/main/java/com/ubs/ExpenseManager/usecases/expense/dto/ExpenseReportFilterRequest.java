package com.ubs.ExpenseManager.usecases.expense.dto;

import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseReportFilterRequest(

    List<UUID> employeeIds,

    List<ExpenseCategory> categories,

    List<String> departmentNames,

    @NotNull(message = "Start date is required")
    OffsetDateTime dateFrom,

    @NotNull(message = "End date is required")
    OffsetDateTime dateTo
) {}