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
            .orElseThrow(() -> new ResourceNotFoundException("Departamento não encontrado"));

        Employee manager = employeeRepository.findByIdAndRoleAndActiveTrue(request.managerId(),
                Role.MANAGER)
            .orElseThrow(() -> new ResourceNotFoundException("Gerente não encontrado"));

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
            throw new ConflictException("Email já cadastrado");
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
            .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado"));
    }

    public void reallocateManager(ManagerReallocationRequest request) {
        if (request.currentManagerId().equals(request.newManagerId())) {
            throw new BusinessRuleException("O novo gerente deve ser diferente do atual.");
        }

        Employee current = employeeRepository.findByIdAndActiveTrue(request.currentManagerId())
            .orElseThrow(() -> new ResourceNotFoundException("Gerente atual não encontrado"));

        Employee newManager = employeeRepository.findByIdAndActiveTrue(request.newManagerId())
            .orElseThrow(() -> new ResourceNotFoundException("Novo gerente não encontrado"));

        if (current.getRole() != Role.MANAGER || newManager.getRole() != Role.MANAGER) {
            throw new BusinessRuleException("Ambos devem ter o cargo de gerente.");
        }

        List<Employee> subordinates = employeeRepository.findAllByManagerId(request.currentManagerId());
        if (subordinates.isEmpty()) {
            throw new BusinessRuleException("O gerente atual não possui subordinados para realocar.");
        }

        for (Employee emp : subordinates) {
            emp.setManager(newManager);
        }
        employeeRepository.saveAll(subordinates);
    }

    public void delete(UUID id) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado"));
        if (employee.getRole() == Role.MANAGER) {
            if (employeeRepository.existsByManagerId(employee.getId())) {
                throw new BusinessRuleException("Não é possível desativar o gerente enquanto ele "
                    + "possuir subordinados. Realoque-os antes.");
            }
        }
        employee.setActive(false);
        employeeRepository.save(employee);
    }
}
