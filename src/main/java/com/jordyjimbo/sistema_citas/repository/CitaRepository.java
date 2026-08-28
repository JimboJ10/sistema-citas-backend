package com.jordyjimbo.sistema_citas.repository;

import com.jordyjimbo.sistema_citas.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPaciente_Id(Long pacienteId);

    List<Cita> findByDoctor_Id(Long doctorId);

    List<Cita> findByDoctor_IdAndFechaHoraBetween(Long doctorId, LocalDateTime desde, LocalDateTime hasta);

    boolean existsByDoctor_IdAndFechaHora(Long doctorId, LocalDateTime fechaHora);
}