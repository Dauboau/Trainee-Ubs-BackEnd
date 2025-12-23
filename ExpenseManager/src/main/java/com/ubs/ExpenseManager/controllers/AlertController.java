package com.ubs.ExpenseManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ubs.ExpenseManager.usecases.alert.dto.AlertResponse;
import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import com.ubs.ExpenseManager.entities.alert.enums.AlertStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Endpoints para gerenciamento de alertas")
public class AlertController {

    private final AlertUseCase alertUseCase;

    @GetMapping
    @Operation(summary = "Listar alertas", description = "Retorna todos os alertas do sistema")
    @ApiResponse(responseCode = "200", description = "Lista de alertas retornada com sucesso")
    public ResponseEntity<List<AlertResponse>> findAll() {
        return ResponseEntity.ok(alertUseCase.findAll());
    }

    @GetMapping("/expense/{expenseId}")
    @Operation(summary = "Buscar alertas por despesa", description = "Retorna todos os alertas de uma despesa específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de alertas retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Despesa não encontrada")
    })
    public ResponseEntity<List<AlertResponse>> findByExpenseId(@PathVariable UUID expenseId) {
        return ResponseEntity.ok(alertUseCase.findByExpenseId(expenseId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar alertas por status", description = "Retorna todos os alertas com um status específico (NEW, RESOLVED)")
    @ApiResponse(responseCode = "200", description = "Lista de alertas retornada com sucesso")
    public ResponseEntity<List<AlertResponse>> findByStatus(@PathVariable AlertStatus status) {
        return ResponseEntity.ok(alertUseCase.findByStatus(status));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolver alerta", description = "Marca um alerta específico como resolvido")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alerta marcado como resolvido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<AlertResponse> resolve(@PathVariable UUID id) {
        return ResponseEntity.ok(alertUseCase.resolve(id));
    }
}
