package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.EspecialidadRequest;
import com.jordyjimbo.sistema_citas.dto.EspecialidadResponse;
import com.jordyjimbo.sistema_citas.entity.Especialidad;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.exception.RecursoNoEncontradoException;
import com.jordyjimbo.sistema_citas.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public List<EspecialidadResponse> listarTodas() {
        return especialidadRepository.findAll()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public EspecialidadResponse buscarPorId(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad con id " + id + " no encontrada"));
        return aResponse(especialidad);
    }

    @Transactional
    public EspecialidadResponse crear(EspecialidadRequest request) {
        if (especialidadRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new RecursoDuplicadoException("Ya existe una especialidad con el nombre '" + request.nombre() + "'");
        }

        Especialidad especialidad = Especialidad.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .build();

        return aResponse(especialidadRepository.save(especialidad));
    }

    @Transactional
    public EspecialidadResponse actualizar(Long id, EspecialidadRequest request) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad con id " + id + " no encontrada"));

        especialidad.setNombre(request.nombre());
        especialidad.setDescripcion(request.descripcion());

        return aResponse(especialidadRepository.save(especialidad));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!especialidadRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Especialidad con id " + id + " no encontrada");
        }
        especialidadRepository.deleteById(id);
    }

    private EspecialidadResponse aResponse(Especialidad especialidad) {
        return new EspecialidadResponse(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion()
        );
    }
}