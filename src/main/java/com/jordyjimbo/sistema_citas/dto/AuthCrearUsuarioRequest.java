package com.jordyjimbo.sistema_citas.dto;

import com.jordyjimbo.sistema_citas.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthCrearUsuarioRequest(

        @NotBlank(message = "El username es obligatorio")
        @Size(min = 4, max = 50)
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Usuario.Rol rol,

        Long pacienteId,

        Long doctorId
) {
}