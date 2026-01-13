package com.ubs.ExpenseManager.usecases.auth;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.exception.BusinessRuleException;
import com.ubs.ExpenseManager.exception.ResourceNotFoundException;
import com.ubs.ExpenseManager.exception.UnauthorizedException;
import com.ubs.ExpenseManager.security.auth.AuthenticatedUserProvider;
import com.ubs.ExpenseManager.security.jwt.JwtService;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationResponse;
import com.ubs.ExpenseManager.usecases.auth.dto.NewPasswordRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeDetailedResponse;
import java.util.UUID;

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthUseCase {

    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private AuthenticationResponse buildAuthenticationResponse(Employee employee) {
        String jwtToken = jwtService.generateToken(employee);
        return new AuthenticationResponse(jwtToken, EmployeeDetailedResponse.fromEntity(employee));
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password())
        );
        Employee employee = employeeRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!employee.getActive()) {
            throw new ResourceNotFoundException("User not found");
        }
        return buildAuthenticationResponse(employee);
    }

    public void changePassword(UUID userId, NewPasswordRequest request) {
        Employee employee = employeeRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean samePasswords = request.currentPassword().equals(request.newPassword());
        if (samePasswords) {
            throw new BusinessRuleException(
                "New password must be different from the current password");
        }

        boolean correctPassword = passwordEncoder.matches(request.currentPassword(),
            employee.getPassword());
        if (!correctPassword) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        employee.setPassword(passwordEncoder.encode(request.newPassword()));
        employeeRepository.save(employee);
    }
}
