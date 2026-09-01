package com.jordyjimbo.sistema_citas.dto;

public record AuthResponse(
        String token,
        String username,
        String rol,
        Long pacienteId,
        Long doctorId
) {
}