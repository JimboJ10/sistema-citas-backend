package com.jordyjimbo.sistema_citas.controller;

import com.jordyjimbo.sistema_citas.dto.HorarioDisponibleRequest;
import com.jordyjimbo.sistema_citas.dto.HorarioDisponibleResponse;
import com.jordyjimbo.sistema_citas.service.HorarioDisponibleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioDisponibleController {

    private final HorarioDisponibleService horarioService;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<HorarioDisponibleResponse>> listarPorDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(horarioService.listarPorDoctor(doctorId));
    }

    @PostMapping
    public ResponseEntity<HorarioDisponibleResponse> crear(@Valid @RequestBody HorarioDisponibleRequest request) {
        HorarioDisponibleResponse creado = horarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}