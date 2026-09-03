package com.jordyjimbo.sistema_citas.security;

import com.jordyjimbo.sistema_citas.entity.Usuario;
import com.jordyjimbo.sistema_citas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("pacientePermisos")
@RequiredArgsConstructor
public class PacientePermisos {

    private final UsuarioRepository usuarioRepository;

    public boolean esDuenoOAdmin(Long pacienteId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return false;
        }

        if (usuarioDetails.getRol() == Usuario.Rol.ADMIN) {
            return true;
        }

        Usuario usuario = usuarioRepository.findById(usuarioDetails.getId()).orElse(null);
        if (usuario == null || usuario.getPaciente() == null) {
            return false;
        }

        return usuario.getPaciente().getId().equals(pacienteId);
    }
}