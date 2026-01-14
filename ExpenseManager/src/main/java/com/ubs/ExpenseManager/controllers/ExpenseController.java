package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.security.auth.AuthenticatedUser;
import com.ubs.ExpenseManager.security.auth.AuthenticatedUserProvider;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ubs.ExpenseManager.usecases.expense.ExpenseUseCase;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseDetailResponse;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Endpoints for expense management")
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create expense", description = "Creates a new expense in the system with receipt image")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Expense created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "404", description = "Employee or department not found")
    })
    public ResponseEntity<ExpenseResponse> create(@Valid @ModelAttribute ExpenseRequest request) {        
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseUseCase.create(request));
    }

    @GetMapping
    @Operation(summary = "List expenses", description = "Returns all registered expenses")
    @ApiResponse(responseCode = "200", description = "Expense list returned successfully")
    public ResponseEntity<List<ExpenseResponse>> findAll() {
        return ResponseEntity.ok(expenseUseCase.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find expense by ID", description = "Returns a specific expense by ID in detail")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense found"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseUseCase.findById(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        expenseUseCase.approve(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deny")
    public ResponseEntity<Void> deny(@PathVariable UUID id) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        expenseUseCase.deny(id, user);
        return ResponseEntity.noContent().build();
    }

}
