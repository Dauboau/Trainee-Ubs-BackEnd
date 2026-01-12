package com.ubs.ExpenseManager.usecases.employee;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.exception.BusinessRuleException;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.employee.dto.CreateEmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeResponse;
import com.ubs.ExpenseManager.usecases.employee.dto.UpdateEmployeeRequest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeResponse create(CreateEmployeeRequest request) {
        Department department = getDepartment(request.departmentName());
        Employee manager = getValidManager(request.managerId());

        Employee employee = new Employee();
        employee.setManager(manager);
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setPassword(passwordEncoder.encode(request.password()));
        employee.setDepartment(department);
        employee.setPosition(request.position());
        employee.setRole(request.role());

        try {
            return EmployeeResponse.fromEntity(employeeRepository.saveAndFlush(employee));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(("Email already in use"));
        }
    }

    public EmployeeResponse update(UUID id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department department = getDepartment(request.departmentName());
        Employee manager = getValidManager(request.managerId());

        if (!hasChanges(employee, request, department, manager)) {
            throw new BusinessRuleException("No changes detected to update employee");
        }

        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setManager(manager);
        employee.setDepartment(department);
        employee.setPosition(request.position());

        try {
            return EmployeeResponse.fromEntity(employeeRepository.saveAndFlush(employee));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(("Email already in use"));
        }
    }

    private boolean hasChanges(Employee employee, UpdateEmployeeRequest request,
        Department department, Employee manager) {
        return !Objects.equals(employee.getName(), request.name())
            || !Objects.equals(employee.getEmail(), request.email())
            || !Objects.equals(employee.getManager(), manager)
            || !Objects.equals(employee.getDepartment(), department)
            || !Objects.equals(employee.getPosition(), request.position());
    }

    private Employee getValidManager(UUID managerId) {
        Employee manager = employeeRepository.findByIdAndRole(managerId, Role.MANAGER)
            .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (!manager.getActive()) {
            throw new BusinessRuleException("Manager is not active");
        }
        return manager;
    }

    private Department getDepartment(String name) {
        return departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
            .map(EmployeeResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAllManagers() {
        return employeeRepository.findAllByRole(Role.MANAGER)
            .stream()
            .map(EmployeeResponse::fromEntity)
            .toList();
    }


    public void changeActiveStatus(UUID id, boolean active) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getActive() == active) {
            throw new BusinessRuleException("Employee active status is already set to " + active);
        }

        if (!active) {
            validateManagerDeactivation(employee);
        }

        employee.setActive(active);
        employeeRepository.save(employee);
    }

    private void validateManagerDeactivation(Employee employee) {
        if (employee.getRole() == Role.MANAGER) {
            if (employeeRepository.existsByManagerId(employee.getId())) {
                throw new BusinessRuleException(
                    "The manager cannot be deactivated while they have subordinates. Reallocate them first"
                );
            }
        }
    }
}
