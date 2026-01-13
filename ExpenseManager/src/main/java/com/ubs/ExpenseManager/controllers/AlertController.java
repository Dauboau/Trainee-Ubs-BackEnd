package com.ubs.ExpenseManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @GetMapping("/status/unresolved")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Looks for status NEW (Unresolved)", description = "Return all alerts with status unresolved")
    public ResponseEntity<List<AlertResponse>> findByStatusUnresolved(){
        return ResponseEntity.ok((alertUseCase.findByStatus(AlertStatus.NEW)));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Resolver alerta", description = "Marca um alerta específico como resolvido")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alerta marcado como resolvido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<AlertResponse> resolve(@PathVariable UUID id) {
        return ResponseEntity.ok(alertUseCase.resolve(id));
    }
}
