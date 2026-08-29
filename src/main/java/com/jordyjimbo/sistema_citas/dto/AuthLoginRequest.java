package com.jordyjimbo.sistema_citas.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(

        @NotBlank(message = "El username es obligatorio")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}