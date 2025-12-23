package com.ubs.ExpenseManager.entities.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ubs.ExpenseManager.entities.alert.Alert;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByDepartmentId(Long departmentId);
    List<Alert> findByIsReadFalse();
    List<Alert> findByDepartmentIdAndIsReadFalse(Long departmentId);
}
