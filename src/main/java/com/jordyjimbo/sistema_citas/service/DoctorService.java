package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.DoctorRequest;
import com.jordyjimbo.sistema_citas.dto.DoctorResponse;
import com.jordyjimbo.sistema_citas.dto.EspecialidadResponse;
import com.jordyjimbo.sistema_citas.entity.Doctor;
import com.jordyjimbo.sistema_citas.entity.Especialidad;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.exception.RecursoNoEncontradoException;
import com.jordyjimbo.sistema_citas.repository.DoctorRepository;
import com.jordyjimbo.sistema_citas.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final EspecialidadRepository especialidadRepository;

    public List<DoctorResponse> listarTodos() {
        return doctorRepository.findAll()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public DoctorResponse buscarPorId(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Doctor con id " + id + " no encontrado"));
        return aResponse(doctor);
    }

    @Transactional
    public DoctorResponse crear(DoctorRequest request) {
        if (doctorRepository.findByEmail(request.email()).isPresent()) {
            throw new RecursoDuplicadoException("Ya existe un doctor con el email '" + request.email() + "'");
        }

        Set<Especialidad> especialidades = buscarEspecialidades(request.especialidadIds());

        Doctor doctor = Doctor.builder()
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .email(request.email())
                .telefono(request.telefono())
                .especialidades(especialidades)
                .build();

        return aResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorResponse actualizar(Long id, DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Doctor con id " + id + " no encontrado"));

        Set<Especialidad> especialidades = buscarEspecialidades(request.especialidadIds());

        doctor.setNombres(request.nombres());
        doctor.setApellidos(request.apellidos());
        doctor.setEmail(request.email());
        doctor.setTelefono(request.telefono());
        doctor.setEspecialidades(especialidades);

        return aResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Doctor con id " + id + " no encontrado");
        }
        doctorRepository.deleteById(id);
    }

    private Set<Especialidad> buscarEspecialidades(Set<Long> ids) {
        Set<Especialidad> especialidades = new java.util.HashSet<>(especialidadRepository.findAllById(ids));

        if (especialidades.size() != ids.size()) {
            throw new RecursoNoEncontradoException("Una o más especialidades no existen");
        }

        return especialidades;
    }

    private DoctorResponse aResponse(Doctor doctor) {
        Set<EspecialidadResponse> especialidades = doctor.getEspecialidades()
                .stream()
                .map(e -> new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion()))
                .collect(Collectors.toSet());

        return new DoctorResponse(
                doctor.getId(),
                doctor.getNombres(),
                doctor.getApellidos(),
                doctor.getEmail(),
                doctor.getTelefono(),
                especialidades
        );
    }
}