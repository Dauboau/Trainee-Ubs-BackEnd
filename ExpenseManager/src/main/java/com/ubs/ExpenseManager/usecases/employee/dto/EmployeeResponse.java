package com.ubs.ExpenseManager.usecases.employee.dto;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;

public record EmployeeResponse(
    Long id,
    String name,
    String email,
    Long departmentId,
    String departmentName,
    Role role
) {
    public static EmployeeResponse fromEntity(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment() != null ? employee.getDepartment().getId() : null,
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            employee.getRole()
        );
    }
}
