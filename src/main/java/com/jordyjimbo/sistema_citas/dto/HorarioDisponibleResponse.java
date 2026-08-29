package com.jordyjimbo.sistema_citas.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioDisponibleResponse(
        Long id,
        Long doctorId,
        String doctorNombreCompleto,
        String diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean activo
) {
}