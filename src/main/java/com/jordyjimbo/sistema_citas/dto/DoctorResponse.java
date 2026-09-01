package com.jordyjimbo.sistema_citas.dto;

import java.util.Set;

public record DoctorResponse(
        Long id,
        String nombres,
        String apellidos,
        String email,
        String telefono,
        Set<EspecialidadResponse> especialidades,
        boolean tieneAcceso
) {
}