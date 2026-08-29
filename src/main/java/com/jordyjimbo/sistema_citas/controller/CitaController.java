package com.jordyjimbo.sistema_citas.controller;

import com.jordyjimbo.sistema_citas.dto.CitaRequest;
import com.jordyjimbo.sistema_citas.dto.CitaResponse;
import com.jordyjimbo.sistema_citas.entity.Cita;
import com.jordyjimbo.sistema_citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CitaResponse>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(citaService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<CitaResponse>> listarPorDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(citaService.listarPorDoctor(doctorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest request) {
        CitaResponse creada = citaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CitaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Cita.EstadoCita nuevoEstado) {
        return ResponseEntity.ok(citaService.cambiarEstado(id, nuevoEstado));
    }
}