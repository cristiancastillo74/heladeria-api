package com.heladeria.heladeria.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Getter
@Setter
public class RegisterRequestDTO {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String dui;
    private String phone;
    private String address;
    private BigDecimal salary;

    private String workDays;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String role; // ejemplo: "ADMIN_GLOBAL" o "ADMIN_BRANCH"

    private Long branchId; // opcional si la vas a usar
}
