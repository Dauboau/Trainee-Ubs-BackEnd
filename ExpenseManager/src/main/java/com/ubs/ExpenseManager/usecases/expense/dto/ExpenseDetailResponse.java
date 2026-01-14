package com.ubs.ExpenseManager.usecases.expense.dto;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentDetailedResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseDetailResponse(
    UUID id,
    EmployeeInfo employee,
    DepartmentDetailedResponse department,
    OffsetDateTime date,
    ExpenseCategory category,
    BigDecimal amount,
    CurrencyCode currency,
    BigDecimal exchangeRate,
    String description,
    String receiptUrl,
    ManagerDetailedDecisionInfo managerDecision,
    FinanceDetailedDecisionInfo financeDecision,
    Boolean revision,
    Instant createdAt,
    Instant updatedAt,
    ExpenseStatus status
) {
    public static ExpenseDetailResponse fromEntity(Expense expense) {
        return new ExpenseDetailResponse(
            expense.getId(),
            expense.getEmployee() != null ? EmployeeInfo.fromEntity(expense.getEmployee()) : null,
            expense.getDepartment() != null ? DepartmentDetailedResponse.fromEntity(expense.getDepartment()) : null,
            expense.getDate(),
            expense.getCategory(),
            expense.getAmount(),
            expense.getCurrency(),
            expense.getExchangeRate(),
            expense.getDescription(),
            expense.getReceiptUrl(),
            expense.getManager() != null ? new ManagerDetailedDecisionInfo(
                EmployeeInfo.fromEntity(expense.getManager()),
                expense.getManagerDecision(),
                expense.getManagerDecisionDate()
            ) : null,
            expense.getFinance() != null ? new FinanceDetailedDecisionInfo(
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

    public record ManagerDetailedDecisionInfo(
        EmployeeInfo manager,
        DecisionType decision,
        OffsetDateTime decisionDate
    ) {}

    public record FinanceDetailedDecisionInfo(
        EmployeeInfo finance,
        DecisionType decision,
        OffsetDateTime decisionDate
    ) {}
}
