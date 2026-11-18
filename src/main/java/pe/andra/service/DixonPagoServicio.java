package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.DixonExpedienteRepositorio;
import pe.andra.repository.DixonMatriculaRepositorio;
import pe.andra.repository.DixonPagoRepositorio;
import pe.andra.util.DixonDiasHabiles;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;
import pe.andra.util.DixonTarjetaJemm;
import pe.andra.util.DixonValidacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class DixonPagoServicio {

    public record DixonRecojo(LocalDate dixonFecha, String dixonTexto) {
    }

    public static final List<String[]> DIXON_PREDEFINIDOS = List.of(new String[]{"mat", "Matrícula"},
            new String[]{"doc", "Documentos y carné"}, new String[]{"week", "Vence esta semana"},
            new String[]{"gt50", "Más de S/ 50"});
    public static final List<String> DIXON_USOS = List.of("Trámite personal", "Prácticas preprofesionales", "Beca o subvención",
            "Trabajo", "Convalidación o traslado", "Otro");

    private static final Random DIXON_AZAR_JEMM = new Random();

    private final DixonPagoRepositorio dixonPagos;
    private final DixonExpedienteRepositorio dixonExpedientes;
    private final DixonMatriculaRepositorio dixonMatriculas;
    private final DixonSesionServicio dixonSesion;
    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonNotificacionServicio dixonNotificaciones;
    private final DixonSeguimientoServicio dixonSeguimiento;
    private final DixonConexionServicio dixonConexion;

    public static DixonRecojo dixonRecojo(DixonTramite dixonTramite, LocalDateTime dixonPagado) {
        LocalDate dixonDia = dixonPagado.toLocalDate();
        int dixonHora = dixonPagado.getHour();
        if (dixonTramite == null || "inmediato".equals(dixonTramite.getDixonEntrega())) {
            return new DixonRecojo(null, "Habilitado al instante. No requiere recojo.");
        }
        if ("mismo".equals(dixonTramite.getDixonEntrega())) {
            boolean dixonMismoDia = dixonHora < 15 && DixonDiasHabiles.dixonEsHabil(dixonDia);
            LocalDate dixonFecha = dixonMismoDia ? dixonDia : DixonDiasHabiles.dixonSumar(dixonDia, 1);
            return new DixonRecojo(dixonFecha, dixonMismoDia ? "Hoy, " + DixonFechas.dixonLargo(dixonFecha) + ", desde las 16:00" : DixonFechas.dixonLargo(dixonFecha) + ", desde las 09:00");
        }
        int dixonCantidad = "carne".equals(dixonTramite.getDixonEntrega()) ? 10 : Integer.parseInt(dixonTramite.getDixonEntrega());
        LocalDate dixonFecha = DixonDiasHabiles.dixonSumar(dixonDia, dixonCantidad);
        return new DixonRecojo(dixonFecha, DixonFechas.dixonLargo(dixonFecha) + " (" + dixonCantidad + " días hábiles)");
    }

    public static BigDecimal dixonTotal(Collection<DixonPago> dixonLista) {
        return dixonLista.stream().map(DixonPago::dixonObtenerTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal dixonTotalOrden(List<DixonElementoPago> dixonElementos) {
        return dixonElementos.stream().map(DixonElementoPago::dixonObtenerImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<DixonPago> dixonMios() {
        return dixonPagos.findByDixonPersonaOrderByDixonCreadoDesc(dixonSesion.dixonEstudiante());
    }

    @Transactional(readOnly = true)
    public List<DixonPago> dixonTodos() {
        return dixonPagos.findAllByOrderByDixonCreadoDesc();
    }

    @Transactional(readOnly = true)
    public DixonPago dixonObtener(String dixonCodigo) {
        return dixonPagos.findByDixonCodigo(dixonCodigo).orElseThrow(() -> new DixonNegocioExcepcion("La orden no existe."));
    }

    @Transactional(readOnly = true)
    public List<DixonPago> dixonPendientes() {
        return dixonMios().stream().filter(DixonPago::dixonEsPendiente).toList();
    }

    @Transactional(readOnly = true)
    public Optional<DixonPago> dixonMasProximo() {
        return dixonPendientes().stream().filter(dixonPago -> dixonPago.getDixonVence() != null).min(Comparator.comparing(DixonPago::getDixonVence));
    }

    @Transactional(readOnly = true)
    public BigDecimal dixonPagadoEnAnio(int dixonAnio) {
        return dixonTotal(dixonMios().stream().filter(dixonPago -> dixonPago.dixonEsPagado() && dixonPago.getDixonPagado() != null && dixonPago.getDixonPagado().getYear() == dixonAnio).toList());
    }

    @Transactional(readOnly = true)
    public DixonMatricula dixonMatricula() {
        return dixonMatriculas.findFirstByOrderByDixonIdDesc().orElseThrow(() -> new DixonNegocioExcepcion("No hay matrícula."));
    }

    public Map<String, Predicate<DixonPago>> dixonPredicados() {
        LocalDate dixonSemana = DixonFechas.dixonHoy().plusDays(7);
        return Map.of("mat", dixonPago -> dixonPago.getDixonTipo() == DixonTipoPago.MATRICULA,
                "doc", dixonPago -> dixonPago.getDixonTipo() == DixonTipoPago.DOCUMENTO || dixonPago.getDixonTipo() == DixonTipoPago.CARNE,
                "week", dixonPago -> dixonPago.dixonEsPendiente() && dixonPago.getDixonVence() != null && !dixonPago.getDixonVence().isAfter(dixonSemana),
                "gt50", dixonPago -> dixonPago.dixonObtenerTotal().compareTo(BigDecimal.valueOf(50)) > 0);
    }

    public static String dixonEstadoVisible(DixonPago dixonPago) {
        if (dixonPago.getDixonEstado() == DixonEstadoPago.PENDIENTE && dixonPago.getDixonVence() != null && dixonPago.getDixonVence().isBefore(DixonFechas.dixonHoy())) {
            return "Vencido";
        }
        return dixonPago.getDixonEstado().dixonObtenerEtiqueta();
    }

    public static String dixonTonoEstado(String dixonEstado) {
        return switch (dixonEstado) {
            case "Pagado" -> "success";
            case "Vencido" -> "error";
            case "Por pagar" -> "warning";
            default -> "neutral";
        };
    }

    public Map<String, String> dixonValidarSolicitante(DixonTramite dixonTramite, DixonDatosSolicitante dixonDatosSolicitante, boolean dixonTieneFoto) {
        Map<String, String> dixonMapa = new LinkedHashMap<>();
        if (dixonBlanco(dixonDatosSolicitante.getDixonNombre())) {
            dixonMapa.put("nombre", "Escribe tus nombres y apellidos.");
        }
        if (dixonBlanco(dixonDatosSolicitante.getDixonDni())) {
            dixonMapa.put("dni", "Escribe tu DNI.");
        } else if (!DixonValidacion.dixonDni(dixonDatosSolicitante.getDixonDni())) {
            dixonMapa.put("dni", "El DNI debe tener 8 dígitos.");
        }
        boolean dixonPideCodigo = List.of("Estudiante", "Egresado").contains(dixonDatosSolicitante.getDixonTipo());
        if (dixonPideCodigo && dixonBlanco(dixonDatosSolicitante.getDixonCodigo())) {
            dixonMapa.put("codigo", "Escribe tu código universitario.");
        }
        if (List.of("Estudiante", "Egresado", "Docente").contains(dixonDatosSolicitante.getDixonTipo()) && dixonBlanco(dixonDatosSolicitante.getDixonEscuela())) {
            dixonMapa.put("escuela", "Elige tu escuela.");
        }
        if (dixonBlanco(dixonDatosSolicitante.getDixonCorreo())) {
            dixonMapa.put("correo", "Escribe tu correo.");
        } else if (!DixonValidacion.dixonCorreo(dixonDatosSolicitante.getDixonCorreo())) {
            dixonMapa.put("correo", "Escribe un correo válido.");
        }
        if (dixonMapa.isEmpty() && !dixonTramite.dixonAdmite(dixonDatosSolicitante.getDixonTipo())) {
            dixonMapa.put("publico", "Este trámite es para: " + String.join(", ", dixonTramite.dixonObtenerListaPublicos()) + ".");
        }
        if (dixonMapa.isEmpty() && dixonTramite.dixonEsCarne() && !dixonTieneFoto) {
            dixonMapa.put("foto", "Sube tu foto para el carné.");
        }
        return dixonMapa;
    }

    public static Map<String, String> dixonValidarYape(String dixonCelular, String dixonCodigo) {
        Map<String, String> dixonMapa = new LinkedHashMap<>();
        if (dixonBlanco(dixonCelular)) {
            dixonMapa.put("celular", "Escribe tu celular.");
        } else if (!DixonValidacion.dixonCelular(dixonCelular)) {
            dixonMapa.put("celular", "Celular de 9 dígitos que empiece con 9.");
        }
        if (dixonBlanco(dixonCodigo)) {
            dixonMapa.put("codigo", "Escribe el código de 6 dígitos.");
        } else if (!DixonValidacion.dixonCodigoAprobacion(dixonCodigo)) {
            dixonMapa.put("codigo", "El código tiene 6 dígitos.");
        }
        return dixonMapa;
    }

    public static Map<String, String> dixonValidarPlin(String dixonCelular) {
        Map<String, String> dixonMapa = new LinkedHashMap<>();
        if (dixonBlanco(dixonCelular)) {
            dixonMapa.put("celular", "Escribe tu celular.");
        } else if (!DixonValidacion.dixonCelular(dixonCelular)) {
            dixonMapa.put("celular", "Celular de 9 dígitos que empiece con 9.");
        }
        return dixonMapa;
    }

    public static Map<String, String> dixonValidarTarjeta(String dixonNumero, String dixonTitular, String dixonVencimiento, String dixonCvv,
                                                     String dixonCorreo, YearMonth dixonMesActual) {
        Map<String, String> dixonMapa = new LinkedHashMap<>();
        if (dixonBlanco(dixonNumero)) {
            dixonMapa.put("numero", "Escribe el número de tu tarjeta.");
        }
        if (dixonBlanco(dixonTitular)) {
            dixonMapa.put("titular", "Escribe el nombre como aparece en la tarjeta.");
        }
        if (dixonBlanco(dixonVencimiento)) {
            dixonMapa.put("vencimiento", "Escribe el vencimiento.");
        }
        if (dixonBlanco(dixonCvv)) {
            dixonMapa.put("cvv", "Escribe el CVV.");
        }
        if (dixonBlanco(dixonCorreo)) {
            dixonMapa.put("correo", "Escribe un correo.");
        }
        if (!dixonMapa.isEmpty()) {
            return dixonMapa;
        }
        if (DixonTarjetaJemm.dixonMarca(dixonNumero) == null) {
            dixonMapa.put("numero", "Usa una tarjeta Visa, Mastercard, American Express o Diners Club.");
        } else if (!DixonTarjetaJemm.dixonLuhn(dixonNumero)) {
            dixonMapa.put("numero", "El número de tarjeta no es válido. Revísalo.");
        } else if (!DixonTarjetaJemm.dixonVencimientoValido(dixonVencimiento, dixonMesActual)) {
            dixonMapa.put("vencimiento", "La fecha de vencimiento no es válida.");
        } else if (!DixonValidacion.dixonCvv(dixonCvv)) {
            dixonMapa.put("cvv", "El CVV tiene 3 o 4 dígitos.");
        }
        return dixonMapa;
    }

    private static boolean dixonBlanco(String dixonTexto) {
        return dixonTexto == null || dixonTexto.isBlank();
    }

    public static List<DixonElementoPago> dixonElementosPara(DixonTramite dixonTramite, int dixonCantidad) {
        int dixonNumero = dixonTramite.isDixonPorCantidad() ? Math.max(1, Math.min(20, dixonCantidad)) : 1;
        String dixonConcepto = dixonTramite.getDixonNombre() + (dixonTramite.isDixonPorCantidad() && dixonNumero > 1 ? " (" + dixonNumero + " cursos)" : "");
        return new ArrayList<>(List.of(new DixonElementoPago(dixonConcepto, dixonNumero, dixonTramite.getDixonMonto())));
    }

    @Transactional(readOnly = true)
    public String dixonSiguienteCodigoOrden() {
        int dixonCantidad = 5600 + (int) dixonPagos.count();
        String dixonTexto;
        do {
            dixonTexto = "PAG-2026-00" + dixonCantidad++;
        } while (dixonPagos.findByDixonCodigo(dixonTexto).isPresent());
        return dixonTexto;
    }

    @Transactional
    public DixonPago dixonCrearOrden(DixonTramite dixonTramite, DixonDatosSolicitante dixonSol, int dixonCantidad, String dixonMotivo, String dixonSedeCodigo, byte[] dixonFoto) {
        DixonSede dixonSede = dixonCatalogo.dixonSede(dixonSedeCodigo).orElseThrow();
        List<DixonElementoPago> dixonElementos = dixonElementosPara(dixonTramite, dixonCantidad);
        String dixonOrdenJemm = dixonSiguienteCodigoOrden();
        DixonTipoPago dixonTipo = dixonTramite.dixonEsCarne() ? DixonTipoPago.CARNE : dixonTramite.dixonGeneraDocumento() ? DixonTipoPago.DOCUMENTO : DixonTipoPago.TRAMITE;
        DixonPago dixonPago = dixonPagos.save(DixonPago.builder().dixonCodigo(dixonOrdenJemm).dixonPersona(dixonSesion.dixonEstudiante()).dixonTipo(dixonTipo).dixonTramite(dixonTramite)
                .dixonConcepto(dixonTramite.getDixonNombre()).dixonEstado(DixonEstadoPago.PENDIENTE).dixonCreado(DixonFechas.dixonAhora()).dixonVence(DixonFechas.dixonHoy().plusDays(3))
                .dixonSede(dixonSede).dixonSolicitante(dixonSol).dixonMotivo(dixonMotivo).dixonFoto(dixonFoto).dixonElementos(dixonElementos).build());
        String dixonCodigoExp = dixonSiguienteCodigoExpediente("SOL-2026-0", 85400);
        DixonExpediente dixonExpediente = DixonExpediente.builder().dixonCodigo(dixonCodigoExp).dixonPersona(dixonSesion.dixonEstudiante()).dixonSede(dixonSede).dixonClase(DixonClaseExpediente.DOC)
                .dixonTramite(dixonTramite).dixonPago(dixonPago).dixonGrupo(DixonGrupoExpediente.TRAMITE).dixonIcono(dixonTramite.dixonEsCarne() ? "idcard" : "doc").dixonRotuloIcono("PAGO")
                .dixonTono("warning").dixonEstado("Pago pendiente en Andra Pay").dixonTitulo(dixonTramite.getDixonNombre())
                .dixonDescripcion("Documento a nombre de " + dixonSol.getDixonNombre() + " (" + dixonSol.getDixonTipo() + "). Uso: " + dixonMotivo + ".")
                .dixonPaso(1).dixonSolicitante(dixonSol).dixonFecha(DixonFechas.dixonAhora()).build();
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Fecha de registro", "Hoy, " + DixonFechas.dixonDmy(DixonFechas.dixonHoy())));
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Derecho / Tasa", DixonMontos.dixonSoles(dixonPago.dixonObtenerTotal()) + " (Pendiente)"));
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Recojo", "Mesa de Partes · " + dixonSede.getDixonNombreCompleto()));
        dixonExpediente.dixonPonerAcciones("primary", "pay-sol", "Pagar en Andra Pay", "secondary", "route-sol", "Seguimiento en vivo",
                "danger", "desist-sol", "Anular trámite");
        dixonExpedientes.save(dixonExpediente);
        dixonNotificaciones.dixonNotificarAdministrador("Nueva orden " + dixonOrdenJemm + ": " + dixonTramite.getDixonNombre() + " (" + dixonSol.getDixonNombre() + ").", "a-tramites");
        dixonConexion.dixonEncolar("Solicitar " + dixonTramite.getDixonNombre().toLowerCase());
        return dixonPago;
    }

    @Transactional(readOnly = true)
    public String dixonSiguienteCodigoExpediente(String dixonPrefijo, int dixonBase) {
        int dixonCantidad = dixonBase + (int) dixonExpedientes.count();
        String dixonTexto;
        do {
            dixonTexto = dixonPrefijo + dixonCantidad++;
        } while (dixonExpedientes.existsByDixonCodigo(dixonTexto));
        return dixonTexto;
    }

    public static String dixonNuevaOperacion(DixonMetodoPago dixonMetodoPago) {
        return dixonMetodoPago.dixonObtenerPrefijo() + (10000000 + DIXON_AZAR_JEMM.nextInt(89999999));
    }

    @Transactional
    public DixonPago dixonCompletar(String dixonCodigo, DixonMetodoPago dixonMetodo) {
        if (dixonConexion.dixonSinConexion()) {
            throw new DixonNegocioExcepcion("Necesitas conexión para pagar");
        }
        DixonPago dixonPago = dixonObtener(dixonCodigo);
        if (dixonPago.dixonEsPagado()) {
            return dixonPago;
        }
        if (!dixonPago.dixonEsPendiente()) {
            throw new DixonNegocioExcepcion("Esta orden ya no se puede pagar.");
        }
        DixonTramite dixonTramite = dixonPago.getDixonTramite();
        dixonPago.setDixonEstado(DixonEstadoPago.PAGADO);
        dixonPago.setDixonMetodo(dixonMetodo);
        dixonPago.setDixonPagado(DixonFechas.dixonAhora());
        dixonPago.setDixonOperacion(dixonNuevaOperacion(dixonMetodo));
        dixonPago.setDixonBoleta("B001-00" + (4000 + dixonPagos.countByDixonBoletaIsNotNull()));
        DixonRecojo dixonRecojo = dixonRecojo(dixonTramite, dixonPago.getDixonPagado());
        dixonPago.setDixonRecojoFecha(dixonRecojo.dixonFecha());
        dixonPago.setDixonRecojoTexto(dixonRecojo.dixonTexto());
        dixonPagos.save(dixonPago);
        String dixonMonto = DixonMontos.dixonSoles(dixonPago.dixonObtenerTotal());
        dixonExpedientes.findFirstByDixonPago(dixonPago).ifPresent(dixonSol -> {
            boolean dixonListo = dixonTramite != null && "mismo".equals(dixonTramite.getDixonEntrega());
            boolean dixonInmediato = dixonTramite != null && "inmediato".equals(dixonTramite.getDixonEntrega());
            dixonSol.setDixonTono("success");
            dixonSol.setDixonRotuloIcono("PAGADO");
            dixonSol.dixonPonerDato(1, "Derecho / Tasa", dixonMonto + " (Pagado con " + dixonMetodo.dixonObtenerEtiqueta() + ")");
            dixonSol.dixonPonerDato(2, "Recojo", dixonRecojo.dixonFecha() != null ? DixonFechas.dixonDmy(dixonRecojo.dixonFecha()) + " · " + dixonPago.getDixonSede().getDixonNombre() : "No requiere");
            dixonSol.setDixonGrupo(dixonListo || dixonInmediato ? DixonGrupoExpediente.LISTO : DixonGrupoExpediente.TRAMITE);
            dixonSol.setDixonEstado(dixonInmediato ? "Pagado · Habilitado" : dixonListo ? "Pagado · Listo para recoger" : "Pagado · En emisión");
            dixonSol.setDixonPaso(dixonListo || dixonInmediato ? 4 : 2);
            dixonSol.getDixonEtiquetas().clear();
            dixonSol.getDixonEtiquetas().add("Comprobante " + dixonPago.getDixonBoleta());
            dixonSol.getDixonEtiquetas().add("Operación " + dixonPago.getDixonOperacion());
            if (dixonTramite != null && dixonTramite.dixonGeneraDocumento()) {
                dixonSol.dixonPonerAcciones("primary", "gen-sol", "Ver documento generado", "secondary", "rcpt-sol", "Ver comprobante",
                        "tertiary", "route-sol", "Seguimiento en vivo");
            } else {
                dixonSol.dixonPonerAcciones("primary", "rcpt-sol", "Ver comprobante", "secondary", "route-sol", "Seguimiento en vivo");
            }
            dixonExpedientes.save(dixonSol);
            dixonSeguimiento.dixonRegistrar(dixonSol, "Andra: recibimos tu pago de " + dixonMonto + " por " + dixonPago.getDixonConcepto() + ". "
                    + (dixonRecojo.dixonFecha() != null ? "Recojo: " + dixonRecojo.dixonTexto() + "." : ""));
        });
        DixonMatricula dixonMatricula = dixonMatricula();
        if (dixonPago.getDixonTipo() == DixonTipoPago.MATRICULA && dixonMatricula.getDixonPago().getDixonId().equals(dixonPago.getDixonId())) {
            dixonMatricula.setDixonEstado(DixonEstadoMatricula.CONFIRMADA);
            dixonMatriculas.save(dixonMatricula);
            dixonNotificaciones.dixonNotificarEstudiante("Tu matrícula " + dixonMatricula.getDixonPeriodo() + " quedó confirmada. Pago " + dixonPago.getDixonBoleta() + ".", "pay");
        } else {
            dixonNotificaciones.dixonNotificarEstudiante("Pago confirmado: " + dixonPago.getDixonConcepto() + " (" + dixonMonto + ")."
                    + (dixonRecojo.dixonFecha() != null ? " Recojo: " + DixonFechas.dixonDmy(dixonRecojo.dixonFecha()) + "." : ""), "pay");
        }
        dixonNotificaciones.dixonNotificarAdministrador("Pago confirmado por " + dixonMetodo.dixonObtenerEtiqueta() + ": " + dixonPago.getDixonConcepto() + " · " + dixonMonto + ".", "a-pagos");
        return dixonPago;
    }

    @Transactional
    public void dixonAnular(DixonPago dixonPago) {
        if (dixonPago != null && dixonPago.dixonEsPendiente()) {
            DixonPago dixonActual = dixonObtener(dixonPago.getDixonCodigo());
            dixonActual.setDixonEstado(DixonEstadoPago.ANULADO);
            dixonPagos.save(dixonActual);
        }
    }
}
