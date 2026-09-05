package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.AuthLoginRequest;
import com.jordyjimbo.sistema_citas.dto.AuthRegistroRequest;
import com.jordyjimbo.sistema_citas.dto.AuthResponse;
import com.jordyjimbo.sistema_citas.entity.Paciente;
import com.jordyjimbo.sistema_citas.entity.Usuario;
import com.jordyjimbo.sistema_citas.exception.RecursoDuplicadoException;
import com.jordyjimbo.sistema_citas.repository.DoctorRepository;
import com.jordyjimbo.sistema_citas.repository.PacienteRepository;
import com.jordyjimbo.sistema_citas.repository.UsuarioRepository;
import com.jordyjimbo.sistema_citas.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrar_conUsernameNuevo_creaPacienteYUsuarioCorrectamente() {
        AuthRegistroRequest request = new AuthRegistroRequest(
                "jimboj10", "claveSegura1", "Jordy", "Jimbo", "0991234567"
        );

        when(usuarioRepository.existsByUsername("jimboj10")).thenReturn(false);
        when(passwordEncoder.encode("claveSegura1")).thenReturn("hash-simulado");
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocacion -> {
            Paciente p = invocacion.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(jwtService.generarToken(any(UserDetails.class))).thenReturn("token-simulado");

        AuthResponse response = authService.registrar(request);

        assertThat(response.token()).isEqualTo("token-simulado");
        assertThat(response.username()).isEqualTo("jimboj10");
        assertThat(response.rol()).isEqualTo("PACIENTE");
        assertThat(response.pacienteId()).isEqualTo(1L);
        assertThat(response.doctorId()).isNull();
    }

    @Test
    void registrar_encriptaLaContrasenaAntesDeGuardar() {
        AuthRegistroRequest request = new AuthRegistroRequest(
                "usuario.nuevo", "contraseñaEnTextoPlano", "Ana", "Lopez", "0987654321"
        );

        when(usuarioRepository.existsByUsername("usuario.nuevo")).thenReturn(false);
        when(passwordEncoder.encode("contraseñaEnTextoPlano")).thenReturn("hash-bcrypt-simulado");
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(jwtService.generarToken(any(UserDetails.class))).thenReturn("token");

        authService.registrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioGuardado = usuarioCaptor.getValue();
        assertThat(usuarioGuardado.getPasswordHash()).isEqualTo("hash-bcrypt-simulado");
        assertThat(usuarioGuardado.getPasswordHash()).isNotEqualTo("contraseñaEnTextoPlano");
    }

    @Test
    void registrar_conUsernameYaExistente_lanzaRecursoDuplicado() {
        AuthRegistroRequest request = new AuthRegistroRequest(
                "ya.existe", "clave12345", "Test", "Test", "0999999999"
        );

        when(usuarioRepository.existsByUsername("ya.existe")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("ya.existe");

        verify(pacienteRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_conCredencialesValidas_devuelveTokenYDatosDelUsuario() {
        AuthLoginRequest request = new AuthLoginRequest("admin.principal", "clave12345");

        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("admin.principal")
                .rol(Usuario.Rol.ADMIN)
                .activo(true)
                .build();

        when(usuarioRepository.findByUsername("admin.principal")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(any(UserDetails.class))).thenReturn("token-de-admin");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token-de-admin");
        assertThat(response.rol()).isEqualTo("ADMIN");
        assertThat(response.pacienteId()).isNull();
        assertThat(response.doctorId()).isNull();
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_conCredencialesInvalidas_propagaLaExcepcion() {
        AuthLoginRequest request = new AuthLoginRequest("usuario", "claveMala");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }
}