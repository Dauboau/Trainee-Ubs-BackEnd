package com.ubs.ExpenseManager.usecases.auth;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.security.jwt.JwtService;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationResponse;

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthUseCase {

    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private AuthenticationResponse buildAuthenticationResponse(Employee employee) {
        String jwtToken = jwtService.generateToken(employee);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password())
        );
        Employee employee = employeeRepository.findByEmail(request.email())
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuário com email " + request.email() + " não encontrado"));
        return buildAuthenticationResponse(employee);
    }
}
