package com.jordyjimbo.sistema_citas.repository;

import com.jordyjimbo.sistema_citas.entity.HorarioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface HorarioDisponibleRepository extends JpaRepository<HorarioDisponible, Long> {

    List<HorarioDisponible> findByDoctor_IdAndDiaSemanaAndActivoTrue(Long doctorId, DayOfWeek diaSemana);

    List<HorarioDisponible> findByDoctor_Id(Long doctorId);
}