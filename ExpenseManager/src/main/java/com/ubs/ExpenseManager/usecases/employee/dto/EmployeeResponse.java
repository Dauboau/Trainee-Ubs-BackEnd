package com.ubs.ExpenseManager.usecases.employee.dto;

import java.util.UUID;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;

public record EmployeeResponse(
    UUID id,
    String name,
    String email,
    String departmentName,
    Role role
) {
    public static EmployeeResponse fromEntity(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment().getName(),
            employee.getRole()
        );
    }
}