package com.jordyjimbo.sistema_citas.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Los campos @Value no se inyectan fuera de un contexto Spring,
        // así que los seteamos manualmente para este test unitario.
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "clave-secreta-de-prueba-para-tests-debe-tener-32-caracteres-o-mas");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void generarToken_creaTokenValidoParaElUsuario() {
        UserDetails userDetails = User.withUsername("admin.principal")
                .password("cualquiera")
                .authorities("ROLE_ADMIN")
                .build();

        String token = jwtService.generarToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extraerUsername(token)).isEqualTo("admin.principal");
    }

    @Test
    void esTokenValido_devuelveTrueParaElMismoUsuario() {
        UserDetails userDetails = User.withUsername("carlos.ramirez")
                .password("cualquiera")
                .authorities("ROLE_DOCTOR")
                .build();

        String token = jwtService.generarToken(userDetails);

        assertThat(jwtService.esTokenValido(token, userDetails)).isTrue();
    }

    @Test
    void esTokenValido_devuelveFalseParaUnUsuarioDistinto() {
        UserDetails usuarioOriginal = User.withUsername("maria.torres")
                .password("cualquiera")
                .authorities("ROLE_PACIENTE")
                .build();

        UserDetails otroUsuario = User.withUsername("otro.usuario")
                .password("cualquiera")
                .authorities("ROLE_PACIENTE")
                .build();

        String token = jwtService.generarToken(usuarioOriginal);

        assertThat(jwtService.esTokenValido(token, otroUsuario)).isFalse();
    }

    @Test
    void extraerUsername_lanzaExcepcionSiElTokenEstaMalFormado() {
        String tokenInvalido = "esto.no.es.un.token.valido";

        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.JwtException.class,
                () -> jwtService.extraerUsername(tokenInvalido)
        );
    }
}