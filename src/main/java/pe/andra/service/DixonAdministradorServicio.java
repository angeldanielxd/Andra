package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DixonAdministradorServicio {

    public record DixonDia(String dixonEtiqueta, BigDecimal dixonMonto, boolean dixonHoy) {
    }

    public record DixonPanel(BigDecimal dixonHoy, BigDecimal dixonMes, List<DixonExpediente> dixonPorAtender, List<DixonReserva> dixonReservasPendientes,
                        List<DixonPrestamo> dixonPrestamosEnRevision, List<DixonDia> dixonDias, Map<String, BigDecimal> dixonPorMetodo,
                        Map<String, BigDecimal> dixonPorConcepto, List<DixonPago> dixonRecientes) {
    }

    public static final List<String[]> DIXON_PREDEFINIDOS_TRAMITES = List.of(new String[]{"today", "Hoy"},
            new String[]{"d7", "Últimos 7 días"}, new String[]{"month", "Este mes"}, new String[]{"paid", "Pago confirmado"},
            new String[]{"unpaid", "Pago pendiente"}, new String[]{"carne", "Carné"});
    public static final List<String[]> DIXON_PREDEFINIDOS_PAGOS = List.of(new String[]{"today", "Hoy"},
            new String[]{"d7", "Últimos 7 días"}, new String[]{"month", "Este mes"}, new String[]{"yape", "Yape"},
            new String[]{"plin", "Plin"}, new String[]{"card", "Tarjeta"});
    public static final List<String[]> DIXON_PREDEFINIDOS_RESERVAS = List.of(new String[]{"today", "Para hoy"},
            new String[]{"d7", "Próximos 7 días"}, new String[]{"big", "100 personas o más"}, new String[]{"aud", "Auditorios"});
    public static final List<String[]> DIXON_PREDEFINIDOS_PRESTAMOS = List.of(new String[]{"rev", "En revisión"},
            new String[]{"apr", "Aprobados"}, new String[]{"back", "Devueltos"}, new String[]{"lab", "Instrumental de laboratorio"});

    private final DixonExpedienteRepositorio dixonExpedientes;
    private final DixonPagoRepositorio dixonPagos;
    private final DixonReservaRepositorio dixonReservas;
    private final DixonPrestamoRepositorio dixonPrestamos;
    private final DixonPrestamoActivoRepositorio dixonActivos;
    private final DixonNotificacionServicio dixonNotificaciones;
    private final DixonSeguimientoServicio dixonSeguimiento;
    private final DixonContextoServicio dixonContexto;
    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonConexionServicio dixonConexion;

    @Transactional(readOnly = true)
    public List<DixonExpediente> dixonExpedientes() {
        return dixonExpedientes.findAllByOrderByDixonIdDesc().stream().filter(dixonExpediente -> dixonContexto.dixonIncluyeSede(dixonExpediente.getDixonSede())).toList();
    }

    @Transactional(readOnly = true)
    public List<DixonPago> dixonPagos() {
        return dixonPagos.findAllByOrderByDixonCreadoDesc().stream().filter(dixonPago -> dixonContexto.dixonIncluyeSede(dixonPago.getDixonSede())).toList();
    }

    @Transactional(readOnly = true)
    public List<DixonReserva> dixonReservas() {
        return dixonReservas.findAllByOrderByDixonIdDesc().stream().filter(dixonReserva -> dixonContexto.dixonIncluyeSede(dixonReserva.getDixonEspacio().getDixonSede())).toList();
    }

    @Transactional(readOnly = true)
    public List<DixonPrestamo> dixonPrestamos() {
        return dixonPrestamos.findAllByOrderByDixonIdDesc().stream().filter(dixonPrestamo -> dixonContexto.dixonIncluyeSede(dixonPrestamo.getDixonSede())).toList();
    }

    @Transactional(readOnly = true)
    public Optional<DixonPrestamoActivo> dixonCustodia() {
        return dixonActivos.findFirstByOrderByDixonIdDesc().filter(dixonPrestamoActivo -> !dixonPrestamoActivo.isDixonDevuelto())
                .filter(dixonPrestamoActivo -> dixonContexto.dixonTodasLasSedes() || "central".equals(dixonContexto.dixonCodigoSede()));
    }

    @Transactional(readOnly = true)
    public DixonPanel dixonPanel() {
        LocalDate dixonHoy = DixonFechas.dixonHoy();
        List<DixonPago> dixonPagados = dixonPagos().stream().filter(dixonPago -> dixonPago.dixonEsPagado() && dixonPago.getDixonPagado() != null).toList();
        BigDecimal dixonDeHoy = DixonPagoServicio.dixonTotal(dixonPagados.stream().filter(dixonPago -> dixonPago.getDixonPagado().toLocalDate().equals(dixonHoy)).toList());
        BigDecimal dixonDelMes = DixonPagoServicio.dixonTotal(dixonPagados.stream().filter(dixonPago -> dixonPago.getDixonPagado().getYear() == dixonHoy.getYear()
                && dixonPago.getDixonPagado().getMonth() == dixonHoy.getMonth()).toList());
        List<DixonExpediente> dixonPorAtender = dixonExpedientes().stream().filter(dixonExpediente -> dixonExpediente.getDixonGrupo() == DixonGrupoExpediente.TRAMITE
                && !dixonExpediente.getDixonEstado().contains("Pago pendiente")).toList();
        List<DixonReserva> dixonResP = dixonReservas().stream().filter(dixonReserva -> dixonReserva.getDixonEstado() == DixonEstadoReserva.PENDIENTE).toList();
        List<DixonPrestamo> dixonPreP = dixonPrestamos().stream().filter(dixonPrestamo -> dixonPrestamo.getDixonEstado() == DixonEstadoPrestamo.EN_REVISION).toList();
        List<DixonDia> dixonDias = new ArrayList<>();
        for (int dixonIndice = 0; dixonIndice < 7; dixonIndice++) {
            LocalDate dixonFecha = dixonHoy.minusDays(6 - dixonIndice);
            String[] dixonPartes = DixonFechas.dixonCorto(dixonFecha).split(" ");
            dixonDias.add(new DixonDia(dixonPartes[0] + " " + dixonPartes[1], DixonPagoServicio.dixonTotal(dixonPagados.stream()
                    .filter(dixonPago -> dixonPago.getDixonPagado().toLocalDate().equals(dixonFecha)).toList()), dixonIndice == 6));
        }
        Map<String, BigDecimal> dixonMetodo = new LinkedHashMap<>();
        dixonMetodo.put("Yape", BigDecimal.ZERO);
        dixonMetodo.put("Tarjeta", BigDecimal.ZERO);
        dixonMetodo.put("Plin", BigDecimal.ZERO);
        dixonPagados.forEach(dixonPago -> dixonMetodo.merge(dixonPago.getDixonMetodo().dixonObtenerGrupo(), dixonPago.dixonObtenerTotal(), BigDecimal::add));
        Map<String, BigDecimal> dixonConcepto = new LinkedHashMap<>();
        dixonPagados.forEach(dixonPago -> dixonConcepto.merge(dixonPago.getDixonTipo().dixonObtenerEtiqueta(), dixonPago.dixonObtenerTotal(), BigDecimal::add));
        List<DixonPago> dixonRecientes = dixonPagados.stream().sorted(Comparator.comparing(DixonPago::getDixonPagado).reversed()).limit(6).toList();
        return new DixonPanel(dixonDeHoy, dixonDelMes, dixonPorAtender, dixonResP, dixonPreP, dixonDias, dixonMetodo, dixonConcepto, dixonRecientes);
    }

    @Transactional(readOnly = true)
    public DixonExpediente dixonExpediente(String dixonCodigo) {
        return dixonExpedientes.findByDixonCodigo(dixonCodigo).orElseThrow(() -> new DixonNegocioExcepcion("El expediente no existe."));
    }

    public static boolean dixonPuedeEmitir(DixonExpediente dixonExpediente) {
        return dixonExpediente.getDixonGrupo() == DixonGrupoExpediente.TRAMITE && (dixonExpediente.getDixonPago() == null || dixonExpediente.getDixonPago().dixonEsPagado());
    }

    @Transactional
    public DixonExpediente dixonEmitir(String dixonCodigo) {
        DixonExpediente dixonExpediente = dixonExpediente(dixonCodigo);
        if (!dixonPuedeEmitir(dixonExpediente)) {
            throw new DixonNegocioExcepcion(dixonExpediente.getDixonPago() != null && !dixonExpediente.getDixonPago().dixonEsPagado() ? "Esperando pago" : "Ya atendido");
        }
        dixonExpediente.setDixonGrupo(DixonGrupoExpediente.LISTO);
        dixonExpediente.setDixonTono("success");
        dixonExpediente.setDixonDestacado(false);
        dixonExpediente.setDixonEstado("Documento emitido · Listo para recoger");
        dixonExpediente.setDixonPaso(4);
        dixonExpediente.setDixonRotuloIcono("LISTO");
        if (dixonExpediente.getDixonPago() != null && dixonExpediente.getDixonTramite() != null && dixonExpediente.getDixonTramite().dixonGeneraDocumento()) {
            dixonExpediente.dixonPonerAcciones("primary", "gen-sol", "Ver documento generado", "secondary", "rcpt-sol", "Ver comprobante",
                    "tertiary", "route-sol", "Seguimiento en vivo");
        } else {
            dixonExpediente.dixonPonerAcciones("primary", "route-sol", "Seguimiento en vivo");
        }
        dixonExpedientes.save(dixonExpediente);
        if (dixonExpediente.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Tu trámite " + dixonExpediente.getDixonCodigo() + " (" + dixonExpediente.getDixonTitulo()
                    + ") está listo para recoger en Mesa de Partes.", "solicitudes");
        }
        dixonSeguimiento.dixonRegistrar(dixonExpediente, "Andra: tu " + dixonExpediente.getDixonTitulo() + " (" + dixonExpediente.getDixonCodigo() + ") está listo. Recógelo en la ventanilla "
                + DixonSeguimientoServicio.dixonVentanilla(dixonExpediente.getDixonCodigo()) + " de Mesa de Partes con tu DNI.");
        return dixonExpediente;
    }

    @Transactional
    public DixonExpediente dixonObservar(String dixonCodigo, String dixonMotivo) {
        if (dixonMotivo == null || dixonMotivo.trim().length() < 15) {
            throw new DixonNegocioExcepcion("Escribe al menos 15 caracteres.");
        }
        DixonExpediente dixonExpediente = dixonExpediente(dixonCodigo);
        if (dixonExpediente.getDixonGrupo() != DixonGrupoExpediente.TRAMITE) {
            throw new DixonNegocioExcepcion("Solo se observan trámites en curso.");
        }
        String dixonTexto = dixonMotivo.trim();
        dixonExpediente.setDixonGrupo(DixonGrupoExpediente.OBSERVADO);
        dixonExpediente.setDixonTono("error");
        dixonExpediente.setDixonDestacado(true);
        dixonExpediente.setDixonEstado("Observado por Mesa de Partes");
        dixonExpediente.setDixonObservacion(dixonTexto);
        dixonExpediente.setDixonRotuloIcono("ALERTA");
        dixonExpediente.setDixonIcono("alert");
        dixonExpediente.dixonPonerAcciones("primary", "fix-sol", "Subsanar Documento", "secondary", "obs-sol", "Ver Observaciones",
                "tertiary", "ask-sol", "Consultar a Secretaría");
        dixonExpedientes.save(dixonExpediente);
        if (dixonExpediente.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Tu trámite " + dixonExpediente.getDixonCodigo() + " fue observado: " + dixonTexto, "solicitudes");
        }
        dixonSeguimiento.dixonRegistrar(dixonExpediente, "Andra: tu trámite " + dixonExpediente.getDixonCodigo() + " tiene una observación: " + dixonTexto);
        return dixonExpediente;
    }

    @Transactional
    public DixonExpediente dixonEntregar(String dixonCodigo) {
        DixonExpediente dixonExpediente = dixonExpediente(dixonCodigo);
        dixonExpediente.setDixonEstado("Entregado al solicitante");
        dixonExpediente.setDixonTono("neutral");
        dixonExpedientes.save(dixonExpediente);
        if (dixonExpediente.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Registramos la entrega de " + dixonExpediente.getDixonTitulo() + ".", "solicitudes");
        }
        dixonSeguimiento.dixonRegistrar(dixonExpediente, "Andra: entregamos tu " + dixonExpediente.getDixonTitulo() + " (" + dixonExpediente.getDixonCodigo() + "). ¡Gracias!");
        return dixonExpediente;
    }

    @Transactional
    public DixonPago dixonReembolsar(String dixonCodigoPago) {
        DixonPago dixonPago = dixonPagos.findByDixonCodigo(dixonCodigoPago).orElseThrow(() -> new DixonNegocioExcepcion("La orden no existe."));
        if (!dixonPago.dixonEsPagado()) {
            throw new DixonNegocioExcepcion("Solo se reembolsan pagos confirmados.");
        }
        dixonPago.setDixonEstado(DixonEstadoPago.REEMBOLSADO);
        dixonPagos.save(dixonPago);
        if (dixonPago.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Se reembolsó tu pago " + dixonPago.getDixonBoleta() + " (" + DixonMontos.dixonSoles(dixonPago.dixonObtenerTotal()) + ").", "pay");
        }
        return dixonPago;
    }

    @Transactional
    public DixonReserva dixonAprobarReserva(Long dixonId) {
        DixonReserva dixonReserva = dixonReservas.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("La reserva no existe."));
        dixonReserva.setDixonEstado(DixonEstadoReserva.APROBADO);
        dixonReservas.save(dixonReserva);
        if (dixonReserva.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Tu reserva de " + dixonReserva.getDixonEspacio().getDixonNombre() + " para el "
                    + DixonFechas.dixonDmy(dixonReserva.getDixonFecha()) + " fue aprobada.", "reservas");
        }
        return dixonReserva;
    }

    @Transactional
    public DixonReserva dixonRechazarReserva(Long dixonId, String dixonMotivo) {
        if (dixonMotivo == null || dixonMotivo.trim().length() < 10) {
            throw new DixonNegocioExcepcion("Escribe al menos 10 caracteres.");
        }
        DixonReserva dixonReserva = dixonReservas.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("La reserva no existe."));
        dixonReserva.setDixonEstado(DixonEstadoReserva.RECHAZADO);
        dixonReserva.setDixonNota(dixonMotivo.trim());
        dixonReservas.save(dixonReserva);
        if (dixonReserva.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Tu reserva de " + dixonReserva.getDixonEspacio().getDixonNombre() + " fue rechazada: " + dixonMotivo.trim(), "reservas");
        }
        return dixonReserva;
    }

    @Transactional
    public DixonPrestamo dixonAprobarPrestamo(Long dixonId) {
        DixonPrestamo dixonPrestamo = dixonPrestamos.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("El préstamo no existe."));
        dixonPrestamo.setDixonEstado(DixonEstadoPrestamo.APROBADO);
        dixonPrestamos.save(dixonPrestamo);
        if (dixonPrestamo.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Tu préstamo " + dixonPrestamo.getDixonCodigo() + " fue aprobado. Recógelo en el punto de entrega.", "equipos");
        }
        return dixonPrestamo;
    }

    @Transactional
    public DixonPrestamo dixonRechazarPrestamo(Long dixonId) {
        DixonPrestamo dixonPrestamo = dixonPrestamos.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("El préstamo no existe."));
        dixonPrestamo.setDixonEstado(DixonEstadoPrestamo.RECHAZADO);
        dixonPrestamos.save(dixonPrestamo);
        if (dixonPrestamo.getDixonPersona().isDixonPrincipal()) {
            dixonNotificaciones.dixonNotificarEstudiante("Tu préstamo " + dixonPrestamo.getDixonCodigo() + " fue rechazado.", "equipos");
        }
        return dixonPrestamo;
    }

    @Transactional
    public DixonPrestamo dixonDevolverPrestamo(Long dixonId) {
        DixonPrestamo dixonPrestamo = dixonPrestamos.findById(dixonId).orElseThrow(() -> new DixonNegocioExcepcion("El préstamo no existe."));
        dixonPrestamo.setDixonEstado(DixonEstadoPrestamo.DEVUELTO);
        String[] dixonPartes = dixonPrestamo.getDixonDetalle().split("•");
        dixonPrestamo.setDixonDetalle("Devuelto con éxito: " + DixonFechas.dixonDmy(DixonFechas.dixonHoy()) + " · " + dixonPartes[dixonPartes.length - 1].trim());
        return dixonPrestamos.save(dixonPrestamo);
    }

    @Transactional
    public DixonPrestamoActivo dixonDevolverCustodia() {
        DixonPrestamoActivo dixonPrestamoActivo = dixonActivos.findFirstByOrderByDixonIdDesc().filter(dixonPosicionX -> !dixonPosicionX.isDixonDevuelto())
                .orElseThrow(() -> new DixonNegocioExcepcion("No hay préstamos en custodia."));
        dixonPrestamoActivo.setDixonDevuelto(true);
        dixonActivos.save(dixonPrestamoActivo);
        dixonPrestamos.save(DixonPrestamo.builder().dixonCodigo(dixonPrestamoActivo.getDixonCodigo()).dixonClave("h" + UUID.randomUUID().toString().substring(0, 6))
                .dixonPersona(dixonPrestamoActivo.getDixonPersona()).dixonNombre(dixonPrestamoActivo.getDixonEquipo()).dixonDetalle("Devuelto con éxito: " + DixonFechas.dixonDmy(DixonFechas.dixonHoy()) + " • Sede Central")
                .dixonEstado(DixonEstadoPrestamo.DEVUELTO).dixonSede(dixonCatalogo.dixonSede("central").orElseThrow()).build());
        dixonNotificaciones.dixonNotificarEstudiante("Registramos la devolución del " + dixonPrestamoActivo.getDixonEquipo() + ".", "equipos");
        return dixonPrestamoActivo;
    }

    public boolean dixonEsLaboratorio(DixonPrestamo dixonPrestamo) {
        return dixonCatalogo.dixonEquipos().stream().anyMatch(dixonEquipo -> "laboratorio".equals(dixonEquipo.getDixonCategoria().getDixonCodigo())
                && dixonPrestamo.getDixonNombre().contains(dixonEquipo.getDixonNombre().split(" ")[0]));
    }

    public DixonPersona dixonPersona(String dixonCodigo) {
        return dixonCatalogo.dixonPersona(dixonCodigo).orElse(null);
    }

    public DixonConexionServicio dixonConexion() {
        return dixonConexion;
    }
}
