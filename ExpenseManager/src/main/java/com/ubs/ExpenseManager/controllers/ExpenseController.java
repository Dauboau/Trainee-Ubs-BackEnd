package com.ubs.ExpenseManager.controllers;

import com.ubs.ExpenseManager.security.auth.AuthenticatedUser;
import com.ubs.ExpenseManager.security.auth.AuthenticatedUserProvider;
import com.ubs.ExpenseManager.usecases.expense.ExpenseUseCase;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseDetailResponse;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseReportFilterRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseReportType;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
})
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Endpoints for expense management")
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create expense", description = "Creates a new expense in the system with "
        + "receipt image")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Expense created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "404", description = "Employee or department not found")
    })
    public ResponseEntity<ExpenseResponse> create(@Valid @ModelAttribute ExpenseRequest request) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseUseCase.create(request, user));
    }

    @GetMapping("/pending/manager")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "List pending expenses for manager",
        description = "Returns all pending expense requests submitted by employees under the "
            + "authenticated manager that are awaiting manager approval"
    )
    @ApiResponse(responseCode = "200", description = "Pending expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponse>> findPendingExpensesForManager() {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        return ResponseEntity.ok(expenseUseCase.findPendingExpensesForManager(user.id()));
    }

    @GetMapping("/pending/finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "List pending expenses for finance",
        description = "Returns all expense requests approved by managers that are awaiting finance "
            + "approval"
    )
    @ApiResponse(responseCode = "200", description = "Pending expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponse>> findPendingExpensesForFinance() {
        return ResponseEntity.ok(expenseUseCase.findPendingExpensesForFinance());
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "List employees expenses for manager",
        description = "Returns all expense requests submitted by employees under the authenticated "
            + "manager"
    )
    @ApiResponse(responseCode = "200", description = "Employees expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponse>> findEmployeesExpensesForManager() {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        return ResponseEntity.ok(expenseUseCase.findEmployeesExpensesForManager(user.id()));
    }

    @GetMapping("/finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "List all employees expenses",
        description = "Returns all expense requests submitted by every employee in the system, "
            + "regardless of status"
    )
    @ApiResponse(responseCode = "200", description = "Employees expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponse>> findAllEmployeesExpenses() {
        return ResponseEntity.ok(expenseUseCase.findAllEmployeesExpenses());
    }

    @GetMapping("/my")
    @Operation(
        summary = "List my expenses",
        description = "Returns all expense requests submitted by the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponse>> findMyExpenses() {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        return ResponseEntity.ok(expenseUseCase.findMyExpenses(user.id()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Get expense by id",
        description = "Returns detailed information about a specific expense"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseUseCase.findById(id));
    }

    @PatchMapping("/{id}/approve/manager")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Approve expense (manager)",
        description = "Approves a pending expense submitted by an employee under "
            + "the authenticated manager"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Expense approved successfully by manager"
        ),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(
            responseCode = "422",
            description = """
                Business rule violation. Possible reasons:
                - The expense is not in PENDING status
                - The expense does not belong to the manager's department
            """
        )
    })
    public ResponseEntity<Void> approveByManager(@PathVariable UUID id) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        expenseUseCase.approve(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve/finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Approve expense (finance)",
        description = "Approves an expense previously approved by a manager"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Expense approved successfully by finance"
        ),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(
            responseCode = "422",
            description = """
            Business rule violation. Possible reasons:
            - The expense is not in APPROVED_BY_MANAGER status
        """
        )
    })
    public ResponseEntity<Void> approveByFinance(@PathVariable UUID id) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        expenseUseCase.approve(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deny/manager")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Deny expense (manager)",
        description = "Denies a pending expense submitted by an employee under the"
            + " authenticated manager"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Expense denied successfully by manager"),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(
            responseCode = "422",
            description = """
                Business rule violation. Possible reasons:
                - The expense is not in PENDING status
                - The expense does not belong to the manager's department
            """
        )
    })
    public ResponseEntity<Void> denyByManager(@PathVariable UUID id) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        expenseUseCase.deny(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deny/finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Deny expense (finance)",
        description = "Denies an expense previously approved by a manager"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Expense denied successfully by finance"),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(
            responseCode = "422",
                description = """
                Business rule violation. Possible reasons:
                - The expense is not in APPROVED_BY_MANAGER status
            """
        )
    })
    public ResponseEntity<Void> denyByFinance(@PathVariable UUID id) {
        AuthenticatedUser user = authenticatedUserProvider.getUser();
        expenseUseCase.deny(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/by-employee")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Generate expense report by employee",
        description = "Generates a report containing all expenses approved by finance for the "
            + "specified employees within a given date range. This endpoint requires at least one "
            + "employee ID and a valid date interval. "
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(
            responseCode = "422",
            description = """
                Business rule violation. Possible reasons:
                - Employee ID list is missing or empty
                - dateFrom is after dateTo
            """
        ),
    })
    public ResponseEntity<List<ExpenseResponse>> findByEmployees(
        @Valid @RequestBody ExpenseReportFilterRequest request) {
        return ResponseEntity.ok(expenseUseCase.findApprovedExpenses(request,
            ExpenseReportType.BY_EMPLOYEE));
    }

    @PostMapping("/reports/by-category")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Generate expense report by category",
        description = "Generates a report containing all expenses approved by finance for the "
            + "specified expense categories within a given date range. This endpoint requires at "
            + "least one expense category and a valid date interval. "
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(
            responseCode = "422",
            description = """
                Business rule violation. Possible reasons:
                - Expense category list is missing or empty
                - dateFrom is after dateTo
            """
        )
    })
    public ResponseEntity<List<ExpenseResponse>> findByCategories(
        @Valid @RequestBody ExpenseReportFilterRequest request) {
        return ResponseEntity.ok(expenseUseCase.findApprovedExpenses(request,
            ExpenseReportType.BY_CATEGORY));
    }

    @PostMapping("/reports/by-department")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(
        summary = "Generate expense report by department",
        description = "Generates a report containing all expenses approved by finance for the "
            + "specified departments within a given date range. This endpoint requires at least one "
            + "department and a valid date interval. "
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(
            responseCode = "422",
            description = """
                Business rule violation. Possible reasons:
                - Department list is missing or empty
                - dateFrom is after dateTo
            """
        )
    })
    public ResponseEntity<List<ExpenseResponse>> findByDepartments(
        @Valid @RequestBody ExpenseReportFilterRequest request) {
        return ResponseEntity.ok(expenseUseCase.findApprovedExpenses(request,
            ExpenseReportType.BY_DEPARTMENT));
    }
}
