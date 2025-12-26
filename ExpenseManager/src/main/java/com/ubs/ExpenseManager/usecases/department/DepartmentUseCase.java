package com.ubs.ExpenseManager.usecases.department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ubs.ExpenseManager.usecases.department.dto.DepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentUseCase {

    private final DepartmentRepository departmentRepository;

    public DepartmentResponse create(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        department.setMonthlyBudget(request.monthlyBudget());

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
        return departmentRepository.findById(name)
            .map(DepartmentResponse::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado"));
    }

    public DepartmentResponse update(String name, DepartmentRequest request) {
        Department department = departmentRepository.findById(name)
            .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado"));

        department.setName(request.name());
        department.setMonthlyBudget(request.monthlyBudget());

        return DepartmentResponse.fromEntity(departmentRepository.save(department));
    }

    public void delete(String name) {
        if (!departmentRepository.existsById(name)) {
            throw new IllegalArgumentException("Departamento não encontrado");
        }
        departmentRepository.deleteById(name);
    }
}
