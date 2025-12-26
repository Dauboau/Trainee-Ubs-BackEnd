package com.ubs.ExpenseManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ubs.ExpenseManager.usecases.department.dto.DepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;
import com.ubs.ExpenseManager.usecases.department.DepartmentUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Endpoints para gerenciamento de departamentos")
public class DepartmentController {

    private final DepartmentUseCase departmentUseCase;

    @PostMapping
    @Operation(summary = "Criar departamento", description = "Cria um novo departamento no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Departamento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DepartmentResponse> create(@RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentUseCase.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar departamentos", description = "Retorna todos os departamentos cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de departamentos retornada com sucesso")
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentUseCase.findAll());
    }

    @GetMapping("/{name}")
    @Operation(summary = "Buscar departamento por nome", description = "Retorna um departamento específico pelo nome")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Departamento encontrado"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<DepartmentResponse> findById(@PathVariable String name) {
        return ResponseEntity.ok(departmentUseCase.findById(name));
    }

    @PutMapping("/{name}")
    @Operation(summary = "Atualizar departamento", description = "Atualiza os dados de um departamento existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Departamento atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DepartmentResponse> update(@PathVariable String name, @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentUseCase.update(name, request));
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Deletar departamento", description = "Remove um departamento do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Departamento deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable String name) {
        departmentUseCase.delete(name);
        return ResponseEntity.noContent().build();
    }
}
