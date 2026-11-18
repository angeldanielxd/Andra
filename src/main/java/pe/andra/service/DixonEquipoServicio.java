package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.DixonEquipoRepositorio;
import pe.andra.repository.DixonPrestamoActivoRepositorio;
import pe.andra.repository.DixonPrestamoRepositorio;
import pe.andra.util.DixonFechas;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class DixonEquipoServicio {

    public static final Map<String, String> DIXON_DURACIONES = new LinkedHashMap<>();

    static {
        DIXON_DURACIONES.put("2h", "2 horas lectivas");
        DIXON_DURACIONES.put("4h", "4 horas lectivas");
        DIXON_DURACIONES.put("day", "Jornada completa");
    }
    public static final List<String[]> DIXON_DURACIONES_LARGAS = List.of(
            new String[]{"2h", "Franja corta (hasta 2 horas lectivas)"},
            new String[]{"4h", "Franja estándar (hasta 4 horas lectivas)"},
            new String[]{"day", "Jornada completa"});
    public static final List<String[]> DIXON_PREDEFINIDOS = List.of(new String[]{"now", "Disponibles ahora"},
            new String[]{"fav", "Mis favoritos"}, new String[]{"fac", "De una escuela"},
            new String[]{"many", "3 unidades o más"});
    public static final List<String> DIXON_TIPOS_INCIDENCIA = List.of("Falla de funcionamiento", "Daño físico",
            "Pérdida o extravío de accesorio", "Otro");

    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonContextoServicio dixonContexto;
    private final DixonEquipoRepositorio dixonEquipos;
    private final DixonPrestamoRepositorio dixonPrestamos;
    private final DixonPrestamoActivoRepositorio dixonActivos;
    private final DixonSesionServicio dixonSesion;
    private final DixonFavoritoServicio dixonFavoritos;
    private final DixonConexionServicio dixonConexion;
    private final DixonNotificacionServicio dixonNotificaciones;

    @Transactional(readOnly = true)
    public List<DixonEquipo> dixonEnContexto() {
        return dixonCatalogo.dixonEquipos().stream().filter(dixonEquipo -> dixonContexto.dixonIncluye(dixonEquipo.getDixonSede(), dixonEquipo.getDixonEscuela())).toList();
    }

    public int dixonDisponiblesEnCategoria(List<DixonEquipo> dixonConjunto, DixonCategoriaEquipo dixonCategoriaEquipo) {
        return dixonConjunto.stream().filter(dixonEquipo -> dixonEquipo.getDixonCategoria().getDixonCodigo().equals(dixonCategoriaEquipo.getDixonCodigo()))
                .mapToInt(dixonEquipo -> Math.max(0, dixonEquipo.getDixonSaldo())).sum();
    }

    public Map<String, Predicate<DixonEquipo>> dixonPredicados() {
        java.util.Set<String> dixonFav = dixonFavoritos.dixonCodigos(DixonTipoFavorito.EQUIPO);
        return Map.of("now", DixonEquipo::dixonEsDisponibleAhora, "fav", dixonEquipo -> dixonFav.contains(dixonEquipo.getDixonCodigo()),
                "fac", dixonEquipo -> dixonEquipo.getDixonEscuela() != null, "many", dixonEquipo -> dixonEquipo.getDixonSaldo() >= 3);
    }

    @Transactional
    public DixonPrestamo dixonSolicitar(DixonEquipo dixonEquipo, LocalDate dixonFecha, String dixonHora, String dixonDuracion, String dixonMotivo, boolean dixonAcepta) {
        if (dixonFecha == null) {
            throw new DixonNegocioExcepcion("Elige la fecha.");
        }
        if (dixonFecha.isBefore(DixonFechas.dixonHoy())) {
            throw new DixonNegocioExcepcion("Elige hoy o una fecha posterior.");
        }
        if (dixonHora == null || dixonHora.isBlank()) {
            throw new DixonNegocioExcepcion(dixonEquipo.dixonEsPorTurno() ? "Elige un turno." : "Elige la hora de retiro.");
        }
        if (dixonMotivo == null || dixonMotivo.trim().length() < 10) {
            throw new DixonNegocioExcepcion("Escribe al menos 10 caracteres.");
        }
        if (!dixonAcepta) {
            throw new DixonNegocioExcepcion("Debes aceptar el reglamento para continuar.");
        }
        DixonEquipo dixonEquipo2 = dixonEquipos.findById(dixonEquipo.getDixonId()).orElseThrow();
        if (!dixonEquipo2.dixonEsPorTurno()) {
            if (dixonEquipo2.getDixonSaldo() <= 0) {
                throw new DixonNegocioExcepcion("Sin unidades disponibles.");
            }
            dixonEquipo2.setDixonSaldo(dixonEquipo2.getDixonSaldo() - 1);
            dixonEquipos.save(dixonEquipo2);
        }
        String dixonCodigo = dixonNuevoCodigo();
        DixonPrestamo dixonPrestamo = dixonPrestamos.save(DixonPrestamo.builder().dixonCodigo(dixonCodigo).dixonClave("h" + UUID.randomUUID().toString().substring(0, 6))
                .dixonPersona(dixonSesion.dixonEstudiante()).dixonEquipo(dixonEquipo2).dixonNombre(dixonEquipo2.getDixonNombre())
                .dixonDetalle("Solicitud registrada para el " + DixonFechas.dixonDmy(dixonFecha) + " (" + dixonHora + ") • "
                        + DIXON_DURACIONES.getOrDefault(dixonDuracion, "4 horas lectivas") + " • Sede " + dixonEquipo2.getDixonSede().getDixonNombre())
                .dixonEstado(DixonEstadoPrestamo.EN_REVISION).dixonSede(dixonEquipo2.getDixonSede()).build());
        dixonNotificaciones.dixonNotificarAdministrador("Nueva solicitud de préstamo " + dixonCodigo + ": " + dixonEquipo2.getDixonNombre() + ".", "a-prestamos");
        dixonConexion.dixonEncolar(dixonEquipo2.dixonEsPorTurno() ? "Reservar turno: " + dixonEquipo2.getDixonNombre() : "Solicitar préstamo: " + dixonEquipo2.getDixonNombre());
        return dixonPrestamo;
    }

    private String dixonNuevoCodigo() {
        int dixonCantidad = 460 + (int) dixonPrestamos.count();
        String dixonTexto;
        do {
            dixonTexto = "EQ-2026-0" + dixonCantidad++;
        } while (dixonPrestamos.existsByDixonCodigo(dixonTexto));
        return dixonTexto;
    }

    @Transactional(readOnly = true)
    public List<DixonPrestamo> dixonHistorial() {
        return dixonPrestamos.findByDixonPersonaOrderByDixonIdDesc(dixonSesion.dixonEstudiante());
    }

    @Transactional(readOnly = true)
    public DixonPrestamo dixonPrestamo(Long dixonId) {
        return dixonPrestamos.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("El préstamo no existe."));
    }

    @Transactional
    public boolean dixonEliminar(Long dixonId) {
        DixonPrestamo dixonPrestamo = dixonPrestamo(dixonId);
        boolean dixonActivo = dixonPrestamo.getDixonEstado().dixonEsEnCurso();
        if (dixonActivo && dixonPrestamo.getDixonEquipo() != null && !dixonPrestamo.getDixonEquipo().dixonEsPorTurno()) {
            DixonEquipo dixonEquipo = dixonEquipos.findById(dixonPrestamo.getDixonEquipo().getDixonId()).orElseThrow();
            dixonEquipo.setDixonSaldo(Math.min(dixonEquipo.getDixonUnidadesIniciales(), dixonEquipo.getDixonSaldo() + 1));
            dixonEquipos.save(dixonEquipo);
        }
        dixonPrestamos.delete(dixonPrestamo);
        dixonConexion.dixonEncolar(dixonActivo ? "¿Cancelar esta solicitud de préstamo?" : "¿Eliminar del historial?");
        return dixonActivo;
    }

    @Transactional(readOnly = true)
    public Optional<DixonPrestamoActivo> dixonActivo() {
        return dixonActivos.findFirstByOrderByDixonIdDesc().filter(dixonPrestamoActivo -> !dixonPrestamoActivo.isDixonDevuelto());
    }

    @Transactional(readOnly = true)
    public Optional<DixonPrestamoActivo> dixonCustodia() {
        return dixonActivos.findFirstByOrderByDixonIdDesc();
    }

    @Transactional
    public DixonPrestamoActivo dixonRenovar() {
        DixonPrestamoActivo dixonPrestamoActivo = dixonActivo().orElseThrow(() -> new DixonNegocioExcepcion("No tienes préstamos activos."));
        if (dixonPrestamoActivo.isDixonRenovado()) {
            throw new DixonNegocioExcepcion("Solo puedes renovar una vez por préstamo.");
        }
        dixonPrestamoActivo.setDixonLimite(dixonPrestamoActivo.getDixonLimite().plusHours(2));
        dixonPrestamoActivo.setDixonRenovado(true);
        dixonConexion.dixonEncolar("Renovar tiempo de préstamo");
        return dixonActivos.save(dixonPrestamoActivo);
    }

    @Transactional
    public DixonPrestamoActivo dixonReportar(String dixonTipo, String dixonDescripcion) {
        if (dixonTipo == null || dixonTipo.isBlank()) {
            throw new DixonNegocioExcepcion("Elige el tipo de incidencia.");
        }
        if (dixonDescripcion == null || dixonDescripcion.trim().length() < 15) {
            throw new DixonNegocioExcepcion("Escribe al menos 15 caracteres.");
        }
        DixonPrestamoActivo dixonPrestamoActivo = dixonActivo().orElseThrow(() -> new DixonNegocioExcepcion("No tienes préstamos activos."));
        dixonPrestamoActivo.setDixonIncidencia(dixonTipo);
        dixonConexion.dixonEncolar("Reportar daño o falla");
        return dixonActivos.save(dixonPrestamoActivo);
    }

    public static String dixonTextoLimite(LocalDateTime dixonLimite, LocalDateTime dixonAhora) {
        String dixonHoraTexto = String.format("%02d:%02d hrs", dixonLimite.getHour(), dixonLimite.getMinute());
        Duration dixonResta = Duration.between(dixonAhora, dixonLimite);
        if (dixonResta.isNegative()) {
            return "Hoy, " + dixonHoraTexto + " (vencido)";
        }
        long dixonAlto = dixonResta.toHours();
        long dixonNumero = dixonResta.toMinutesPart();
        return "Hoy, " + dixonHoraTexto + " (Restan " + dixonAlto + "h " + String.format("%02d", dixonNumero) + "m)";
    }
}
