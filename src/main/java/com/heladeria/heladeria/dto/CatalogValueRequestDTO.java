package com.heladeria.heladeria.dto;

import jakarta.validation.constraints.NotBlank;

public record CatalogValueRequestDTO(@NotBlank String name) {}