package com.ubs.ExpenseManager.entities.department.repository;

import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpendingSettingRepository extends
    JpaRepository<SpendingSetting, SpendingSettingId> {

    void deleteAllByDepartment_Name(String departmentName);
}
