package com.jordyjimbo.sistema_citas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono;

    private LocalDate fechaNacimiento;

    /**
     * true = el paciente se registró completo (tiene cuenta con login).
     * false = paciente "invitado", creado desde el chatbot solo con datos básicos.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean registrado = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void alCrear() {
        this.creadoEn = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "paciente")
    @Builder.Default
    private List<Cita> citas = new ArrayList<>();
}