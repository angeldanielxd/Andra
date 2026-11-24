package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AndraAdministradorServicio {

    public record AndraDia(String andraEtiqueta, BigDecimal andraMonto, boolean andraHoy) {
    }

    public record AndraPanel(BigDecimal andraHoy, BigDecimal andraMes, List<AndraExpediente> andraPorAtender, List<AndraReserva> andraReservasPendientes,
                        List<AndraPrestamo> andraPrestamosEnRevision, List<AndraDia> andraDias, Map<String, BigDecimal> andraPorMetodo,
                        Map<String, BigDecimal> andraPorConcepto, List<AndraPago> andraRecientes) {
    }

    public static final List<String[]> ANDRA_PREDEFINIDOS_TRAMITES = List.of(new String[]{"today", "Hoy"},
            new String[]{"d7", "Últimos 7 días"}, new String[]{"month", "Este mes"}, new String[]{"paid", "Pago confirmado"},
            new String[]{"unpaid", "Pago pendiente"}, new String[]{"carne", "Carné"});
    public static final List<String[]> ANDRA_PREDEFINIDOS_PAGOS = List.of(new String[]{"today", "Hoy"},
            new String[]{"d7", "Últimos 7 días"}, new String[]{"month", "Este mes"}, new String[]{"yape", "Yape"},
            new String[]{"plin", "Plin"}, new String[]{"card", "Tarjeta"});
    public static final List<String[]> ANDRA_PREDEFINIDOS_RESERVAS = List.of(new String[]{"today", "Para hoy"},
            new String[]{"d7", "Próximos 7 días"}, new String[]{"big", "100 personas o más"}, new String[]{"aud", "Auditorios"});
    public static final List<String[]> ANDRA_PREDEFINIDOS_PRESTAMOS = List.of(new String[]{"rev", "En revisión"},
            new String[]{"apr", "Aprobados"}, new String[]{"back", "Devueltos"}, new String[]{"lab", "Instrumental de laboratorio"});

    private final AndraExpedienteRepositorio andraExpedientes;
    private final AndraPagoRepositorio andraPagos;
    private final AndraReservaRepositorio andraReservas;
    private final AndraPrestamoRepositorio andraPrestamos;
    private final AndraPrestamoActivoRepositorio andraActivos;
    private final AndraNotificacionServicio andraNotificaciones;
    private final AndraSeguimientoServicio andraSeguimiento;
    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraConexionServicio andraConexion;

    @Transactional(readOnly = true)
    public List<AndraExpediente> andraExpedientes() {
        return andraExpedientes.findAllByOrderByAndraIdDesc().stream().filter(andraExpediente -> andraContexto.andraIncluyeSede(andraExpediente.getAndraSede())).toList();
    }

    @Transactional(readOnly = true)
    public List<AndraPago> andraPagos() {
        return andraPagos.findAllByOrderByAndraCreadoDesc().stream().filter(andraPago -> andraContexto.andraIncluyeSede(andraPago.getAndraSede())).toList();
    }

    @Transactional(readOnly = true)
    public List<AndraReserva> andraReservas() {
        return andraReservas.findAllByOrderByAndraIdDesc().stream().filter(andraReserva -> andraContexto.andraIncluyeSede(andraReserva.getAndraEspacio().getAndraSede())).toList();
    }

    @Transactional(readOnly = true)
    public List<AndraPrestamo> andraPrestamos() {
        return andraPrestamos.findAllByOrderByAndraIdDesc().stream().filter(andraPrestamo -> andraContexto.andraIncluyeSede(andraPrestamo.getAndraSede())).toList();
    }

    @Transactional(readOnly = true)
    public Optional<AndraPrestamoActivo> andraCustodia() {
        return andraActivos.findFirstByOrderByAndraIdDesc().filter(andraPrestamoActivo -> !andraPrestamoActivo.isAndraDevuelto())
                .filter(andraPrestamoActivo -> andraContexto.andraTodasLasSedes() || "central".equals(andraContexto.andraCodigoSede()));
    }

    @Transactional(readOnly = true)
    public AndraPanel andraPanel() {
        LocalDate andraHoy = AndraFechas.andraHoy();
        List<AndraPago> andraPagados = andraPagos().stream().filter(andraPago -> andraPago.andraEsPagado() && andraPago.getAndraPagado() != null).toList();
        BigDecimal andraDeHoy = AndraPagoServicio.andraTotal(andraPagados.stream().filter(andraPago -> andraPago.getAndraPagado().toLocalDate().equals(andraHoy)).toList());
        BigDecimal andraDelMes = AndraPagoServicio.andraTotal(andraPagados.stream().filter(andraPago -> andraPago.getAndraPagado().getYear() == andraHoy.getYear()
                && andraPago.getAndraPagado().getMonth() == andraHoy.getMonth()).toList());
        List<AndraExpediente> andraPorAtender = andraExpedientes().stream().filter(andraExpediente -> andraExpediente.getAndraGrupo() == AndraGrupoExpediente.TRAMITE
                && !andraExpediente.getAndraEstado().contains("Pago pendiente")).toList();
        List<AndraReserva> andraResP = andraReservas().stream().filter(andraReserva -> andraReserva.getAndraEstado() == AndraEstadoReserva.PENDIENTE).toList();
        List<AndraPrestamo> andraPreP = andraPrestamos().stream().filter(andraPrestamo -> andraPrestamo.getAndraEstado() == AndraEstadoPrestamo.EN_REVISION).toList();
        List<AndraDia> andraDias = new ArrayList<>();
        for (int andraIndice = 0; andraIndice < 7; andraIndice++) {
            LocalDate andraFecha = andraHoy.minusDays(6 - andraIndice);
            String[] andraPartes = AndraFechas.andraCorto(andraFecha).split(" ");
            andraDias.add(new AndraDia(andraPartes[0] + " " + andraPartes[1], AndraPagoServicio.andraTotal(andraPagados.stream()
                    .filter(andraPago -> andraPago.getAndraPagado().toLocalDate().equals(andraFecha)).toList()), andraIndice == 6));
        }
        Map<String, BigDecimal> andraMetodo = new LinkedHashMap<>();
        andraMetodo.put("Yape", BigDecimal.ZERO);
        andraMetodo.put("Tarjeta", BigDecimal.ZERO);
        andraMetodo.put("Plin", BigDecimal.ZERO);
        andraPagados.forEach(andraPago -> andraMetodo.merge(andraPago.getAndraMetodo().andraObtenerGrupo(), andraPago.andraObtenerTotal(), BigDecimal::add));
        Map<String, BigDecimal> andraConcepto = new LinkedHashMap<>();
        andraPagados.forEach(andraPago -> andraConcepto.merge(andraPago.getAndraTipo().andraObtenerEtiqueta(), andraPago.andraObtenerTotal(), BigDecimal::add));
        List<AndraPago> andraRecientes = andraPagados.stream().sorted(Comparator.comparing(AndraPago::getAndraPagado).reversed()).limit(6).toList();
        return new AndraPanel(andraDeHoy, andraDelMes, andraPorAtender, andraResP, andraPreP, andraDias, andraMetodo, andraConcepto, andraRecientes);
    }

    @Transactional(readOnly = true)
    public AndraExpediente andraExpediente(String andraCodigo) {
        return andraExpedientes.findByAndraCodigo(andraCodigo).orElseThrow(() -> new AndraNegocioExcepcion("El expediente no existe."));
    }

    public static boolean andraPuedeEmitir(AndraExpediente andraExpediente) {
        return andraExpediente.getAndraGrupo() == AndraGrupoExpediente.TRAMITE && (andraExpediente.getAndraPago() == null || andraExpediente.getAndraPago().andraEsPagado());
    }

    @Transactional
    public AndraExpediente andraEmitir(String andraCodigo) {
        AndraExpediente andraExpediente = andraExpediente(andraCodigo);
        if (!andraPuedeEmitir(andraExpediente)) {
            throw new AndraNegocioExcepcion(andraExpediente.getAndraPago() != null && !andraExpediente.getAndraPago().andraEsPagado() ? "Esperando pago" : "Ya atendido");
        }
        andraExpediente.setAndraGrupo(AndraGrupoExpediente.LISTO);
        andraExpediente.setAndraTono("success");
        andraExpediente.setAndraDestacado(false);
        andraExpediente.setAndraEstado("Documento emitido · Listo para recoger");
        andraExpediente.setAndraPaso(4);
        andraExpediente.setAndraRotuloIcono("LISTO");
        if (andraExpediente.getAndraPago() != null && andraExpediente.getAndraTramite() != null && andraExpediente.getAndraTramite().andraGeneraDocumento()) {
            andraExpediente.andraPonerAcciones("primary", "gen-sol", "Ver documento generado", "secondary", "rcpt-sol", "Ver comprobante",
                    "tertiary", "route-sol", "Seguimiento en vivo");
        } else {
            andraExpediente.andraPonerAcciones("primary", "route-sol", "Seguimiento en vivo");
        }
        andraExpedientes.save(andraExpediente);
        if (andraExpediente.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Tu trámite " + andraExpediente.getAndraCodigo() + " (" + andraExpediente.getAndraTitulo()
                    + ") está listo para recoger en Mesa de Partes.", "solicitudes");
        }
        andraSeguimiento.andraRegistrar(andraExpediente, "Andra: tu " + andraExpediente.getAndraTitulo() + " (" + andraExpediente.getAndraCodigo() + ") está listo. Recógelo en la ventanilla "
                + AndraSeguimientoServicio.andraVentanilla(andraExpediente.getAndraCodigo()) + " de Mesa de Partes con tu DNI.");
        return andraExpediente;
    }

    @Transactional
    public AndraExpediente andraObservar(String andraCodigo, String andraMotivo) {
        if (andraMotivo == null || andraMotivo.trim().length() < 15) {
            throw new AndraNegocioExcepcion("Escribe al menos 15 caracteres.");
        }
        AndraExpediente andraExpediente = andraExpediente(andraCodigo);
        if (andraExpediente.getAndraGrupo() != AndraGrupoExpediente.TRAMITE) {
            throw new AndraNegocioExcepcion("Solo se observan trámites en curso.");
        }
        String andraTexto = andraMotivo.trim();
        andraExpediente.setAndraGrupo(AndraGrupoExpediente.OBSERVADO);
        andraExpediente.setAndraTono("error");
        andraExpediente.setAndraDestacado(true);
        andraExpediente.setAndraEstado("Observado por Mesa de Partes");
        andraExpediente.setAndraObservacion(andraTexto);
        andraExpediente.setAndraRotuloIcono("ALERTA");
        andraExpediente.setAndraIcono("alert");
        andraExpediente.andraPonerAcciones("primary", "fix-sol", "Subsanar Documento", "secondary", "obs-sol", "Ver Observaciones",
                "tertiary", "ask-sol", "Consultar a Secretaría");
        andraExpedientes.save(andraExpediente);
        if (andraExpediente.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Tu trámite " + andraExpediente.getAndraCodigo() + " fue observado: " + andraTexto, "solicitudes");
        }
        andraSeguimiento.andraRegistrar(andraExpediente, "Andra: tu trámite " + andraExpediente.getAndraCodigo() + " tiene una observación: " + andraTexto);
        return andraExpediente;
    }

    @Transactional
    public AndraExpediente andraEntregar(String andraCodigo) {
        AndraExpediente andraExpediente = andraExpediente(andraCodigo);
        andraExpediente.setAndraEstado("Entregado al solicitante");
        andraExpediente.setAndraTono("neutral");
        andraExpedientes.save(andraExpediente);
        if (andraExpediente.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Registramos la entrega de " + andraExpediente.getAndraTitulo() + ".", "solicitudes");
        }
        andraSeguimiento.andraRegistrar(andraExpediente, "Andra: entregamos tu " + andraExpediente.getAndraTitulo() + " (" + andraExpediente.getAndraCodigo() + "). ¡Gracias!");
        return andraExpediente;
    }

    @Transactional
    public AndraPago andraReembolsar(String andraCodigoPago) {
        AndraPago andraPago = andraPagos.findByAndraCodigo(andraCodigoPago).orElseThrow(() -> new AndraNegocioExcepcion("La orden no existe."));
        if (!andraPago.andraEsPagado()) {
            throw new AndraNegocioExcepcion("Solo se reembolsan pagos confirmados.");
        }
        andraPago.setAndraEstado(AndraEstadoPago.REEMBOLSADO);
        andraPagos.save(andraPago);
        if (andraPago.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Se reembolsó tu pago " + andraPago.getAndraBoleta() + " (" + AndraMontos.andraSoles(andraPago.andraObtenerTotal()) + ").", "pay");
        }
        return andraPago;
    }

    @Transactional
    public AndraReserva andraAprobarReserva(Long andraId) {
        AndraReserva andraReserva = andraReservas.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("La reserva no existe."));
        andraReserva.setAndraEstado(AndraEstadoReserva.APROBADO);
        andraReservas.save(andraReserva);
        if (andraReserva.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Tu reserva de " + andraReserva.getAndraEspacio().getAndraNombre() + " para el "
                    + AndraFechas.andraDmy(andraReserva.getAndraFecha()) + " fue aprobada.", "reservas");
        }
        return andraReserva;
    }

    @Transactional
    public AndraReserva andraRechazarReserva(Long andraId, String andraMotivo) {
        if (andraMotivo == null || andraMotivo.trim().length() < 10) {
            throw new AndraNegocioExcepcion("Escribe al menos 10 caracteres.");
        }
        AndraReserva andraReserva = andraReservas.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("La reserva no existe."));
        andraReserva.setAndraEstado(AndraEstadoReserva.RECHAZADO);
        andraReserva.setAndraNota(andraMotivo.trim());
        andraReservas.save(andraReserva);
        if (andraReserva.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Tu reserva de " + andraReserva.getAndraEspacio().getAndraNombre() + " fue rechazada: " + andraMotivo.trim(), "reservas");
        }
        return andraReserva;
    }

    @Transactional
    public AndraPrestamo andraAprobarPrestamo(Long andraId) {
        AndraPrestamo andraPrestamo = andraPrestamos.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("El préstamo no existe."));
        andraPrestamo.setAndraEstado(AndraEstadoPrestamo.APROBADO);
        andraPrestamos.save(andraPrestamo);
        if (andraPrestamo.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Tu préstamo " + andraPrestamo.getAndraCodigo() + " fue aprobado. Recógelo en el punto de entrega.", "equipos");
        }
        return andraPrestamo;
    }

    @Transactional
    public AndraPrestamo andraRechazarPrestamo(Long andraId) {
        AndraPrestamo andraPrestamo = andraPrestamos.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("El préstamo no existe."));
        andraPrestamo.setAndraEstado(AndraEstadoPrestamo.RECHAZADO);
        andraPrestamos.save(andraPrestamo);
        if (andraPrestamo.getAndraPersona().isAndraPrincipal()) {
            andraNotificaciones.andraNotificarEstudiante("Tu préstamo " + andraPrestamo.getAndraCodigo() + " fue rechazado.", "equipos");
        }
        return andraPrestamo;
    }

    @Transactional
    public AndraPrestamo andraDevolverPrestamo(Long andraId) {
        AndraPrestamo andraPrestamo = andraPrestamos.findById(andraId).orElseThrow(() -> new AndraNegocioExcepcion("El préstamo no existe."));
        andraPrestamo.setAndraEstado(AndraEstadoPrestamo.DEVUELTO);
        String[] andraPartes = andraPrestamo.getAndraDetalle().split("•");
        andraPrestamo.setAndraDetalle("Devuelto con éxito: " + AndraFechas.andraDmy(AndraFechas.andraHoy()) + " · " + andraPartes[andraPartes.length - 1].trim());
        return andraPrestamos.save(andraPrestamo);
    }

    @Transactional
    public AndraPrestamoActivo andraDevolverCustodia() {
        AndraPrestamoActivo andraPrestamoActivo = andraActivos.findFirstByOrderByAndraIdDesc().filter(andraPosicionX -> !andraPosicionX.isAndraDevuelto())
                .orElseThrow(() -> new AndraNegocioExcepcion("No hay préstamos en custodia."));
        andraPrestamoActivo.setAndraDevuelto(true);
        andraActivos.save(andraPrestamoActivo);
        andraPrestamos.save(AndraPrestamo.builder().andraCodigo(andraPrestamoActivo.getAndraCodigo()).andraClave("h" + UUID.randomUUID().toString().substring(0, 6))
                .andraPersona(andraPrestamoActivo.getAndraPersona()).andraNombre(andraPrestamoActivo.getAndraEquipo()).andraDetalle("Devuelto con éxito: " + AndraFechas.andraDmy(AndraFechas.andraHoy()) + " • Sede Central")
                .andraEstado(AndraEstadoPrestamo.DEVUELTO).andraSede(andraCatalogo.andraSede("central").orElseThrow()).build());
        andraNotificaciones.andraNotificarEstudiante("Registramos la devolución del " + andraPrestamoActivo.getAndraEquipo() + ".", "equipos");
        return andraPrestamoActivo;
    }

    public boolean andraEsLaboratorio(AndraPrestamo andraPrestamo) {
        return andraCatalogo.andraEquipos().stream().anyMatch(andraEquipo -> "laboratorio".equals(andraEquipo.getAndraCategoria().getAndraCodigo())
                && andraPrestamo.getAndraNombre().contains(andraEquipo.getAndraNombre().split(" ")[0]));
    }

    public AndraPersona andraPersona(String andraCodigo) {
        return andraCatalogo.andraPersona(andraCodigo).orElse(null);
    }

    public AndraConexionServicio andraConexion() {
        return andraConexion;
    }
}
