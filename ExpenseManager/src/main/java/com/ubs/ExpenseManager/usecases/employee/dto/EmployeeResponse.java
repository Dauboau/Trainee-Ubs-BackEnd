package com.ubs.ExpenseManager.usecases.employee.dto;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;

import java.util.UUID;

public record EmployeeResponse(
    UUID id,
    String name,
    String email,
    String departmentName,
    Role role,
    Boolean active
) {
    public static EmployeeResponse fromEntity(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment().getName(),
            employee.getRole(),
            employee.getActive()
        );
    }
}