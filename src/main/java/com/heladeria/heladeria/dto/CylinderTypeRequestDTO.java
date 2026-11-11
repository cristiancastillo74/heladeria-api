package com.heladeria.heladeria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CylinderTypeRequestDTO(
        @NotBlank String name,
        @NotNull Double fraction
) {}