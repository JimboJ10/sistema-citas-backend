package com.jordyjimbo.sistema_citas.repository;

import com.jordyjimbo.sistema_citas.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);

    List<Doctor> findByEspecialidades_Id(Long especialidadId);
}