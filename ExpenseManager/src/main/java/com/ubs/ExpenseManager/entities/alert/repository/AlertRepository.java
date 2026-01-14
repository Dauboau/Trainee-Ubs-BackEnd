package com.ubs.ExpenseManager.entities.alert.repository;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertStatus;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    boolean existsByExpenseId(UUID expenseId);
    List<Alert> findByStatus(AlertStatus status);
}
