package com.jordyjimbo.sistema_citas.repository;

import com.jordyjimbo.sistema_citas.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}