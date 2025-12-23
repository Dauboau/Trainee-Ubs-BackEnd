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

import java.util.List;

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

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Buscar alertas por departamento", description = "Retorna todos os alertas de um departamento específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de alertas retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<List<AlertResponse>> findByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(alertUseCase.findByDepartmentId(departmentId));
    }

    @GetMapping("/unread")
    @Operation(summary = "Buscar alertas não lidos", description = "Retorna todos os alertas que ainda não foram lidos")
    @ApiResponse(responseCode = "200", description = "Lista de alertas não lidos retornada com sucesso")
    public ResponseEntity<List<AlertResponse>> findUnread() {
        return ResponseEntity.ok(alertUseCase.findUnread());
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar alerta como lido", description = "Marca um alerta específico como lido")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alerta marcado como lido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<AlertResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(alertUseCase.markAsRead(id));
    }
}
