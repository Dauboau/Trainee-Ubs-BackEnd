package com.ubs.ExpenseManager.entities.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByExpenseId(UUID expenseId);
    List<Alert> findByStatus(AlertStatus status);
    List<Alert> findByExpenseIdAndStatus(UUID expenseId, AlertStatus status);
}
