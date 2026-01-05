package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.usecases.auth.AuthUseCase;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    @Operation(
        summary = "Realizar login",
        description = "Autentica o usuário e retorna um token JWT"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthenticationResponse> login(
        @Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }
}
