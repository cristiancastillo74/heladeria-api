package com.heladeria.heladeria.dto;

import jakarta.validation.constraints.NotBlank;

public record BranchRequestDTO(
        @NotBlank String name,
        String location,
        String phone
) {}