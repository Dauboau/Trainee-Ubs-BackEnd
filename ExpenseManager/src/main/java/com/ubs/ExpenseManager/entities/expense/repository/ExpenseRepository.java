package com.ubs.ExpenseManager.entities.expense.repository;

import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
        String departmentName,
        DecisionType financeDecision,
        OffsetDateTime financeDecisionDateAfter,
        OffsetDateTime financeDecisionDateBefore
    );

    List<Expense> findAllByEmployeeId(UUID employeeId);

    List<Expense> findAllByManagerDecisionAndFinanceDecisionIsNull(DecisionType managerDecision);

    List<Expense> findAllByEmployeeManagerId(UUID managerId);

    List<Expense> findAllByEmployeeManagerIdAndManagerDecisionIsNull(UUID managerId);

    List<Expense> findAllByEmployeeIdInAndFinanceDecisionAndFinanceDecisionDateBetween(
        List<UUID> employeeIds,
        DecisionType financeDecision,
        OffsetDateTime financeDecisionDateAfter,
        OffsetDateTime financeDecisionDateBefore
    );

    List<Expense> findAllByCategoryInAndFinanceDecisionAndFinanceDecisionDateBetween(
        List<ExpenseCategory> categories,
        DecisionType financeDecision,
        OffsetDateTime financeDecisionDateAfter,
        OffsetDateTime financeDecisionDateBefore
    );

    List<Expense> findAllByDepartmentNameInAndFinanceDecisionAndFinanceDecisionDateBetween(
        List<String> departmentNames,
        DecisionType financeDecision,
        OffsetDateTime financeDecisionDateAfter,
        OffsetDateTime financeDecisionDateBefore
    );
}
