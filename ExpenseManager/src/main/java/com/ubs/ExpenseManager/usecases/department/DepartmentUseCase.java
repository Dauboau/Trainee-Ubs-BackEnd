package com.ubs.ExpenseManager.usecases.department;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.dto.RenameDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.UpdateDepartmentRequest;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentUseCase {

    private final DepartmentRepository departmentRepository;

    public DepartmentResponse create(CreateDepartmentRequest request) {
        try {
            departmentRepository.create(request.name(), request.currency().name());
            return DepartmentResponse.fromCreate(request);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Departamento já existente");
        }
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream().map(DepartmentResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(String name) {
        return DepartmentResponse.fromEntity(departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Departamento não encontrado")));
    }

    public DepartmentResponse update(String name, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Departamento não encontrado"));
        department.setCurrency(request.currency());
        department.setMonthlyBudget(request.monthlyBudget());

        return DepartmentResponse.fromEntity(departmentRepository.save(department));
    }

    public void rename(String name, RenameDepartmentRequest request) {
        if (name.equals(request.newName())) {
            return;
        }
        try {
            int updated = departmentRepository.rename(name, request.newName());
            if (updated == 0) {
                throw new ResourceNotFoundException("Departamento não encontrado");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ConflictException("Já existe um departamento com esse nome");
        }
    }

    public void delete(String name) {
        int deleted = departmentRepository.deleteByName(name);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Departamento não encontrado");
        }
    }
}
