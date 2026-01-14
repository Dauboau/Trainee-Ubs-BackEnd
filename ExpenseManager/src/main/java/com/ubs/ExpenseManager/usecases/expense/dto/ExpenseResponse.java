package com.ubs.ExpenseManager.usecases.expense.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseResponse(
    UUID id,
    UUID employeeId,
    String departmentName,
    OffsetDateTime date,
    ExpenseCategory category,
    BigDecimal amount,
    CurrencyCode currency,
    BigDecimal exchangeRate,
    String description,
    String receiptUrl,
    ManagerDecisionInfo managerDecision,
    FinanceDecisionInfo financeDecision,
    Boolean revision,
    Instant createdAt,
    Instant updatedAt,
    ExpenseStatus status
) {
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getEmployee() != null ? expense.getEmployee().getId() : null,
            expense.getDepartment() != null ? expense.getDepartment().getName() : null,
            expense.getDate(),
            expense.getCategory(),
            expense.getAmount(),
            expense.getCurrency(),
            expense.getExchangeRate(),
            expense.getDescription(),
            expense.getReceiptUrl(),
            expense.getManager() != null ? new ManagerDecisionInfo(
                expense.getManager().getId(),
                expense.getManagerDecision(),
                expense.getManagerDecisionDate()
            ) : null,
            expense.getFinance() != null ? new FinanceDecisionInfo(
                expense.getFinance().getId(),
                expense.getFinanceDecision(),
                expense.getFinanceDecisionDate()
            ) : null,
            expense.getRevision(),
            expense.getCreatedAt(),
            expense.getUpdatedAt(),
            expense.getStatus()
        );
    }

    public record ManagerDecisionInfo(
        UUID managerId,
        DecisionType decision,
        OffsetDateTime decisionDate
    ) {}

    public record FinanceDecisionInfo(
        UUID financeId,
        DecisionType decision,
        OffsetDateTime decisionDate
    ) {}
}
