package com.heladeria.heladeria.dto;

import jakarta.validation.constraints.NotNull;

public record ThresholdUpdateDTO(@NotNull Double threshold) {}