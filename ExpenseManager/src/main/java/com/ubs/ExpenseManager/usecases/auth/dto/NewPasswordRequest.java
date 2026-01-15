package com.ubs.ExpenseManager.usecases.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NewPasswordRequest(
    @NotBlank
    String currentPassword,

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$",
        message = "Password must be between 8 and 64 characters long and include at least one "
            + "uppercase letter, one lowercase letter, one number, and one special character."
    )
    String newPassword
) {}
