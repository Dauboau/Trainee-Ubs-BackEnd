package com.ubs.ExpenseManager.usecases.employee;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.exception.BusinessRuleException;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeResponse;
import com.ubs.ExpenseManager.usecases.employee.dto.ManagerReallocationRequest;

import java.util.List;
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

    public EmployeeResponse create(EmployeeRequest request) {

        Department department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Employee manager = employeeRepository.findByIdAndRoleAndActiveTrue(request.managerId(),
                Role.MANAGER).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

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

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAllByActiveTrue().stream()
            .map(EmployeeResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAllManagers() {
        return employeeRepository.findAllByActiveTrueAndRole(Role.MANAGER)
            .stream()
            .map(EmployeeResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID id) {
        return employeeRepository.findByIdAndActiveTrue(id)
            .map(EmployeeResponse::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    public void reallocateManager(ManagerReallocationRequest request) {
        if (request.currentManagerId().equals(request.newManagerId())) {
            throw new BusinessRuleException("The new manager must be different from the "
                + "current one");
        }

        Employee current = employeeRepository.findByIdAndActiveTrue(request.currentManagerId())
            .orElseThrow(() -> new ResourceNotFoundException("Current manager not found"));

        Employee newManager = employeeRepository.findByIdAndActiveTrue(request.newManagerId())
            .orElseThrow(() -> new ResourceNotFoundException("New manager not found"));

        if (current.getRole() != Role.MANAGER || newManager.getRole() != Role.MANAGER) {
            throw new BusinessRuleException("Both employees must have the manager role");
        }

        List<Employee> subordinates = employeeRepository.findAllByManagerId(
            request.currentManagerId());
        if (subordinates.isEmpty()) {
            throw new BusinessRuleException("The current manager has no subordinates "
                + "to reallocate");
        }

        for (Employee emp : subordinates) {
            emp.setManager(newManager);
        }
        employeeRepository.saveAll(subordinates);
    }

    public void delete(UUID id) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (employee.getRole() == Role.MANAGER) {
            if (employeeRepository.existsByManagerId(employee.getId())) {
                throw new BusinessRuleException("The manager cannot be deactivated while they have "
                    + "subordinates. Reallocate them first");
            }
        }
        employee.setActive(false);
        employeeRepository.save(employee);
    }
}
