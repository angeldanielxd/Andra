package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.AndraEquipoRepositorio;
import pe.andra.repository.AndraPrestamoActivoRepositorio;
import pe.andra.repository.AndraPrestamoRepositorio;
import pe.andra.util.AndraFechas;

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
public class AndraEquipoServicio {

    public static final Map<String, String> ANDRA_DURACIONES = new LinkedHashMap<>();

    static {
        ANDRA_DURACIONES.put("2h", "2 horas lectivas");
        ANDRA_DURACIONES.put("4h", "4 horas lectivas");
        ANDRA_DURACIONES.put("day", "Jornada completa");
    }
    public static final List<String[]> ANDRA_DURACIONES_LARGAS = List.of(
            new String[]{"2h", "Franja corta (hasta 2 horas lectivas)"},
            new String[]{"4h", "Franja estándar (hasta 4 horas lectivas)"},
            new String[]{"day", "Jornada completa"});
    public static final List<String[]> ANDRA_PREDEFINIDOS = List.of(new String[]{"now", "Disponibles ahora"},
            new String[]{"fav", "Mis favoritos"}, new String[]{"fac", "De una escuela"},
            new String[]{"many", "3 unidades o más"});
    public static final List<String> ANDRA_TIPOS_INCIDENCIA = List.of("Falla de funcionamiento", "Daño físico",
            "Pérdida o extravío de accesorio", "Otro");

    private final AndraCatalogoServicio andraCatalogo;
    private final AndraContextoServicio andraContexto;
    private final AndraEquipoRepositorio andraEquipos;
    private final AndraPrestamoRepositorio andraPrestamos;
    private final AndraPrestamoActivoRepositorio andraActivos;
    private final AndraSesionServicio andraSesion;
    private final AndraFavoritoServicio andraFavoritos;
    private final AndraConexionServicio andraConexion;
    private final AndraNotificacionServicio andraNotificaciones;

    @Transactional(readOnly = true)
    public List<AndraEquipo> andraEnContexto() {
        return andraCatalogo.andraEquipos().stream().filter(andraEquipo -> andraContexto.andraIncluye(andraEquipo.getAndraSede(), andraEquipo.getAndraEscuela())).toList();
    }

    public int andraDisponiblesEnCategoria(List<AndraEquipo> andraConjunto, AndraCategoriaEquipo andraCategoriaEquipo) {
        return andraConjunto.stream().filter(andraEquipo -> andraEquipo.getAndraCategoria().getAndraCodigo().equals(andraCategoriaEquipo.getAndraCodigo()))
                .mapToInt(andraEquipo -> Math.max(0, andraEquipo.getAndraSaldo())).sum();
    }

    public Map<String, Predicate<AndraEquipo>> andraPredicados() {
        java.util.Set<String> andraFav = andraFavoritos.andraCodigos(AndraTipoFavorito.EQUIPO);
        return Map.of("now", AndraEquipo::andraEsDisponibleAhora, "fav", andraEquipo -> andraFav.contains(andraEquipo.getAndraCodigo()),
                "fac", andraEquipo -> andraEquipo.getAndraEscuela() != null, "many", andraEquipo -> andraEquipo.getAndraSaldo() >= 3);
    }

    @Transactional
    public AndraPrestamo andraSolicitar(AndraEquipo andraEquipo, LocalDate andraFecha, String andraHora, String andraDuracion, String andraMotivo, boolean andraAcepta) {
        if (andraFecha == null) {
            throw new AndraNegocioExcepcion("Elige la fecha.");
        }
        if (andraFecha.isBefore(AndraFechas.andraHoy())) {
            throw new AndraNegocioExcepcion("Elige hoy o una fecha posterior.");
        }
        if (andraHora == null || andraHora.isBlank()) {
            throw new AndraNegocioExcepcion(andraEquipo.andraEsPorTurno() ? "Elige un turno." : "Elige la hora de retiro.");
        }
        if (andraMotivo == null || andraMotivo.trim().length() < 10) {
            throw new AndraNegocioExcepcion("Escribe al menos 10 caracteres.");
        }
        if (!andraAcepta) {
            throw new AndraNegocioExcepcion("Debes aceptar el reglamento para continuar.");
        }
        AndraEquipo andraEquipo2 = andraEquipos.findById(andraEquipo.getAndraId()).orElseThrow();
        if (!andraEquipo2.andraEsPorTurno()) {
            if (andraEquipo2.getAndraSaldo() <= 0) {
                throw new AndraNegocioExcepcion("Sin unidades disponibles.");
            }
            andraEquipo2.setAndraSaldo(andraEquipo2.getAndraSaldo() - 1);
            andraEquipos.save(andraEquipo2);
        }
        String andraCodigo = andraNuevoCodigo();
        AndraPrestamo andraPrestamo = andraPrestamos.save(AndraPrestamo.builder().andraCodigo(andraCodigo).andraClave("h" + UUID.randomUUID().toString().substring(0, 6))
                .andraPersona(andraSesion.andraEstudiante()).andraEquipo(andraEquipo2).andraNombre(andraEquipo2.getAndraNombre())
                .andraDetalle("Solicitud registrada para el " + AndraFechas.andraDmy(andraFecha) + " (" + andraHora + ") • "
                        + ANDRA_DURACIONES.getOrDefault(andraDuracion, "4 horas lectivas") + " • Sede " + andraEquipo2.getAndraSede().getAndraNombre())
                .andraEstado(AndraEstadoPrestamo.EN_REVISION).andraSede(andraEquipo2.getAndraSede()).build());
        andraNotificaciones.andraNotificarAdministrador("Nueva solicitud de préstamo " + andraCodigo + ": " + andraEquipo2.getAndraNombre() + ".", "a-prestamos");
        andraConexion.andraEncolar(andraEquipo2.andraEsPorTurno() ? "Reservar turno: " + andraEquipo2.getAndraNombre() : "Solicitar préstamo: " + andraEquipo2.getAndraNombre());
        return andraPrestamo;
    }

