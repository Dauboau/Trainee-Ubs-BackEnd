package com.ubs.ExpenseManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseDetailResponse;
import com.ubs.ExpenseManager.usecases.expense.ExpenseUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Endpoints for expense management")
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;

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

}
