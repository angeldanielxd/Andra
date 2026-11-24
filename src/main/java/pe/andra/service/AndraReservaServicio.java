package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.AndraEstadoReserva;
import pe.andra.model.AndraEspacio;
import pe.andra.model.AndraPersona;
import pe.andra.model.AndraReserva;
import pe.andra.repository.AndraReservaRepositorio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraHash;
import pe.andra.util.AndraMontos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AndraReservaServicio {

    private final AndraReservaRepositorio andraRepositorio;
    private final AndraSesionServicio andraSesion;
    private final AndraNotificacionServicio andraNotificaciones;
    private final AndraConexionServicio andraConexion;

    public static boolean andraDisponibleSimulado(String andraNombreEspacio, long andraDesplazamientoDias, String andraFranja) {
        return AndraHash.andraNumero(andraNombreEspacio + andraDesplazamientoDias + andraFranja) % 3 != 0;
    }

    @Transactional(readOnly = true)
    public boolean andraOcupadoPorReserva(AndraEspacio andraEspacio, LocalDate andraFecha, String andraFranja) {
        return andraRepositorio.findByAndraEspacioAndAndraFecha(andraEspacio, andraFecha).stream()
                .anyMatch(andraReserva -> andraReserva.getAndraHorario().startsWith(andraFranja) && andraReserva.getAndraEstado().andraEsActiva());
    }

    @Transactional(readOnly = true)
    public boolean andraLibre(AndraEspacio andraEspacio, long andraDesplazamientoDias, String andraFranja) {
        return !andraEspacio.isAndraFueraDeServicio() && andraDisponibleSimulado(andraEspacio.getAndraNombre(), andraDesplazamientoDias, andraFranja)
                && !andraOcupadoPorReserva(andraEspacio, AndraFechas.andraHoy().plusDays(andraDesplazamientoDias), andraFranja);
    }

    @Transactional(readOnly = true)
    public boolean andraLibreEnInicio(AndraEspacio andraEspacio, long andraDesplazamientoDias, String andraFranja) {
        return andraDisponibleSimulado(andraEspacio.getAndraNombre(), andraDesplazamientoDias, andraFranja)
                && !andraOcupadoPorReserva(andraEspacio, AndraFechas.andraHoy().plusDays(andraDesplazamientoDias), andraFranja);
    }

    @Transactional(readOnly = true)
    public String andraEstadoGaleria(AndraEspacio andraEspacio, LocalDate andraFecha, String andraHorario) {
        if (andraEspacio.isAndraFueraDeServicio()) {
            return "Ocupado";
        }
        if (andraFecha != null && andraHorario != null && !andraHorario.isBlank() && andraOcupadoPorReserva(andraEspacio, andraFecha, andraHorario.substring(0, 5))) {
            return "Ocupado";
        }
        return "Disponible";
    }

    @Transactional
    public AndraReserva andraReservar(AndraEspacio andraEspacio, LocalDate andraFecha, String andraHorario, Integer andraAsistentes, String andraMotivo) {
        if (andraFecha == null) {
            throw new AndraNegocioExcepcion("Elige una fecha.");
        }
        if (andraFecha.isBefore(AndraFechas.andraHoy())) {
            throw new AndraNegocioExcepcion("Elige hoy o una fecha posterior.");
        }
        if (andraHorario == null || !AndraCatalogoServicio.ANDRA_HORARIOS.contains(andraHorario)) {
            throw new AndraNegocioExcepcion("Elige un horario.");
        }
        if (andraAsistentes == null || andraAsistentes < 1) {
            throw new AndraNegocioExcepcion("Indica cuántas personas asistirán.");
        }
        if (andraAsistentes > andraEspacio.getAndraCapacidad()) {
            throw new AndraNegocioExcepcion("El máximo permitido es " + AndraMontos.andraMiles(andraEspacio.getAndraCapacidad()) + ".");
        }
        if (andraMotivo == null || andraMotivo.trim().length() < 10) {
            throw new AndraNegocioExcepcion("Escribe al menos 10 caracteres.");
        }
        if (andraOcupadoPorReserva(andraEspacio, andraFecha, andraHorario.substring(0, 5))) {
            throw new AndraNegocioExcepcion("Ese horario ya está reservado. Elige otro.");
        }
        AndraReserva andraReserva = andraRepositorio.save(AndraReserva.builder().andraCodigo(andraNuevoCodigo()).andraPersona(andraSesion.andraEstudiante()).andraEspacio(andraEspacio)
                .andraFecha(andraFecha).andraHorario(andraHorario).andraEstado(AndraEstadoReserva.PENDIENTE).andraMotivo(andraMotivo.trim())
                .andraAsistentes(andraAsistentes).build());
        andraNotificaciones.andraNotificarEstudiante("Solicitud de reserva enviada: " + andraEspacio.getAndraNombre() + ", "
                + AndraFechas.andraDmy(andraFecha) + ".", "reservas");
        andraConexion.andraEncolar("Reserva de " + andraEspacio.getAndraNombre());
        return andraReserva;
    }

    @Transactional
    public AndraReserva andraReservaRapida(AndraEspacio andraEspacio, LocalDate andraFecha, String andraFranja) {
        String andraHorario = AndraCatalogoServicio.ANDRA_HORARIOS.get(AndraCatalogoServicio.ANDRA_FRANJAS.indexOf(andraFranja));
        if (andraOcupadoPorReserva(andraEspacio, andraFecha, andraFranja)) {
            throw new AndraNegocioExcepcion("Ese horario ya está reservado. Elige otro.");
        }
        andraConexion.andraEncolar("Confirmar reserva");
        return andraRepositorio.save(AndraReserva.builder().andraCodigo(andraNuevoCodigo()).andraPersona(andraSesion.andraEstudiante()).andraEspacio(andraEspacio)
                .andraFecha(andraFecha).andraHorario(andraHorario).andraEstado(AndraEstadoReserva.PENDIENTE).andraMotivo("Reserva rápida desde Inicio")
                .andraAsistentes(1).build());
    }

    @Transactional(readOnly = true)
    public List<AndraReserva> andraMias() {
        AndraPersona andraEstudianteActual = andraSesion.andraEstudiante();
        return andraRepositorio.findByAndraPersonaOrderByAndraIdDesc(andraEstudianteActual);
    }

    @Transactional(readOnly = true)
    public List<AndraReserva> andraTodas() {
        return andraRepositorio.findAllByOrderByAndraIdDesc();
    }

    @Transactional(readOnly = true)
    public AndraReserva andraObtener(Long andraId) {
        return andraRepositorio.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("La reserva no existe."));
    }

    @Transactional
    public boolean andraEliminar(Long andraId) {
        AndraReserva andraReserva = andraObtener(andraId);
        boolean andraActiva = andraReserva.getAndraEstado().andraEsActiva();
        andraRepositorio.delete(andraReserva);
        andraConexion.andraEncolar(andraActiva ? "¿Cancelar esta reserva?" : "¿Eliminar del historial?");
        return andraActiva;
    }

    private String andraNuevoCodigo() {
        return "R-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
