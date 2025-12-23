package com.ubs.ExpenseManager.usecases.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;

    public ExpenseResponse create(ExpenseRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        expense.setReceiptUrl(request.receiptUrl());

        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAll() {
        return expenseRepository.findAll().stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        return expenseRepository.findById(id)
            .map(ExpenseResponse::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findByEmployeeId(Long employeeId) {
        return expenseRepository.findByEmployeeId(employeeId).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findByStatus(ExpenseStatus status) {
        return expenseRepository.findByStatus(status).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    public ExpenseResponse approve(Long id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

        expense.setStatus(ExpenseStatus.APPROVED);
        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }

    public ExpenseResponse reject(Long id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

        expense.setStatus(ExpenseStatus.REJECTED);
        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }
}
