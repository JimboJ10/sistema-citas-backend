package com.jordyjimbo.sistema_citas.dto;

import java.time.LocalDateTime;

public record CitaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombreCompleto,
        Long doctorId,
        String doctorNombreCompleto,
        LocalDateTime fechaHora,
        String estado,
        String notas,
        LocalDateTime creadoEn
) {
}