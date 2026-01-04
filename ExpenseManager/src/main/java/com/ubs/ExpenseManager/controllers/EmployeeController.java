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
@Tag(name = "Employees", description = "Endpoints para gerenciamento de funcionários")
public class EmployeeController {

    private final EmployeeUseCase employeeUseCase;

    @PostMapping
    @Operation(summary = "Criar funcionário", description = "Cria um novo funcionário no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeUseCase.create(request));
    }

    @GetMapping
    @Operation(
        summary = "Listar funcionários",
        description = "Retorna todos os funcionários cadastrados"
    )
    @ApiResponse(responseCode = "200", description = "Lista de funcionários retornada com sucesso")
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        return ResponseEntity.ok(employeeUseCase.findAll());
    }

    @GetMapping("/managers")
    @Operation(
        summary = "Listar gerentes",
        description = "Retorna todos os gerentes cadastrados"
    )
    @ApiResponse(responseCode = "200", description = "Lista de gerentes retornada com sucesso")
    public ResponseEntity<List<EmployeeResponse>> findAllManagers() {
        return ResponseEntity.ok(employeeUseCase.findAllManagers());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar funcionário por ID",
        description = "Retorna um funcionário específico pelo ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Funcionário encontrado"),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    public ResponseEntity<EmployeeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeUseCase.findById(id));
    }

    @PostMapping("/realocate")
    @Operation(
        summary = "Realocar subordinados para outro gerente",
        description = "Transfere todos os funcionários de um gerente atual para um novo gerente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subordinados realocados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Gerente não encontrado"),
        @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    public ResponseEntity<Void> reallocateManager(@Valid @RequestBody ManagerReallocationRequest request) {
        employeeUseCase.reallocateManager(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar funcionário", description = "Remove um funcionário do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Funcionário deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
