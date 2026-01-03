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
@Tag(name = "Departments", description = "Endpoints para gerenciamento de departamentos")
public class DepartmentController {

    private final DepartmentUseCase departmentUseCase;

    @PostMapping
    @Operation(summary = "Criar departamento", description = "Cria um novo departamento no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Departamento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "409", description = "Departamento já existe")
    })
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentUseCase.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar departamentos", description = "Retorna todos os departamentos cadastrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de departamentos retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentUseCase.findAll());
    }

    @GetMapping("/{name}")
    @Operation(summary = "Buscar departamento por nome", description = "Retorna um departamento específico pelo nome")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Departamento encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<DepartmentResponse> findById(@PathVariable String name) {
        return ResponseEntity.ok(departmentUseCase.findById(name));
    }

    @PutMapping("/{name}")
    @Operation(
            summary = "Atualizar departamento",
            description = """
                    Atualiza os dados de um departamento existente
                    
                    <b>Observação:</b><br>
                    O campo <code>name</code> <b>NÃO</b> é alterado por este endpoint.<br>
                    Para renomear um departamento, utilize o endpoint
                    <b><code>PATCH /api/departments/{name}/name</code></b>.
                    """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Departamento atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Departamento já existe")
    })
    public ResponseEntity<DepartmentResponse> update(@PathVariable String name,
        @Valid @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(departmentUseCase.update(name, request));
    }

    @PatchMapping("{name}/name")
    @Operation(
            summary = "Atualizar nome de departamento",
            description = """
                    Atualiza apenas o nome de um departamento
                    
                    <b>Observação:</b><br>
                    Para alterar os demais dados de um departamento, utilize o endpoint
                    <b><code>PUT /api/departments/{name}</code></b>.
                    """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Nome do departamento atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Departamento já existe")
    })
    public ResponseEntity<Void> rename(@PathVariable String name, @Valid @RequestBody RenameDepartmentRequest request) {
        departmentUseCase.rename(name, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Deletar departamento", description = "Remove um departamento do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Departamento deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable String name) {
        departmentUseCase.delete(name);
        return ResponseEntity.noContent().build();
    }
}
