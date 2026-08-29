package com.jordyjimbo.sistema_citas.service;

import com.jordyjimbo.sistema_citas.dto.HorarioDisponibleRequest;
import com.jordyjimbo.sistema_citas.dto.HorarioDisponibleResponse;
import com.jordyjimbo.sistema_citas.entity.Doctor;
import com.jordyjimbo.sistema_citas.entity.HorarioDisponible;
import com.jordyjimbo.sistema_citas.exception.RecursoNoEncontradoException;
import com.jordyjimbo.sistema_citas.exception.SolicitudInvalidaException;
import com.jordyjimbo.sistema_citas.repository.DoctorRepository;
import com.jordyjimbo.sistema_citas.repository.HorarioDisponibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HorarioDisponibleService {

    private final HorarioDisponibleRepository horarioRepository;
    private final DoctorRepository doctorRepository;

    public List<HorarioDisponibleResponse> listarPorDoctor(Long doctorId) {
        return horarioRepository.findByDoctor_Id(doctorId)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public HorarioDisponibleResponse crear(HorarioDisponibleRequest request) {
        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new SolicitudInvalidaException("La hora de fin debe ser posterior a la hora de inicio");
        }

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Doctor con id " + request.doctorId() + " no encontrado"));

        HorarioDisponible horario = HorarioDisponible.builder()
                .doctor(doctor)
                .diaSemana(com.jordyjimbo.sistema_citas.util.DiaSemanaUtil.desdeEspanol(request.diaSemana()))
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .activo(true)
                .build();

        return aResponse(horarioRepository.save(horario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!horarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Horario con id " + id + " no encontrado");
        }
        horarioRepository.deleteById(id);
    }

    private HorarioDisponibleResponse aResponse(HorarioDisponible horario) {
        return new HorarioDisponibleResponse(
                horario.getId(),
                horario.getDoctor().getId(),
                horario.getDoctor().getNombres() + " " + horario.getDoctor().getApellidos(),
                com.jordyjimbo.sistema_citas.util.DiaSemanaUtil.aTextoLocalizado(horario.getDiaSemana(), java.util.Locale.forLanguageTag("es")),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.isActivo()
        );
    }
}