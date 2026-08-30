package com.jordyjimbo.sistema_citas.security;

import com.jordyjimbo.sistema_citas.entity.Cita;
import com.jordyjimbo.sistema_citas.entity.Usuario;
import com.jordyjimbo.sistema_citas.repository.CitaRepository;
import com.jordyjimbo.sistema_citas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("citaPermisos")
@RequiredArgsConstructor
public class CitaPermisos {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    public boolean esDuenoOAsignado(Long citaId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return false;
        }

        if (usuarioDetails.getRol() == Usuario.Rol.ADMIN) {
            return true;
        }

        Cita cita = citaRepository.findById(citaId).orElse(null);
        if (cita == null) {
            return false;
        }

        Usuario usuario = usuarioRepository.findById(usuarioDetails.getId()).orElse(null);
        if (usuario == null) {
            return false;
        }

        boolean esPacienteDueno = usuario.getPaciente() != null
                && cita.getPaciente().getId().equals(usuario.getPaciente().getId());

        boolean esDoctorAsignado = usuario.getDoctor() != null
                && cita.getDoctor().getId().equals(usuario.getDoctor().getId());

        return esPacienteDueno || esDoctorAsignado;
    }
}