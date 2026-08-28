package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.PacienteRequest;
import com.jordyjimbo.sistema_citas.dto.PacienteResponse;
import com.jordyjimbo.sistema_citas.entity.Paciente;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.exception.RecursoNoEncontradoException;
import com.jordyjimbo.sistema_citas.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public List<PacienteResponse> listarTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public PacienteResponse buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente con id " + id + " no encontrado"));
        return aResponse(paciente);
    }

    @Transactional
    public PacienteResponse crear(PacienteRequest request) {
        if (StringUtils.hasText(request.email())
                && pacienteRepository.existsByEmailIgnoreCase(request.email())) {
            throw new RecursoDuplicadoException("Ya existe un paciente con el email '" + request.email() + "'");
        }

        Paciente paciente = Paciente.builder()
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .email(request.email())
                .telefono(request.telefono())
                .fechaNacimiento(request.fechaNacimiento())
                .registrado(false)
                .build();

        return aResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponse actualizar(Long id, PacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente con id " + id + " no encontrado"));

        paciente.setNombres(request.nombres());
        paciente.setApellidos(request.apellidos());
        paciente.setEmail(request.email());
        paciente.setTelefono(request.telefono());
        paciente.setFechaNacimiento(request.fechaNacimiento());

        return aResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Paciente con id " + id + " no encontrado");
        }
        pacienteRepository.deleteById(id);
    }

    private PacienteResponse aResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.getEmail(),
                paciente.getTelefono(),
                paciente.getFechaNacimiento(),
                paciente.isRegistrado(),
                paciente.getCreadoEn()
        );
    }
}