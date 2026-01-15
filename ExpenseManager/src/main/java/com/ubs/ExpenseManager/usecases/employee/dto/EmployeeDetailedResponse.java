package com.ubs.ExpenseManager.usecases.employee.dto;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;

import java.util.UUID;

public record EmployeeDetailedResponse(
    UUID id,
    String name,
    String email,
    String departmentName,
    Role role,
    String position,
    Boolean active,
    Boolean firstTime,
    EmployeeResponse manager
) {
    public static EmployeeDetailedResponse fromEntity(Employee employee) {
        return new EmployeeDetailedResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment().getName(),
            employee.getRole(),
            employee.getPosition(),
            employee.getActive(),
            employee.getFirstTime(),
            employee.getManager() != null ? EmployeeResponse.fromEntity(employee.getManager()): null
        );
    }
}