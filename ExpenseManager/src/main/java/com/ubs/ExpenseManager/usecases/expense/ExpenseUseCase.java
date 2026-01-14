package com.ubs.ExpenseManager.usecases.expense;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import com.github.f4b6a3.uuid.UuidCreator;

import com.ubs.ExpenseManager.entities.alert.repository.AlertRepository;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.exception.BusinessRuleException;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.gateways.CurrencyExchangeGateway;
import com.ubs.ExpenseManager.gateways.ImageStorageGateway;
import com.ubs.ExpenseManager.security.auth.AuthenticatedUser;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseDetailResponse;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseReportFilterRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseReportType;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;
import com.ubs.ExpenseManager.usecases.expense.states.ExpenseState;
import com.ubs.ExpenseManager.usecases.expense.states.ExpenseStateFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AlertRepository alertRepository;
    private final CurrencyExchangeGateway currencyExchangeGateway;
    private final ImageStorageGateway storageGateway;
    private final ExpenseProcessor expenseProcessor;
    private final ExpenseStateFactory expenseStateFactory;

    public ExpenseResponse create(ExpenseRequest request, AuthenticatedUser user) {
        Employee employee = employeeRepository.findById(user.id())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(user.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency());
        expense.setCategory(request.category());
        expense.setDate(request.expenseDate());

        java.math.BigDecimal rate = currencyExchangeGateway.getExchangeRate(request.currency(), department.getCurrency());
        expense.setExchangeRate(rate);

        try {
            Metadata receiptImageMetadata = ImageMetadataReader.readMetadata(request.receiptImage().getInputStream());
            Map<String, Map<String, String>> receiptImageMetadataMap = getImageMetadataMap(receiptImageMetadata);
            expense.setReceiptMetadata(receiptImageMetadataMap);
        } catch (ImageProcessingException | IOException e) {
            // Ignore metadata extraction errors
        }

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("-agentlib:jdwp");
        if (!isDebug) {
            String fileName = String.format("receipts/%s", UuidCreator.getRandomBased());
            String receiptUrl = storageGateway.uploadImage(request.receiptImage(), fileName);
            expense.setReceiptUrl(receiptUrl);
        } else {
            expense.setReceiptUrl("receiptUrl.dev");
        }

        try {
            Expense savedExpense = expenseRepository.save(expense);
            expenseRepository.flush();
            expenseProcessor.process(savedExpense);
            return ExpenseResponse.fromEntity(savedExpense);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Expense already exists");
        }
    }

    @Transactional(readOnly = true)
    public ExpenseDetailResponse findById(UUID id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return ExpenseDetailResponse.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findMyExpenses(UUID id) {
        return expenseRepository.findAllByEmployeeId(id).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findPendingExpensesForManager(UUID id) {
        return expenseRepository.findAllByEmployeeManagerIdAndManagerDecisionIsNull(id).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findPendingExpensesForFinance() {
        return expenseRepository.findAllByManagerDecisionAndFinanceDecisionIsNull(
            DecisionType.APPROVED).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findEmployeesExpensesForManager(UUID id) {
        return expenseRepository.findAllByEmployeeManagerId(id).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAllEmployeesExpenses() {
        return expenseRepository.findAll().stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    /**
     * Convert Metadata object to a Map representation.
     * @param imageMetadata The Metadata object containing image metadata.
     * @return A Map where each key is a directory name and the value is another Map of tag names to descriptions.
     */
    private Map<String, Map<String, String>> getImageMetadataMap(Metadata imageMetadata) {
        Map<String, Map<String, String>> cleanedMetadata = new HashMap<>();

        for (Directory directory : imageMetadata.getDirectories()) {
            Map<String, String> tagsMap = new HashMap<>();

            for (Tag tag : directory.getTags()) {
                String tagName = sanitize(tag.getTagName());
                String description = sanitize(tag.getDescription());

                if (tagName != null) {
                    tagsMap.put(tagName, description);
                }
            }

            if (!tagsMap.isEmpty()) {
                cleanedMetadata.put(sanitize(directory.getName()), tagsMap);
            }
        }
        return cleanedMetadata;
    }

    /**
     * Sanitize a string by removing null characters and trimming whitespace.
     * @param value The string to sanitize.
     * @return The sanitized string.
     */
    private String sanitize(String value) {
        if (value == null) return null;
        return value.replace("\u0000", "").trim();
    }

    public void approve(UUID id, AuthenticatedUser user) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Employee employee = employeeRepository.findById(user.id())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        ExpenseState expenseState = expenseStateFactory.from(expense);
        boolean hasPendingAlerts = alertRepository.existsByExpenseId(id);
        expenseState.approve(expense, employee, hasPendingAlerts);
    }

    public void deny(UUID id, AuthenticatedUser user) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Employee employee = employeeRepository.findById(user.id())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        ExpenseState expenseState = expenseStateFactory.from(expense);
        expenseState.reject(expense, employee);
    }

    public List<ExpenseResponse> findApprovedExpenses(ExpenseReportFilterRequest request,
        ExpenseReportType type) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new BusinessRuleException("Date from is after date to");
        }
        return switch (type) {
            case BY_EMPLOYEE -> findByEmployees(request);
            case BY_CATEGORY -> findByCategories(request);
            case BY_DEPARTMENT -> findByDepartments(request);
        };
    }

    private List<ExpenseResponse> findByEmployees(ExpenseReportFilterRequest request) {
        if (request.employeeIds() == null || request.employeeIds().isEmpty()) {
            throw new BusinessRuleException("Employee report requires at least one employeeId");
        }
        return expenseRepository.findAllByEmployeeIdInAndFinanceDecisionAndFinanceDecisionDateBetween(
            request.employeeIds(),
            DecisionType.APPROVED,
            request.dateFrom(),
            request.dateTo()
        ).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    private List<ExpenseResponse> findByCategories(ExpenseReportFilterRequest request) {
        if (request.categories() == null || request.categories().isEmpty()) {
            throw new BusinessRuleException("Expense reports require at least one category");
        }
        return expenseRepository.findAllByCategoryInAndFinanceDecisionAndFinanceDecisionDateBetween(
            request.categories(),
            DecisionType.APPROVED,
            request.dateFrom(),
            request.dateTo()
        ).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    private List<ExpenseResponse> findByDepartments(ExpenseReportFilterRequest request) {
        if (request.departmentNames() == null || request.departmentNames().isEmpty()) {
            throw new BusinessRuleException("Expense reports require at least one department");
        }
        return expenseRepository.findAllByDepartmentNameInAndFinanceDecisionAndFinanceDecisionDateBetween(
            request.departmentNames(),
            DecisionType.APPROVED,
            request.dateFrom(),
            request.dateTo()
        ).stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }
}
