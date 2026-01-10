package com.ubs.ExpenseManager.usecases.expense.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseDetailResponse(
    UUID id,
    EmployeeInfo employee,
    DepartmentResponse department,
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
    public static ExpenseDetailResponse fromEntity(Expense expense) {
        return new ExpenseDetailResponse(
            expense.getId(),
            expense.getEmployee() != null ? EmployeeInfo.fromEntity(expense.getEmployee()) : null,
            expense.getDepartment() != null ? DepartmentResponse.fromEntity(expense.getDepartment()) : null,
            expense.getDate(),
            expense.getCategory(),
            expense.getAmount(),
            expense.getCurrency(),
            expense.getExchangeRate(),
            expense.getDescription(),
            expense.getReceiptUrl(),
            expense.getManager() != null ? new ManagerDecisionInfo(
                EmployeeInfo.fromEntity(expense.getManager()),
                expense.getManagerDecision(),
                expense.getManagerDecisionDate()
            ) : null,
            expense.getFinance() != null ? new FinanceDecisionInfo(
                EmployeeInfo.fromEntity(expense.getFinance()),
                expense.getFinanceDecision(),
                expense.getFinanceDecisionDate()
            ) : null,
            expense.getRevision(),
            expense.getCreatedAt(),
            expense.getUpdatedAt(),
            expense.getStatus()
        );
    }

    public record EmployeeInfo(
        UUID id,
        String name,
        String email,
        String position
    ) {
        public static EmployeeInfo fromEntity(Employee employee) {
            return new EmployeeInfo(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPosition()
            );
        }
    }

    public record ManagerDecisionInfo(
        EmployeeInfo manager,
        DecisionType decision,
        OffsetDateTime decisionDate
    ) {}

    public record FinanceDecisionInfo(
        EmployeeInfo finance,
        DecisionType decision,
        OffsetDateTime decisionDate
    ) {}
}
