package com.jordyjimbo.sistema_citas;

import com.jordyjimbo.sistema_citas.dto.CitaRequest;
import com.jordyjimbo.sistema_citas.dto.CitaResponse;
import com.jordyjimbo.sistema_citas.entity.Doctor;
import com.jordyjimbo.sistema_citas.entity.HorarioDisponible;
import com.jordyjimbo.sistema_citas.entity.Paciente;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.exception.SolicitudInvalidaException;
import com.jordyjimbo.sistema_citas.repository.DoctorRepository;
import com.jordyjimbo.sistema_citas.repository.HorarioDisponibleRepository;
import com.jordyjimbo.sistema_citas.repository.PacienteRepository;
import com.jordyjimbo.sistema_citas.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración: usa un PostgreSQL real (en un contenedor Docker
 * temporal via Testcontainers) en vez de mocks, para verificar que las
 * entidades, relaciones JPA y consultas derivadas funcionan correctamente
 * en conjunto, no solo en aislamiento.
 */
@SpringBootTest
@Testcontainers
class CitaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("sistema_citas_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Sobreescribimos el secreto de JWT para no depender de la variable
        // de entorno real en el entorno de test.
        registry.add("app.jwt.secret", () -> "clave-secreta-de-prueba-para-tests-de-integracion-32-caracteres");
    }

    @Autowired
    private CitaService citaService;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private HorarioDisponibleRepository horarioRepository;

    private Long pacienteId;
    private Long doctorId;

    @BeforeEach
    void setUp() {
        Paciente paciente = pacienteRepository.save(
                Paciente.builder()
                        .nombres("Integración")
                        .apellidos("Test")
                        .telefono("0999999999")
                        .registrado(false)
                        .build()
        );
        pacienteId = paciente.getId();

        Doctor doctor = doctorRepository.save(
                Doctor.builder()
                        .nombres("Doctor")
                        .apellidos("De Prueba")
                        .email("doctor.prueba+" + System.nanoTime() + "@test.com")
                        .build()
        );
        doctorId = doctor.getId();

        horarioRepository.save(
                HorarioDisponible.builder()
                        .doctor(doctor)
                        .diaSemana(DayOfWeek.MONDAY)
                        .horaInicio(LocalTime.of(8, 0))
                        .horaFin(LocalTime.of(12, 0))
                        .activo(true)
                        .build()
        );
    }

    @Test
    void crearCita_persisteCorrectamenteConSusRelaciones() {
        LocalDateTime fechaHoraValida = proximoLunesA(LocalTime.of(9, 0));
        CitaRequest request = new CitaRequest(pacienteId, doctorId, fechaHoraValida, "Consulta de integración");

        CitaResponse response = citaService.crear(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.pacienteNombreCompleto()).isEqualTo("Integración Test");
        assertThat(response.doctorNombreCompleto()).isEqualTo("Doctor De Prueba");
        assertThat(response.estado()).isEqualTo("PENDIENTE");
    }

    @Test
    void crearCita_conDobleReservaReal_lanzaExcepcion() {
        LocalDateTime fechaHoraValida = proximoLunesA(LocalTime.of(10, 0));
        CitaRequest primeraCita = new CitaRequest(pacienteId, doctorId, fechaHoraValida, "Primera");

        citaService.crear(primeraCita);

        CitaRequest citaDuplicada = new CitaRequest(pacienteId, doctorId, fechaHoraValida, "Segunda");

        assertThatThrownBy(() -> citaService.crear(citaDuplicada))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void crearCita_fueraDeHorarioReal_lanzaExcepcion() {
        LocalDateTime fueraDeHorario = proximoLunesA(LocalTime.of(20, 0));
        CitaRequest request = new CitaRequest(pacienteId, doctorId, fueraDeHorario, null);

        assertThatThrownBy(() -> citaService.crear(request))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    private LocalDateTime proximoLunesA(LocalTime hora) {
        LocalDateTime ahora = LocalDateTime.now();
        int diasHastaLunes = (DayOfWeek.MONDAY.getValue() - ahora.getDayOfWeek().getValue() + 7) % 7;
        diasHastaLunes = diasHastaLunes == 0 ? 7 : diasHastaLunes;
        return ahora.plusDays(diasHastaLunes).toLocalDate().atTime(hora);
    }
}