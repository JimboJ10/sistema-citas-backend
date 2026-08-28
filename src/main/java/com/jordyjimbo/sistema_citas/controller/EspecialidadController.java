package com.jordyjimbo.sistema_citas.controller;

import com.jordyjimbo.sistema_citas.dto.EspecialidadRequest;
import com.jordyjimbo.sistema_citas.dto.EspecialidadResponse;
import com.jordyjimbo.sistema_citas.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @GetMapping
    public ResponseEntity<List<EspecialidadResponse>> listarTodas() {
        return ResponseEntity.ok(especialidadService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) {
        EspecialidadResponse creada = especialidadService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.ok(especialidadService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}