package com.ubs.ExpenseManager.security.auth;

import com.ubs.ExpenseManager.entities.employee.Employee;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

    public AuthenticatedUser getUser() {
        Employee employee = (Employee) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        return new AuthenticatedUser(
            employee.getId(),
            employee.getEmail(),
            employee.getRole(),
            employee.getDepartment().getName()
        );
    }
}
