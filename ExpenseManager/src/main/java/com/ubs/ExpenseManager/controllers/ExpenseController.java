package com.ubs.ExpenseManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;
import com.ubs.ExpenseManager.usecases.expense.ExpenseUseCase;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Endpoints para gerenciamento de despesas")
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;

    @PostMapping
    @Operation(summary = "Criar despesa", description = "Cria uma nova despesa no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Despesa criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ExpenseResponse> create(@RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseUseCase.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar despesas", description = "Retorna todas as despesas cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de despesas retornada com sucesso")
    public ResponseEntity<List<ExpenseResponse>> findAll() {
        return ResponseEntity.ok(expenseUseCase.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar despesa por ID", description = "Retorna uma despesa específica pelo ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Despesa encontrada"),
        @ApiResponse(responseCode = "404", description = "Despesa não encontrada")
    })
    public ResponseEntity<ExpenseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseUseCase.findById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Buscar despesas por funcionário", description = "Retorna todas as despesas de um funcionário específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de despesas retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    public ResponseEntity<List<ExpenseResponse>> findByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(expenseUseCase.findByEmployeeId(employeeId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar despesas por status", description = "Retorna todas as despesas com um status específico (PENDING, APPROVED, REJECTED)")
    @ApiResponse(responseCode = "200", description = "Lista de despesas retornada com sucesso")
    public ResponseEntity<List<ExpenseResponse>> findByStatus(@PathVariable ExpenseStatus status) {
        return ResponseEntity.ok(expenseUseCase.findByStatus(status));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprovar despesa", description = "Aprova uma despesa pendente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Despesa aprovada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Despesa não encontrada"),
        @ApiResponse(responseCode = "400", description = "Despesa não pode ser aprovada")
    })
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(expenseUseCase.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rejeitar despesa", description = "Rejeita uma despesa pendente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Despesa rejeitada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Despesa não encontrada"),
        @ApiResponse(responseCode = "400", description = "Despesa não pode ser rejeitada")
    })
    public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(expenseUseCase.reject(id));
    }
}
