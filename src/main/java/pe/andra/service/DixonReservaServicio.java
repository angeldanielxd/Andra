package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.DixonEstadoReserva;
import pe.andra.model.DixonEspacio;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonReserva;
import pe.andra.repository.DixonReservaRepositorio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonHash;
import pe.andra.util.DixonMontos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DixonReservaServicio {

    private final DixonReservaRepositorio dixonRepositorio;
    private final DixonSesionServicio dixonSesion;
    private final DixonNotificacionServicio dixonNotificaciones;
    private final DixonConexionServicio dixonConexion;

    public static boolean dixonDisponibleSimulado(String dixonNombreEspacio, long dixonDesplazamientoDias, String dixonFranja) {
        return DixonHash.dixonNumero(dixonNombreEspacio + dixonDesplazamientoDias + dixonFranja) % 3 != 0;
    }

    @Transactional(readOnly = true)
    public boolean dixonOcupadoPorReserva(DixonEspacio dixonEspacio, LocalDate dixonFecha, String dixonFranja) {
        return dixonRepositorio.findByDixonEspacioAndDixonFecha(dixonEspacio, dixonFecha).stream()
                .anyMatch(dixonReserva -> dixonReserva.getDixonHorario().startsWith(dixonFranja) && dixonReserva.getDixonEstado().dixonEsActiva());
    }

    @Transactional(readOnly = true)
    public boolean dixonLibre(DixonEspacio dixonEspacio, long dixonDesplazamientoDias, String dixonFranja) {
        return !dixonEspacio.isDixonFueraDeServicio() && dixonDisponibleSimulado(dixonEspacio.getDixonNombre(), dixonDesplazamientoDias, dixonFranja)
                && !dixonOcupadoPorReserva(dixonEspacio, DixonFechas.dixonHoy().plusDays(dixonDesplazamientoDias), dixonFranja);
    }

    @Transactional(readOnly = true)
    public boolean dixonLibreEnInicio(DixonEspacio dixonEspacio, long dixonDesplazamientoDias, String dixonFranja) {
        return dixonDisponibleSimulado(dixonEspacio.getDixonNombre(), dixonDesplazamientoDias, dixonFranja)
                && !dixonOcupadoPorReserva(dixonEspacio, DixonFechas.dixonHoy().plusDays(dixonDesplazamientoDias), dixonFranja);
    }

    @Transactional(readOnly = true)
    public String dixonEstadoGaleria(DixonEspacio dixonEspacio, LocalDate dixonFecha, String dixonHorario) {
        if (dixonEspacio.isDixonFueraDeServicio()) {
            return "Ocupado";
        }
        if (dixonFecha != null && dixonHorario != null && !dixonHorario.isBlank() && dixonOcupadoPorReserva(dixonEspacio, dixonFecha, dixonHorario.substring(0, 5))) {
            return "Ocupado";
        }
        return "Disponible";
    }

    @Transactional
    public DixonReserva dixonReservar(DixonEspacio dixonEspacio, LocalDate dixonFecha, String dixonHorario, Integer dixonAsistentes, String dixonMotivo) {
        if (dixonFecha == null) {
            throw new DixonNegocioExcepcion("Elige una fecha.");
        }
        if (dixonFecha.isBefore(DixonFechas.dixonHoy())) {
            throw new DixonNegocioExcepcion("Elige hoy o una fecha posterior.");
        }
        if (dixonHorario == null || !DixonCatalogoServicio.DIXON_HORARIOS.contains(dixonHorario)) {
            throw new DixonNegocioExcepcion("Elige un horario.");
        }
        if (dixonAsistentes == null || dixonAsistentes < 1) {
            throw new DixonNegocioExcepcion("Indica cuántas personas asistirán.");
        }
        if (dixonAsistentes > dixonEspacio.getDixonCapacidad()) {
            throw new DixonNegocioExcepcion("El máximo permitido es " + DixonMontos.dixonMiles(dixonEspacio.getDixonCapacidad()) + ".");
        }
        if (dixonMotivo == null || dixonMotivo.trim().length() < 10) {
            throw new DixonNegocioExcepcion("Escribe al menos 10 caracteres.");
        }
        if (dixonOcupadoPorReserva(dixonEspacio, dixonFecha, dixonHorario.substring(0, 5))) {
            throw new DixonNegocioExcepcion("Ese horario ya está reservado. Elige otro.");
        }
        DixonReserva dixonReserva = dixonRepositorio.save(DixonReserva.builder().dixonCodigo(dixonNuevoCodigo()).dixonPersona(dixonSesion.dixonEstudiante()).dixonEspacio(dixonEspacio)
                .dixonFecha(dixonFecha).dixonHorario(dixonHorario).dixonEstado(DixonEstadoReserva.PENDIENTE).dixonMotivo(dixonMotivo.trim())
                .dixonAsistentes(dixonAsistentes).build());
        dixonNotificaciones.dixonNotificarEstudiante("Solicitud de reserva enviada: " + dixonEspacio.getDixonNombre() + ", "
                + DixonFechas.dixonDmy(dixonFecha) + ".", "reservas");
        dixonConexion.dixonEncolar("Reserva de " + dixonEspacio.getDixonNombre());
        return dixonReserva;
    }

    @Transactional
    public DixonReserva dixonReservaRapida(DixonEspacio dixonEspacio, LocalDate dixonFecha, String dixonFranja) {
        String dixonHorario = DixonCatalogoServicio.DIXON_HORARIOS.get(DixonCatalogoServicio.DIXON_FRANJAS.indexOf(dixonFranja));
        if (dixonOcupadoPorReserva(dixonEspacio, dixonFecha, dixonFranja)) {
            throw new DixonNegocioExcepcion("Ese horario ya está reservado. Elige otro.");
        }
        dixonConexion.dixonEncolar("Confirmar reserva");
        return dixonRepositorio.save(DixonReserva.builder().dixonCodigo(dixonNuevoCodigo()).dixonPersona(dixonSesion.dixonEstudiante()).dixonEspacio(dixonEspacio)
                .dixonFecha(dixonFecha).dixonHorario(dixonHorario).dixonEstado(DixonEstadoReserva.PENDIENTE).dixonMotivo("Reserva rápida desde Inicio")
                .dixonAsistentes(1).build());
    }

    @Transactional(readOnly = true)
    public List<DixonReserva> dixonMias() {
        DixonPersona dixonEstudianteActual = dixonSesion.dixonEstudiante();
        return dixonRepositorio.findByDixonPersonaOrderByDixonIdDesc(dixonEstudianteActual);
    }

    @Transactional(readOnly = true)
    public List<DixonReserva> dixonTodas() {
        return dixonRepositorio.findAllByOrderByDixonIdDesc();
    }

    @Transactional(readOnly = true)
    public DixonReserva dixonObtener(Long dixonId) {
        return dixonRepositorio.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("La reserva no existe."));
    }

    @Transactional
    public boolean dixonEliminar(Long dixonId) {
        DixonReserva dixonReserva = dixonObtener(dixonId);
        boolean dixonActiva = dixonReserva.getDixonEstado().dixonEsActiva();
        dixonRepositorio.delete(dixonReserva);
        dixonConexion.dixonEncolar(dixonActiva ? "¿Cancelar esta reserva?" : "¿Eliminar del historial?");
        return dixonActiva;
    }

    private String dixonNuevoCodigo() {
        return "R-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
