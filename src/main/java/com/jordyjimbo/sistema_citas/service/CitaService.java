package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.CitaRequest;
import com.jordyjimbo.sistema_citas.dto.CitaResponse;
import com.jordyjimbo.sistema_citas.entity.Cita;
import com.jordyjimbo.sistema_citas.entity.Doctor;
import com.jordyjimbo.sistema_citas.entity.HorarioDisponible;
import com.jordyjimbo.sistema_citas.entity.Paciente;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.exception.RecursoNoEncontradoException;
import com.jordyjimbo.sistema_citas.exception.SolicitudInvalidaException;
import com.jordyjimbo.sistema_citas.repository.CitaRepository;
import com.jordyjimbo.sistema_citas.repository.DoctorRepository;
import com.jordyjimbo.sistema_citas.repository.HorarioDisponibleRepository;
import com.jordyjimbo.sistema_citas.repository.PacienteRepository;
import com.jordyjimbo.sistema_citas.util.DiaSemanaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final HorarioDisponibleRepository horarioRepository;

    public List<CitaResponse> listarPorPaciente(Long pacienteId) {
        return citaRepository.findByPaciente_Id(pacienteId)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public List<CitaResponse> listarPorDoctor(Long doctorId) {
        return citaRepository.findByDoctor_Id(doctorId)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public CitaResponse buscarPorId(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita con id " + id + " no encontrada"));
        return aResponse(cita);
    }

    @Transactional
    public CitaResponse crear(CitaRequest request) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente con id " + request.pacienteId() + " no encontrado"));

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Doctor con id " + request.doctorId() + " no encontrado"));

        validarDentroDeHorario(doctor.getId(), request.fechaHora());
        validarSinDobleReserva(doctor.getId(), request.fechaHora());

        Cita cita = Cita.builder()
                .paciente(paciente)
                .doctor(doctor)
                .fechaHora(request.fechaHora())
                .notas(request.notas())
                .estado(Cita.EstadoCita.PENDIENTE)
                .build();

        return aResponse(citaRepository.save(cita));
    }

    @Transactional
    public CitaResponse cambiarEstado(Long id, Cita.EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita con id " + id + " no encontrada"));

        cita.setEstado(nuevoEstado);
        return aResponse(citaRepository.save(cita));
    }

    private void validarDentroDeHorario(Long doctorId, java.time.LocalDateTime fechaHora) {
        DayOfWeek dia = fechaHora.getDayOfWeek();
        LocalTime hora = fechaHora.toLocalTime();

        List<HorarioDisponible> horarios = horarioRepository.findByDoctor_IdAndDiaSemanaAndActivoTrue(doctorId, dia);

        boolean dentroDeAlgunHorario = horarios.stream()
                .anyMatch(h -> !hora.isBefore(h.getHoraInicio()) && !hora.isAfter(h.getHoraFin()));

        if (!dentroDeAlgunHorario) {
            String diaTexto = DiaSemanaUtil.aTextoLocalizado(dia, Locale.forLanguageTag("es"));
            throw new SolicitudInvalidaException(
                    "El doctor no atiende los " + diaTexto + " a las " + hora + " (fuera de su horario disponible)"
            );
        }
    }

    private void validarSinDobleReserva(Long doctorId, java.time.LocalDateTime fechaHora) {
        if (citaRepository.existsByDoctor_IdAndFechaHora(doctorId, fechaHora)) {
            throw new RecursoDuplicadoException("El doctor ya tiene una cita agendada en ese horario exacto");
        }
    }

    public List<CitaResponse> listarTodas() {
        return citaRepository.findAll()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    private CitaResponse aResponse(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getPaciente().getId(),
                cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos(),
                cita.getDoctor().getId(),
                cita.getDoctor().getNombres() + " " + cita.getDoctor().getApellidos(),
                cita.getFechaHora(),
                cita.getEstado().name(),
                cita.getNotas(),
                cita.getCreadoEn()
        );
    }
}