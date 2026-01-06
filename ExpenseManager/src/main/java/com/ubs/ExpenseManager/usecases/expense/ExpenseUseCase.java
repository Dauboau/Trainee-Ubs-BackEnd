package com.ubs.ExpenseManager.usecases.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseDetailResponse;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.usecases.currency.CurrencyConverter;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.exceptions.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrencyConverter currencyConverter;

    public ExpenseResponse create(ExpenseRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(request.departmentName())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // Processar imagem e extrair metadados
        Map<String, Object> receiptMetadata = extractReceiptMetadata(request.receiptImage());
        String receiptUrl = "https://storage.example.com/receipts/" + UUID.randomUUID().toString(); // Hardcoded temporariamente

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency());
        expense.setCategory(request.category());
        expense.setDate(request.expenseDate());
        expense.setReceiptUrl(receiptUrl);
        expense.setReceiptMetadata(receiptMetadata);

        java.math.BigDecimal rate = currencyConverter.getExchangeRate(request.currency(), department.getCurrency());
        expense.setExchangeRate(rate);

        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }

    private Map<String, Object> extractReceiptMetadata(MultipartFile receiptImage) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (receiptImage != null && !receiptImage.isEmpty()) {
            metadata.put("originalFilename", receiptImage.getOriginalFilename());
            metadata.put("contentType", receiptImage.getContentType());
            metadata.put("size", receiptImage.getSize());
            metadata.put("uploadedAt", java.time.Instant.now().toString());
            
            // Extrair extensão do arquivo
            String filename = receiptImage.getOriginalFilename();
            if (filename != null && filename.contains(".")) {
                String extension = filename.substring(filename.lastIndexOf("."));
                metadata.put("fileExtension", extension);
            }
        }
        
        return metadata;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAll() {
        return expenseRepository.findAll().stream()
            .map(ExpenseResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseDetailResponse findById(UUID id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return ExpenseDetailResponse.fromEntity(expense);
    }

}
