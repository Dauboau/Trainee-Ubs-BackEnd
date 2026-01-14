package com.ubs.ExpenseManager.usecases.department;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentDetailedResponse;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.dto.RenameDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.SpendingSettingRequest;
import com.ubs.ExpenseManager.usecases.department.dto.UpdateDepartmentRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            throw new ConflictException("Department already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream().map(DepartmentResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDetailedResponse findByName(String name) {
        return DepartmentDetailedResponse.fromEntity(departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
    }

    public DepartmentDetailedResponse update(String name, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        validateSpendingSettings(request.spendingSettings());

        department.setCurrency(request.currency());
        department.setMonthlyBudget(request.monthlyBudget());
        department.getSpendingSettings().clear();

        if (request.spendingSettings() != null) {
            for (SpendingSettingRequest settingRequest : request.spendingSettings()) {
                SpendingSetting setting = new SpendingSetting(
                    settingRequest.toId(department.getName()),
                    settingRequest.budget()
                );
                setting.setDepartment(department);
                department.getSpendingSettings().add(setting);
            }
        }
        return DepartmentDetailedResponse.fromEntity(departmentRepository.save(department));
    }

    private void validateSpendingSettings(List<SpendingSettingRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        Set<String> setting = new HashSet<>();
        for (SpendingSettingRequest request : requests) {
            String key = request.category() + ":" + request.type();
            if (!setting.add(key)) {
                throw new ConflictException(
                    "There cannot be more than one spending setting with the same category and type"
                );
            }
        }
    }

    public void rename(String name, RenameDepartmentRequest request) {
        if (name.equals(request.newName())) {
            return;
        }
        try {
            int updated = departmentRepository.rename(name, request.newName());
            if (updated == 0) {
                throw new ResourceNotFoundException("Department not found");
            }
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("A department with this name already exists");
        }
    }

    public void delete(String name) {
        int deleted = departmentRepository.deleteByName(name);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Department not found");
        }
    }
}
