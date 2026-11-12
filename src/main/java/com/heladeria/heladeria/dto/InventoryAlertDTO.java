package com.heladeria.heladeria.dto;

public record InventoryAlertDTO(
        String item,
        String branch,
        Double current,
        Number threshold,
        String unit,
        String kind
) {}