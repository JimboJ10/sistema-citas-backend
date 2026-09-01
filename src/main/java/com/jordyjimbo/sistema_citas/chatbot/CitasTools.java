package com.jordyjimbo.sistema_citas.chatbot;

import com.jordyjimbo.sistema_citas.dto.*;
import com.jordyjimbo.sistema_citas.service.CitaService;
import com.jordyjimbo.sistema_citas.service.DoctorService;
import com.jordyjimbo.sistema_citas.service.EspecialidadService;
import com.jordyjimbo.sistema_citas.service.HorarioDisponibleService;
import com.jordyjimbo.sistema_citas.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CitasTools {

    private final EspecialidadService especialidadService;
    private final DoctorService doctorService;
    private final HorarioDisponibleService horarioService;
    private final CitaService citaService;
    private final PacienteService pacienteService;

    @Tool(description = "Lista todas las especialidades médicas disponibles en la clínica, con su nombre y descripción")
    public List<EspecialidadResponse> listarEspecialidades() {
        return especialidadService.listarTodas();
    }

    @Tool(description = "Lista todos los doctores registrados en la clínica, incluyendo sus especialidades")
    public List<DoctorResponse> listarDoctores() {
        return doctorService.listarTodos();
    }

    @Tool(description = "Consulta los horarios de atención disponibles de un doctor específico, dado su id")
    public List<HorarioDisponibleResponse> consultarHorariosDeDoctor(
            @ToolParam(description = "El id del doctor cuyo horario se quiere consultar") Long doctorId) {
        return horarioService.listarPorDoctor(doctorId);
    }

    @Tool(description = "Registra un paciente NUEVO (invitado, sin cuenta) usando solo su nombre completo y teléfono, y devuelve su id interno. Úsala SOLO cuando el usuario no está identificado todavía (no se te dio un id de paciente al inicio de la conversación) y quiere agendar una cita: pide su nombre y teléfono en la conversación, nunca le pidas un id.")
    public PacienteResponse registrarPacienteInvitado(
            @ToolParam(description = "Nombres del paciente") String nombres,
            @ToolParam(description = "Apellidos del paciente") String apellidos,
            @ToolParam(description = "Teléfono de contacto del paciente") String telefono) {

        PacienteRequest request = new PacienteRequest(nombres, apellidos, null, telefono, null);
        return pacienteService.crear(request);
    }

    @Tool(description = "Crea una cita médica nueva para un paciente con un doctor, en una fecha y hora específicas. La fecha debe estar en formato ISO (ej: 2026-09-15T09:00:00) y debe caer dentro de un horario disponible del doctor. Si ya conoces el id del paciente (porque te lo dieron al inicio de la conversación, o porque acabas de registrarlo con registrarPacienteInvitado), úsalo directamente sin volver a pedirlo.")
    public CitaResponse crearCita(
            @ToolParam(description = "El id del paciente que agenda la cita") Long pacienteId,
            @ToolParam(description = "El id del doctor con quien se agenda la cita") Long doctorId,
            @ToolParam(description = "Fecha y hora de la cita en formato ISO, ej: 2026-09-15T09:00:00") String fechaHora,
            @ToolParam(description = "Notas u observaciones adicionales sobre la cita, opcional", required = false) String notas) {

        CitaRequest request = new CitaRequest(
                pacienteId,
                doctorId,
                LocalDateTime.parse(fechaHora),
                notas
        );

        return citaService.crear(request);
    }
}