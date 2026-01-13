package com.ubs.ExpenseManager.usecases.department;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.exception.BusinessRuleException;
import com.ubs.ExpenseManager.exception.ConflictException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentDetailedResponse;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.dto.RenameDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.SpendingSettingRequest;
import com.ubs.ExpenseManager.usecases.department.dto.UpdateDepartmentRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    public DepartmentDetailedResponse findByName(String name) {
        return DepartmentDetailedResponse.fromEntity(departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
    }

    public DepartmentDetailedResponse update(String name, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(name)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        validateSpendingSettings(request.spendingSettings());

        if (!hasChanges(department, request)) {
            throw new BusinessRuleException("No changes detected to update department");
        }

        department.setCurrency(request.currency());
        department.setMonthlyBudget(request.monthlyBudget());

        spendingSettingRepository.deleteAllByDepartment_Name(name);
        List<SpendingSetting> newList = new ArrayList<>();
        if (request.spendingSettings() != null) {
            for (SpendingSettingRequest settingRequest : request.spendingSettings()) {
                SpendingSetting setting = new SpendingSetting(
                    settingRequest.toId(department.getName()),
                    settingRequest.budget()
                );
                setting.setDepartment(department);
                newList.add(spendingSettingRepository.save(setting));
            }
        }
        department.setSpendingSettings(newList);
        return DepartmentDetailedResponse.fromEntity(department);
    }

    private boolean hasChanges(Department department, UpdateDepartmentRequest request) {
        if (!Objects.equals(department.getCurrency(), request.currency())
            || department.getMonthlyBudget().compareTo(request.monthlyBudget()) != 0) {
            return true;
        }

        Collection<SpendingSetting> current = department.getSpendingSettings();
        List<SpendingSettingRequest> incoming = request.spendingSettings();

        if ((current == null || current.isEmpty()) && (incoming == null || incoming.isEmpty())) {
            return false;
        }

        if (current == null || incoming == null || current.size() != incoming.size()) {
            return true;
        }

        Map<String, BigDecimal> currentMap = current.stream()
            .collect(Collectors.toMap(
                s -> s.getId().getCategory() + ":" + s.getId().getType(),
                SpendingSetting::getBudget
            ));

        for (SpendingSettingRequest spendingSetting : incoming) {
            String key = spendingSetting.category() + ":" + spendingSetting.type();
            BigDecimal oldBudget = currentMap.remove(key);
            if (oldBudget == null || oldBudget.compareTo(spendingSetting.budget()) != 0) {
                return true;
            }
        }

        return !currentMap.isEmpty();
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
