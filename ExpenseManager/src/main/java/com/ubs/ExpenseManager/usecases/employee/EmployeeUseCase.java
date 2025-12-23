package com.ubs.ExpenseManager.usecases.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeResponse;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado"));
        }

        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setPasswordHash(request.password()); // TODO: Hash password
        employee.setDepartment(department);
        employee.setRole(request.role());

        return EmployeeResponse.fromEntity(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
            .map(EmployeeResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return employeeRepository.findById(id)
            .map(EmployeeResponse::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
    }

    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }
        employeeRepository.deleteById(id);
    }
}
