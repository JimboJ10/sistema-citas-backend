package com.jordyjimbo.sistema_citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CitaRequest(

        @NotNull(message = "El id del paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El id del doctor es obligatorio")
        Long doctorId,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La fecha de la cita debe ser en el futuro")
        LocalDateTime fechaHora,

        @Size(max = 500)
        String notas
) {
}