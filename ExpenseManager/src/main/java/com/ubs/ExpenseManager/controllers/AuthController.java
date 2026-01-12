package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.security.auth.AuthenticatedUser;
import com.ubs.ExpenseManager.security.auth.AuthenticatedUserProvider;
import com.ubs.ExpenseManager.usecases.auth.AuthUseCase;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationResponse;

import com.ubs.ExpenseManager.usecases.auth.dto.NewPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final AuthenticatedUserProvider userProvider;

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates the user and returns a JWT token along with the "
            + "authenticated user data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthenticationResponse> login(
        @Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Request password changing")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password changed successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication failed"),
        @ApiResponse(responseCode = "422", description = "Password does not meet the requirements")
    })
    public ResponseEntity<Void> changePassword(@RequestBody @Valid NewPasswordRequest password) {
        AuthenticatedUser user = userProvider.getUser();
        authUseCase.changePassword(user.id(), password);
        return ResponseEntity.noContent().build();
    }
}
