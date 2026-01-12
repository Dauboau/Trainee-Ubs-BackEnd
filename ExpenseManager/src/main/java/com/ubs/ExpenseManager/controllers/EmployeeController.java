package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.usecases.employee.EmployeeUseCase;
import com.ubs.ExpenseManager.usecases.employee.dto.CreateEmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeResponse;
import com.ubs.ExpenseManager.usecases.employee.dto.UpdateEmployeeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/employees")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
})
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeUseCase employeeUseCase;

    @PostMapping
    @Operation(summary = "Create employee", description = "Creates a new employee in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeUseCase.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Email already in use"),
        @ApiResponse(
            responseCode = "422",
            description = "Employee cannot be deactivated because they manage other employees"
        )
    })
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID id,
        @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeUseCase.update(id, request));
    }

    @GetMapping
    @Operation(
        summary = "List employees",
        description = "Returns a list of all active registered employees"
    )
    @ApiResponse(responseCode = "200", description = "Employee list retrieved successfully")
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        return ResponseEntity.ok(employeeUseCase.findAll());
    }

    @GetMapping("/managers")
    @Operation(
        summary = "List managers",
        description = "Returns a list of all active registered managers"
    )
    @ApiResponse(responseCode = "200", description = "Manager list retrieved successfully")
    public ResponseEntity<List<EmployeeResponse>> findAllManagers() {
        return ResponseEntity.ok(employeeUseCase.findAllManagers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Returns a specific employee by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeUseCase.findById(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Activate employee",
        description = "Activates an inactive employee, allowing access to the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee activated successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        employeeUseCase.changeActiveStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
        summary = "Deactivate employee",
        description = "Deactivates an employee, removing their access to the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(
            responseCode = "422",
            description = "Employee cannot be deactivated because they manage other employees"
        )
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        employeeUseCase.changeActiveStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}
