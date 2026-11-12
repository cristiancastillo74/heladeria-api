package com.heladeria.heladeria.dto;

import java.math.BigDecimal;

public record UserResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String dui,
        String phone,
        String address,
        BigDecimal salary,
        String workDays,
        String username,
        String role,
        Long branchId,
        String branchName,
        boolean canViewInventory
) {}