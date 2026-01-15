package com.ubs.ExpenseManager.entities.department.repository;

import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;

import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpendingSettingRepository extends
    JpaRepository<SpendingSetting, SpendingSettingId> {
    void deleteAllByDepartment_Name(String departmentName);
    List<SpendingSetting> findByIdDepartmentNameAndIdCategory(String department, ExpenseCategory category);
}
