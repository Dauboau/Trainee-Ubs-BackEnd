package com.ubs.ExpenseManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(
        name = "Alert Management",
        description = "Endpoints for managing expense alerts and notifications. Alerts are automatically generated when expenses exceed budgets or require attention."
)
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertUseCase alertUseCase;

    @GetMapping("/unresolved")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
            summary = "Get all unresolved alerts",
            description = "Retrieves all alerts with NEW status that require attention from finance team members. These alerts represent pending issues that need to be reviewed and addressed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of unresolved alerts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AlertResponse.class),
                            examples = @ExampleObject(
                                    name = "Unresolved alerts example",
                                    value = """
                    [
                        {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "expenseId": "660e8400-e29b-41d4-a716-446655440001",
                            "type": "BUDGET_EXCEEDED",
                            "message": "Monthly budget exceeded by 15% in Marketing category",
                            "status": "NEW",
                            "createdAt": "2026-01-13T14:30:00Z"
                        },
                        {
                            "id": "550e8400-e29b-41d4-a716-446655440002",
                            "expenseId": "660e8400-e29b-41d4-a716-446655440003",
                            "type": "DUPLICATE_EXPENSE",
                            "message": "Potential duplicate expense detected",
                            "status": "NEW",
                            "createdAt": "2026-01-13T10:15:00Z"
                        }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have FINANCE role"
            )
    })
    public ResponseEntity<List<AlertResponse>> findUnresolved() {
        return ResponseEntity.ok(alertUseCase.findByStatus(AlertStatus.NEW));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
            summary = "Resolve an alert",
            description = "Marks a specific alert as resolved by changing its status from NEW to RESOLVED. This action indicates that the finance team has reviewed and addressed the alert."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert successfully marked as resolved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AlertResponse.class),
                            examples = @ExampleObject(
                                    name = "Resolved alert response",
                                    value = """
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "expenseId": "660e8400-e29b-41d4-a716-446655440001",
                        "type": "BUDGET_EXCEEDED",
                        "message": "Departmental Budget Overrun: The requested amount (100000 USD) exceeds the total",
                        "status": "RESOLVED",
                        "createdAt": "2026-01-13T14:30:00Z",
                        "resolvedAt": "2026-01-13T18:22:00Z"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alert not found - No alert exists with the provided ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "error": "Resource Not Found",
                        "message": "Alert Not Found",
                        "timestamp": "2026-01-13T18:22:00Z"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have FINANCE role"
            )
    })
    public ResponseEntity<AlertResponse> resolve(
            @Parameter(
                    description = "UUID of the alert to resolve",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(alertUseCase.resolve(id));
    }
}