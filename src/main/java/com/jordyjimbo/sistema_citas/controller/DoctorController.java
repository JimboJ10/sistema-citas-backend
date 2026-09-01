package com.jordyjimbo.sistema_citas.controller;

import com.jordyjimbo.sistema_citas.dto.DoctorRequest;
import com.jordyjimbo.sistema_citas.dto.DoctorResponse;
import com.jordyjimbo.sistema_citas.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctores")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listarTodos() {
        return ResponseEntity.ok(doctorService.listarTodos());
    }

    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<List<DoctorResponse>> listarPorEspecialidad(@PathVariable Long especialidadId) {
        return ResponseEntity.ok(doctorService.listarPorEspecialidad(especialidadId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> crear(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse creado = doctorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(doctorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        doctorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}