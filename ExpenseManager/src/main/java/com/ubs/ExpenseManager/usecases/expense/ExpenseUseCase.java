package com.ubs.ExpenseManager.usecases.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public ExpenseResponse create(ExpenseRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        Department department = departmentRepository.findById(request.departmentName())
            .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado"));

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setDate(request.expenseDate());
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
    public ExpenseResponse findById(UUID id) {
        return expenseRepository.findById(id)
            .map(ExpenseResponse::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findByEmployeeId(UUID employeeId) {
        return expenseRepository.findByEmployeeId(employeeId).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }
}
