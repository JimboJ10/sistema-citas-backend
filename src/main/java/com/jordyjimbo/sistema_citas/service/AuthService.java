package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.AuthCrearUsuarioRequest;
import com.jordyjimbo.sistema_citas.dto.AuthLoginRequest;
import com.jordyjimbo.sistema_citas.dto.AuthRegistroRequest;
import com.jordyjimbo.sistema_citas.dto.AuthResponse;
import com.jordyjimbo.sistema_citas.entity.Doctor;
import com.jordyjimbo.sistema_citas.entity.Paciente;
import com.jordyjimbo.sistema_citas.entity.Usuario;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.exception.RecursoNoEncontradoException;
import com.jordyjimbo.sistema_citas.repository.DoctorRepository;
import com.jordyjimbo.sistema_citas.repository.PacienteRepository;
import com.jordyjimbo.sistema_citas.repository.UsuarioRepository;
import com.jordyjimbo.sistema_citas.security.JwtService;
import com.jordyjimbo.sistema_citas.security.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registrar(AuthRegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el username '" + request.username() + "'");
        }

        Usuario.UsuarioBuilder builder = Usuario.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(Usuario.Rol.PACIENTE)
                .activo(true);

        if (request.pacienteId() != null) {
            Paciente paciente = pacienteRepository.findById(request.pacienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Paciente con id " + request.pacienteId() + " no encontrado"));
            paciente.setRegistrado(true);
            pacienteRepository.save(paciente);
            builder.paciente(paciente);
        }

        if (request.doctorId() != null) {
            Doctor doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Doctor con id " + request.doctorId() + " no encontrado"));
            builder.doctor(doctor);
        }

        Usuario usuario = usuarioRepository.save(builder.build());
        UsuarioDetails usuarioDetails = new UsuarioDetails(usuario);
        String token = jwtService.generarToken(usuarioDetails);

        return new AuthResponse(token, usuario.getUsername(), usuario.getRol().name());
    }

    public AuthResponse login(AuthLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        UsuarioDetails usuarioDetails = new UsuarioDetails(usuario);
        String token = jwtService.generarToken(usuarioDetails);

        return new AuthResponse(token, usuario.getUsername(), usuario.getRol().name());
    }

    @Transactional
    public AuthResponse crearUsuarioConRol(AuthCrearUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el username '" + request.username() + "'");
        }

        Usuario.UsuarioBuilder builder = Usuario.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activo(true);

        if (request.pacienteId() != null) {
            Paciente paciente = pacienteRepository.findById(request.pacienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Paciente con id " + request.pacienteId() + " no encontrado"));
            paciente.setRegistrado(true);
            pacienteRepository.save(paciente);
            builder.paciente(paciente);
        }

        if (request.doctorId() != null) {
            Doctor doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Doctor con id " + request.doctorId() + " no encontrado"));
            builder.doctor(doctor);
        }

        Usuario usuario = usuarioRepository.save(builder.build());
        return new AuthResponse(null, usuario.getUsername(), usuario.getRol().name());
    }
}