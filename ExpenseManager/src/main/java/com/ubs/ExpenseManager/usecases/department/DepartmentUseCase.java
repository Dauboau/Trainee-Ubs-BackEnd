package com.ubs.ExpenseManager.usecases.department;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.exceptions.ConflictException;
import com.ubs.ExpenseManager.exceptions.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.dto.UpdateDepartmentRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentUseCase {

    private final DepartmentRepository departmentRepository;

    public DepartmentResponse create(CreateDepartmentRequest request) {
        validateNonExistence(request.name());

        Department department = new Department();
        department.setName(request.name());
        department.setCurrency(request.currency());
        department.setMonthlyBudget(BigDecimal.ZERO); // TODO: adicionar constante em utils

        return DepartmentResponse.fromEntity(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
            .map(DepartmentResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(String name) {
        return DepartmentResponse.fromEntity(findByIdOrThrow(name));
    }

    @Transactional
    public DepartmentResponse update(String name, UpdateDepartmentRequest request) {
        validateExistence(name);

        String newName = request.name();
        boolean willRename = !name.equals(newName);

        if (willRename) {
            if (departmentRepository.existsById(newName)) {
                throw new ConflictException("Já existe um departamento com esse nome");
            }

            int updated = departmentRepository.renameDepartment(name, newName);
            // TODO: adicionar constante em utils
            if (updated == 0) {
                throw new ResourceNotFoundException("Departamento não encontrado");
            }
        }

        Department department = findByIdOrThrow(newName);

        department.setCurrency(request.currency());
        department.setMonthlyBudget(request.monthlyBudget());

        return DepartmentResponse.fromEntity(departmentRepository.save(department));
    }

    public void delete(String name) {
        validateExistence(name);
        departmentRepository.deleteById(name);
    }

    private void validateNonExistence(String name) {
        if (departmentRepository.existsById(name)) {
            throw new ConflictException("Já existe um departamento com esse nome");
        }
    }

    private void validateExistence(String name) {
        if (!departmentRepository.existsById(name)) {
            throw new ResourceNotFoundException("Departamento não encontrado");
        }
    }

    private Department findByIdOrThrow(String name) {
        return departmentRepository.findById(name)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento não encontrado"));
    }
}
