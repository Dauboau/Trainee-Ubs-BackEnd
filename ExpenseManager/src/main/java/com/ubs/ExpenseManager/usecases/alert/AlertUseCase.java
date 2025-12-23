package com.ubs.ExpenseManager.usecases.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ubs.ExpenseManager.usecases.alert.dto.AlertResponse;
import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.alert.repository.AlertRepository;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertUseCase {

    private final AlertRepository alertRepository;
    private final DepartmentRepository departmentRepository;

    public AlertResponse create(Long departmentId, AlertType type, String message) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado"));

        Alert alert = new Alert();
        alert.setDepartment(department);
        alert.setType(type);
        alert.setMessage(message);

        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll() {
        return alertRepository.findAll().stream()
            .map(AlertResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findByDepartmentId(Long departmentId) {
        return alertRepository.findByDepartmentId(departmentId).stream()
            .map(AlertResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findUnread() {
        return alertRepository.findByIsReadFalse().stream()
            .map(AlertResponse::fromEntity)
            .toList();
    }

    public AlertResponse markAsRead(Long id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado"));

        alert.setIsRead(true);
        return AlertResponse.fromEntity(alertRepository.save(alert));
    }
}
