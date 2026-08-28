package com.jordyjimbo.sistema_citas.repository;

import com.jordyjimbo.sistema_citas.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
}