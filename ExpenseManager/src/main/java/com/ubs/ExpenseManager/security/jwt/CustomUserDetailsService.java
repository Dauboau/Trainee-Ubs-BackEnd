package com.ubs.ExpenseManager.security.jwt;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;

import lombok.AllArgsConstructor;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee loadUserByUsername(String email) throws UsernameNotFoundException {
        return employeeRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuário com email " + email + " não encontrado"));
    }
}
