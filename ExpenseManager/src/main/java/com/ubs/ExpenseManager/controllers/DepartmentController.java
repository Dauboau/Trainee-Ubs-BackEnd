package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.usecases.department.DepartmentUseCase;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.dto.RenameDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.UpdateDepartmentRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentUseCase departmentUseCase;

    @PostMapping
    @Operation(
        summary = "Create department",
        description = "Creates a new department in the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Department created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "409", description = "Department already exists")
    })
    public ResponseEntity<DepartmentResponse> create(
        @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentUseCase.create(request));
    }

    @GetMapping
    @Operation(summary = "List departments", description = "Returns all registered departments")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department list retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentUseCase.findAll());
    }

    @GetMapping("/{name}")
    @Operation(
        summary = "Get department by name",
        description = "Returns a specific department by name"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentResponse> findById(@PathVariable String name) {
        return ResponseEntity.ok(departmentUseCase.findById(name));
    }

    @PutMapping("/{name}")
    @Operation(
        summary = "Update department",
        description = """
            Updates the data of an existing department

            <b>Note:</b><br>
            The <code>name</code> field is <b>NOT</b> updated by this endpoint.<br>
            To rename a department, use the endpoint
            <b><code>PATCH /api/departments/{name}/name</code></b>.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department already exists")
    })
    public ResponseEntity<DepartmentResponse> update(@PathVariable String name,
        @Valid @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(departmentUseCase.update(name, request));
    }

    @PatchMapping("/{name}/name")
    @Operation(
        summary = "Update department name",
        description = """
            Updates only the name of a department

            <b>Note:</b><br>
            To update other department data, use the endpoint
            <b><code>PUT /api/departments/{name}</code></b>.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Department name updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department already exists")
    })
    public ResponseEntity<Void> rename(@PathVariable String name,
        @Valid @RequestBody RenameDepartmentRequest request) {
        departmentUseCase.rename(name, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Delete department", description = "Removes a department from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<Void> delete(@PathVariable String name) {
        departmentUseCase.delete(name);
        return ResponseEntity.noContent().build();
    }
}