    private String andraNuevoCodigo() {
        int andraCantidad = 460 + (int) andraPrestamos.count();
        String andraTexto;
        do {
            andraTexto = "EQ-2026-0" + andraCantidad++;
        } while (andraPrestamos.existsByAndraCodigo(andraTexto));
        return andraTexto;
    }

    @Transactional(readOnly = true)
    public List<AndraPrestamo> andraHistorial() {
        return andraPrestamos.findByAndraPersonaOrderByAndraIdDesc(andraSesion.andraEstudiante());
    }

    @Transactional(readOnly = true)
    public AndraPrestamo andraPrestamo(Long andraId) {
        return andraPrestamos.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("El préstamo no existe."));
    }

    @Transactional
    public boolean andraEliminar(Long andraId) {
        AndraPrestamo andraPrestamo = andraPrestamo(andraId);
        boolean andraActivo = andraPrestamo.getAndraEstado().andraEsEnCurso();
        if (andraActivo && andraPrestamo.getAndraEquipo() != null && !andraPrestamo.getAndraEquipo().andraEsPorTurno()) {
            AndraEquipo andraEquipo = andraEquipos.findById(andraPrestamo.getAndraEquipo().getAndraId()).orElseThrow();
            andraEquipo.setAndraSaldo(Math.min(andraEquipo.getAndraUnidadesIniciales(), andraEquipo.getAndraSaldo() + 1));
            andraEquipos.save(andraEquipo);
        }
        andraPrestamos.delete(andraPrestamo);
        andraConexion.andraEncolar(andraActivo ? "¿Cancelar esta solicitud de préstamo?" : "¿Eliminar del historial?");
        return andraActivo;
    }

    @Transactional(readOnly = true)
    public Optional<AndraPrestamoActivo> andraActivo() {
        return andraActivos.findFirstByOrderByAndraIdDesc().filter(andraPrestamoActivo -> !andraPrestamoActivo.isAndraDevuelto());
    }

    @Transactional(readOnly = true)
    public Optional<AndraPrestamoActivo> andraCustodia() {
        return andraActivos.findFirstByOrderByAndraIdDesc();
    }

    @Transactional
    public AndraPrestamoActivo andraRenovar() {
        AndraPrestamoActivo andraPrestamoActivo = andraActivo().orElseThrow(() -> new AndraNegocioExcepcion("No tienes préstamos activos."));
        if (andraPrestamoActivo.isAndraRenovado()) {
            throw new AndraNegocioExcepcion("Solo puedes renovar una vez por préstamo.");
        }
        andraPrestamoActivo.setAndraLimite(andraPrestamoActivo.getAndraLimite().plusHours(2));
        andraPrestamoActivo.setAndraRenovado(true);
        andraConexion.andraEncolar("Renovar tiempo de préstamo");
        return andraActivos.save(andraPrestamoActivo);
    }

    @Transactional
    public AndraPrestamoActivo andraReportar(String andraTipo, String andraDescripcion) {
        if (andraTipo == null || andraTipo.isBlank()) {
            throw new AndraNegocioExcepcion("Elige el tipo de incidencia.");
        }
        if (andraDescripcion == null || andraDescripcion.trim().length() < 15) {
            throw new AndraNegocioExcepcion("Escribe al menos 15 caracteres.");
        }
        AndraPrestamoActivo andraPrestamoActivo = andraActivo().orElseThrow(() -> new AndraNegocioExcepcion("No tienes préstamos activos."));
        andraPrestamoActivo.setAndraIncidencia(andraTipo);
        andraConexion.andraEncolar("Reportar daño o falla");
        return andraActivos.save(andraPrestamoActivo);
    }

    public static String andraTextoLimite(LocalDateTime andraLimite, LocalDateTime andraAhora) {
        String andraHoraTexto = String.format("%02d:%02d hrs", andraLimite.getHour(), andraLimite.getMinute());
        Duration andraResta = Duration.between(andraAhora, andraLimite);
        if (andraResta.isNegative()) {
            return "Hoy, " + andraHoraTexto + " (vencido)";
        }
        long andraAlto = andraResta.toHours();
        long andraNumero = andraResta.toMinutesPart();
        return "Hoy, " + andraHoraTexto + " (Restan " + andraAlto + "h " + String.format("%02d", andraNumero) + "m)";
    }
}
