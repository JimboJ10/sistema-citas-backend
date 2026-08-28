package com.jordyjimbo.sistema_citas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long id,
        String nombres,
        String apellidos,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        boolean registrado,
        LocalDateTime creadoEn
) {
}