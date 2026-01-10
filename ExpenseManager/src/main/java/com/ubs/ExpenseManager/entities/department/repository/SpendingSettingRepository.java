package com.ubs.ExpenseManager.entities.department.repository;

import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

import java.util.List;

import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpendingSettingRepository extends
    JpaRepository<SpendingSetting, SpendingSettingId> {

    List<SpendingSetting> findByIdDepartmentName(String departmentName);

    int deleteByIdDepartmentNameAndIdCategoryAndIdType(String name, ExpenseCategory category,
        SpendingType type);
    List<SpendingSetting> findByIdDepartmentNameAndIdCategory(String department, ExpenseCategory category);
}
