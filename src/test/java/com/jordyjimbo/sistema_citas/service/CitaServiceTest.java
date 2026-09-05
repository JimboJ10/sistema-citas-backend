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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private HorarioDisponibleRepository horarioRepository;

    @InjectMocks
    private CitaService citaService;

    private Paciente paciente;
    private Doctor doctor;
    private HorarioDisponible horarioLunesManana;

    @BeforeEach
    void setUp() {
        paciente = Paciente.builder()
                .id(1L)
                .nombres("Maria")
                .apellidos("Torres")
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .nombres("Carlos")
                .apellidos("Ramirez")
                .build();

        horarioLunesManana = HorarioDisponible.builder()
                .id(1L)
                .doctor(doctor)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .activo(true)
                .build();
    }

    @Test
    void crear_conDatosValidos_creaLaCitaCorrectamente() {
        LocalDateTime fechaHoraValida = proximoLunesA(LocalTime.of(9, 0));
        CitaRequest request = new CitaRequest(1L, 1L, fechaHoraValida, "Control de rutina");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(horarioRepository.findByDoctor_IdAndDiaSemanaAndActivoTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(horarioLunesManana));
        when(citaRepository.existsByDoctor_IdAndFechaHora(1L, fechaHoraValida)).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        CitaResponse response = citaService.crear(request);

        assertThat(response.pacienteNombreCompleto()).isEqualTo("Maria Torres");
        assertThat(response.doctorNombreCompleto()).isEqualTo("Carlos Ramirez");
        assertThat(response.estado()).isEqualTo("PENDIENTE");
        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    void crear_fueraDelHorarioDelDoctor_lanzaSolicitudInvalida() {
        LocalDateTime fechaHoraFueraDeHorario = proximoLunesA(LocalTime.of(15, 0));
        CitaRequest request = new CitaRequest(1L, 1L, fechaHoraFueraDeHorario, null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(horarioRepository.findByDoctor_IdAndDiaSemanaAndActivoTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(horarioLunesManana));

        assertThatThrownBy(() -> citaService.crear(request))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("no atiende");

        verify(citaRepository, never()).save(any());
    }

    @Test
    void crear_conDobleReserva_lanzaRecursoDuplicado() {
        LocalDateTime fechaHoraValida = proximoLunesA(LocalTime.of(9, 0));
        CitaRequest request = new CitaRequest(1L, 1L, fechaHoraValida, null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(horarioRepository.findByDoctor_IdAndDiaSemanaAndActivoTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(horarioLunesManana));
        when(citaRepository.existsByDoctor_IdAndFechaHora(1L, fechaHoraValida)).thenReturn(true);

        assertThatThrownBy(() -> citaService.crear(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("ya tiene una cita");

        verify(citaRepository, never()).save(any());
    }

    @Test
    void crear_conPacienteInexistente_lanzaRecursoNoEncontrado() {
        CitaRequest request = new CitaRequest(999L, 1L, LocalDateTime.now().plusDays(1), null);

        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citaService.crear(request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Paciente");

        verifyNoInteractions(doctorRepository, horarioRepository);
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_actualizaElEstadoDeLaCita() {
        Cita citaExistente = Cita.builder()
                .id(1L)
                .paciente(paciente)
                .doctor(doctor)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .estado(Cita.EstadoCita.PENDIENTE)
                .build();

        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaExistente));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        CitaResponse response = citaService.cambiarEstado(1L, Cita.EstadoCita.CONFIRMADA);

        assertThat(response.estado()).isEqualTo("CONFIRMADA");
    }

    /**
     * Calcula el próximo lunes a partir de hoy, a la hora indicada.
     * Usamos "el próximo lunes" en vez de una fecha fija para que el test
     * nunca falle por quedar en el pasado con el paso del tiempo.
     */
    private LocalDateTime proximoLunesA(LocalTime hora) {
        LocalDateTime ahora = LocalDateTime.now();
        int diasHastaLunes = (DayOfWeek.MONDAY.getValue() - ahora.getDayOfWeek().getValue() + 7) % 7;
        diasHastaLunes = diasHastaLunes == 0 ? 7 : diasHastaLunes;
        return ahora.plusDays(diasHastaLunes).toLocalDate().atTime(hora);
    }
}