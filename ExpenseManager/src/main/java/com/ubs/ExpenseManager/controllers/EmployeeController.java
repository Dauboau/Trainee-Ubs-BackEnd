package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.usecases.employee.EmployeeUseCase;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeResponse;
import com.ubs.ExpenseManager.usecases.employee.dto.ManagerReallocationRequest;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
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
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeUseCase.create(request));
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

    @PostMapping("/reallocate")
    @Operation(
        summary = "Reallocate subordinates to another manager",
        description = "Transfers all employees from the current manager to a new manager"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subordinates reallocated successfully"),
        @ApiResponse(responseCode = "404", description = "Manager not found"),
        @ApiResponse(
            responseCode = "422",
            description = "Business rule violation during manager reallocation"
        )
    })
    public ResponseEntity<Void> reallocateManager(
        @Valid @RequestBody ManagerReallocationRequest request) {
        employeeUseCase.reallocateManager(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Removes an employee from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(
            responseCode = "422",
            description = "Employee cannot be deleted because they manage other employees"
        )
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
