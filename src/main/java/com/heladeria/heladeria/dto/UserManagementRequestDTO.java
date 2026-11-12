package com.heladeria.heladeria.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record UserManagementRequestDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String dui,
        String phone,
        String address,
        BigDecimal salary,
        String workDays,
        @NotBlank String username,
        String password,
        @NotBlank String role,
        Long branchId,
        Boolean canViewInventory
) {}