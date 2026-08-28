package com.jordyjimbo.sistema_citas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100)
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20)
        String telefono,

        LocalDate fechaNacimiento
) {
}