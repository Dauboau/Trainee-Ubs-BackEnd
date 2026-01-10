package com.ubs.ExpenseManager.usecases.department;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentDetailedResponse;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.dto.RenameDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.SpendingSettingRequest;
import com.ubs.ExpenseManager.usecases.department.dto.SpendingSettingResponse;
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
    private final SpendingSettingRepository spendingSettingRepository;

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
    public DepartmentDetailedResponse findById(String name) {
        return DepartmentDetailedResponse.fromEntity(departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
    }

    public DepartmentResponse update(String name, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
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
                throw new ResourceNotFoundException("Department not found");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ConflictException("A department with this name already exists");
        }
    }

    public void delete(String name) {
        int deleted = departmentRepository.deleteByName(name);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Department not found");
        }
    }

    public SpendingSettingResponse createSpendingSetting(String name,
        SpendingSettingRequest request) {
        SpendingSettingId settingId = request.toId(name);
        if (spendingSettingRepository.existsById(settingId)) {
            throw new ConflictException("This spending setting already exists");
        }

        SpendingSetting newSetting = new SpendingSetting(settingId, request.budget());

        saveSpendingSetting(newSetting);
        return SpendingSettingResponse.fromEntity(newSetting);
    }

    public SpendingSettingResponse updateSpendingSetting(String name,
        SpendingSettingRequest request) {
        SpendingSettingId id = request.toId(name);
        SpendingSetting existingSetting = spendingSettingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Spending setting not found"));

        existingSetting.setBudget(request.budget());
        saveSpendingSetting(existingSetting);
        return SpendingSettingResponse.fromEntity(existingSetting);
    }

    private void saveSpendingSetting(SpendingSetting setting) {
        try {
            spendingSettingRepository.saveAndFlush(setting);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceNotFoundException("Department not found");
        }
    }

    public List<SpendingSettingResponse> listSpendingSettings(String name) {
        if (!departmentRepository.existsByName(name)) {
            throw new ResourceNotFoundException("Department not found");
        }
        return spendingSettingRepository.findByIdDepartmentName(name)
            .stream()
            .map(SpendingSettingResponse::fromEntity)
            .toList();
    }

    public void deleteSpendingSetting(String name, ExpenseCategory category, SpendingType type) {
        int deleted = spendingSettingRepository.deleteByIdDepartmentNameAndIdCategoryAndIdType(name,
            category, type);
        if (deleted == 0) {
            throw new ResourceNotFoundException(
                "Spending setting not found for the given department, category and type");
        }
    }
}
