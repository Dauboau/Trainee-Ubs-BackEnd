package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.usecases.department.DepartmentUseCase;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentDetailedResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
})
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentUseCase departmentUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create department",
        description = "Creates a new department in the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Department created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Department already exists")
    })
    public ResponseEntity<DepartmentResponse> create(
        @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentUseCase.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "List departments", description = "Returns all registered departments")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department list retrieved successfully")
    })
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentUseCase.findAll());
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Get department by name",
        description = "Returns a specific department by name"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDetailedResponse> findByName(@PathVariable String name) {
        return ResponseEntity.ok(departmentUseCase.findByName(name));
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('FINANCE')")
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
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Conflicting department data")
    })
    public ResponseEntity<DepartmentDetailedResponse> update(@PathVariable String name,
        @Valid @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(departmentUseCase.update(name, request));
    }

    @PatchMapping("/{name}/name")
    @PreAuthorize("hasRole('FINANCE')")
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
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department already exists")
    })
    public ResponseEntity<Void> rename(@PathVariable String name,
        @Valid @RequestBody RenameDepartmentRequest request) {
        departmentUseCase.rename(name, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department", description = "Removes a department from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<Void> delete(@PathVariable String name) {
        departmentUseCase.delete(name);
        return ResponseEntity.noContent().build();
    }
}
