package com.jordyjimbo.sistema_citas.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioDisponibleRequest(

        @NotNull(message = "El id del doctor es obligatorio")
        Long doctorId,

        @NotNull(message = "El día de la semana es obligatorio")
        String diaSemana,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin
) {
}