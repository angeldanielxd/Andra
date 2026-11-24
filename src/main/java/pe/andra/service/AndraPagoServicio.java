package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.AndraExpedienteRepositorio;
import pe.andra.repository.AndraMatriculaRepositorio;
import pe.andra.repository.AndraPagoRepositorio;
import pe.andra.util.AndraDiasHabiles;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;
import pe.andra.util.AndraTarjetaJemm;
import pe.andra.util.AndraValidacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class AndraPagoServicio {

    public record AndraRecojo(LocalDate andraFecha, String andraTexto) {
    }

    public static final List<String[]> ANDRA_PREDEFINIDOS = List.of(new String[]{"mat", "Matrícula"},
            new String[]{"doc", "Documentos y carné"}, new String[]{"week", "Vence esta semana"},
            new String[]{"gt50", "Más de S/ 50"});
    public static final List<String> ANDRA_USOS = List.of("Trámite personal", "Prácticas preprofesionales", "Beca o subvención",
            "Trabajo", "Convalidación o traslado", "Otro");

    private static final Random ANDRA_AZAR_JEMM = new Random();

    private final AndraPagoRepositorio andraPagos;
    private final AndraExpedienteRepositorio andraExpedientes;
    private final AndraMatriculaRepositorio andraMatriculas;
    private final AndraSesionServicio andraSesion;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraNotificacionServicio andraNotificaciones;
    private final AndraSeguimientoServicio andraSeguimiento;
    private final AndraConexionServicio andraConexion;

    public static AndraRecojo andraRecojo(AndraTramite andraTramite, LocalDateTime andraPagado) {
        LocalDate andraDia = andraPagado.toLocalDate();
        int andraHora = andraPagado.getHour();
        if (andraTramite == null || "inmediato".equals(andraTramite.getAndraEntrega())) {
            return new AndraRecojo(null, "Habilitado al instante. No requiere recojo.");
        }
        if ("mismo".equals(andraTramite.getAndraEntrega())) {
            boolean andraMismoDia = andraHora < 15 && AndraDiasHabiles.andraEsHabil(andraDia);
            LocalDate andraFecha = andraMismoDia ? andraDia : AndraDiasHabiles.andraSumar(andraDia, 1);
            return new AndraRecojo(andraFecha, andraMismoDia ? "Hoy, " + AndraFechas.andraLargo(andraFecha) + ", desde las 16:00" : AndraFechas.andraLargo(andraFecha) + ", desde las 09:00");
        }
        int andraCantidad = "carne".equals(andraTramite.getAndraEntrega()) ? 10 : Integer.parseInt(andraTramite.getAndraEntrega());
        LocalDate andraFecha = AndraDiasHabiles.andraSumar(andraDia, andraCantidad);
        return new AndraRecojo(andraFecha, AndraFechas.andraLargo(andraFecha) + " (" + andraCantidad + " días hábiles)");
    }

    public static BigDecimal andraTotal(Collection<AndraPago> andraLista) {
        return andraLista.stream().map(AndraPago::andraObtenerTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal andraTotalOrden(List<AndraElementoPago> andraElementos) {
        return andraElementos.stream().map(AndraElementoPago::andraObtenerImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<AndraPago> andraMios() {
        return andraPagos.findByAndraPersonaOrderByAndraCreadoDesc(andraSesion.andraEstudiante());
    }

    @Transactional(readOnly = true)
    public List<AndraPago> andraTodos() {
        return andraPagos.findAllByOrderByAndraCreadoDesc();
    }

    @Transactional(readOnly = true)
    public AndraPago andraObtener(String andraCodigo) {
        return andraPagos.findByAndraCodigo(andraCodigo).orElseThrow(() -> new AndraNegocioExcepcion("La orden no existe."));
    }

    @Transactional(readOnly = true)
    public List<AndraPago> andraPendientes() {
        return andraMios().stream().filter(AndraPago::andraEsPendiente).toList();
    }

    @Transactional(readOnly = true)
    public Optional<AndraPago> andraMasProximo() {
        return andraPendientes().stream().filter(andraPago -> andraPago.getAndraVence() != null).min(Comparator.comparing(AndraPago::getAndraVence));
    }

    @Transactional(readOnly = true)
    public BigDecimal andraPagadoEnAnio(int andraAnio) {
        return andraTotal(andraMios().stream().filter(andraPago -> andraPago.andraEsPagado() && andraPago.getAndraPagado() != null && andraPago.getAndraPagado().getYear() == andraAnio).toList());
    }

    @Transactional(readOnly = true)
    public AndraMatricula andraMatricula() {
        return andraMatriculas.findFirstByOrderByAndraIdDesc().orElseThrow(() -> new AndraNegocioExcepcion("No hay matrícula."));
    }

    public Map<String, Predicate<AndraPago>> andraPredicados() {
        LocalDate andraSemana = AndraFechas.andraHoy().plusDays(7);
        return Map.of("mat", andraPago -> andraPago.getAndraTipo() == AndraTipoPago.MATRICULA,
                "doc", andraPago -> andraPago.getAndraTipo() == AndraTipoPago.DOCUMENTO || andraPago.getAndraTipo() == AndraTipoPago.CARNE,
                "week", andraPago -> andraPago.andraEsPendiente() && andraPago.getAndraVence() != null && !andraPago.getAndraVence().isAfter(andraSemana),
                "gt50", andraPago -> andraPago.andraObtenerTotal().compareTo(BigDecimal.valueOf(50)) > 0);
    }

    public static String andraEstadoVisible(AndraPago andraPago) {
        if (andraPago.getAndraEstado() == AndraEstadoPago.PENDIENTE && andraPago.getAndraVence() != null && andraPago.getAndraVence().isBefore(AndraFechas.andraHoy())) {
            return "Vencido";
        }
        return andraPago.getAndraEstado().andraObtenerEtiqueta();
    }

    public static String andraTonoEstado(String andraEstado) {
        return switch (andraEstado) {
            case "Pagado" -> "success";
            case "Vencido" -> "error";
            case "Por pagar" -> "warning";
            default -> "neutral";
        };
    }

    public Map<String, String> andraValidarSolicitante(AndraTramite andraTramite, AndraDatosSolicitante andraDatosSolicitante, boolean andraTieneFoto) {
        Map<String, String> andraMapa = new LinkedHashMap<>();
        if (andraBlanco(andraDatosSolicitante.getAndraNombre())) {
            andraMapa.put("nombre", "Escribe tus nombres y apellidos.");
        }
        if (andraBlanco(andraDatosSolicitante.getAndraDni())) {
            andraMapa.put("dni", "Escribe tu DNI.");
        } else if (!AndraValidacion.andraDni(andraDatosSolicitante.getAndraDni())) {
            andraMapa.put("dni", "El DNI debe tener 8 dígitos.");
        }
        boolean andraPideCodigo = List.of("Estudiante", "Egresado").contains(andraDatosSolicitante.getAndraTipo());
        if (andraPideCodigo && andraBlanco(andraDatosSolicitante.getAndraCodigo())) {
            andraMapa.put("codigo", "Escribe tu código universitario.");
        }
        if (List.of("Estudiante", "Egresado", "Docente").contains(andraDatosSolicitante.getAndraTipo()) && andraBlanco(andraDatosSolicitante.getAndraEscuela())) {
            andraMapa.put("escuela", "Elige tu escuela.");
        }
        if (andraBlanco(andraDatosSolicitante.getAndraCorreo())) {
            andraMapa.put("correo", "Escribe tu correo.");
        } else if (!AndraValidacion.andraCorreo(andraDatosSolicitante.getAndraCorreo())) {
            andraMapa.put("correo", "Escribe un correo válido.");
        }
        if (andraMapa.isEmpty() && !andraTramite.andraAdmite(andraDatosSolicitante.getAndraTipo())) {
            andraMapa.put("publico", "Este trámite es para: " + String.join(", ", andraTramite.andraObtenerListaPublicos()) + ".");
        }
        if (andraMapa.isEmpty() && andraTramite.andraEsCarne() && !andraTieneFoto) {
            andraMapa.put("foto", "Sube tu foto para el carné.");
        }
        return andraMapa;
    }

    public static Map<String, String> andraValidarYape(String andraCelular, String andraCodigo) {
        Map<String, String> andraMapa = new LinkedHashMap<>();
        if (andraBlanco(andraCelular)) {
            andraMapa.put("celular", "Escribe tu celular.");
        } else if (!AndraValidacion.andraCelular(andraCelular)) {
            andraMapa.put("celular", "Celular de 9 dígitos que empiece con 9.");
        }
        if (andraBlanco(andraCodigo)) {
            andraMapa.put("codigo", "Escribe el código de 6 dígitos.");
        } else if (!AndraValidacion.andraCodigoAprobacion(andraCodigo)) {
            andraMapa.put("codigo", "El código tiene 6 dígitos.");
        }
        return andraMapa;
    }

    public static Map<String, String> andraValidarPlin(String andraCelular) {
        Map<String, String> andraMapa = new LinkedHashMap<>();
        if (andraBlanco(andraCelular)) {
            andraMapa.put("celular", "Escribe tu celular.");
        } else if (!AndraValidacion.andraCelular(andraCelular)) {
            andraMapa.put("celular", "Celular de 9 dígitos que empiece con 9.");
        }
        return andraMapa;
    }

    public static Map<String, String> andraValidarTarjeta(String andraNumero, String andraTitular, String andraVencimiento, String andraCvv,
                                                     String andraCorreo, YearMonth andraMesActual) {
        Map<String, String> andraMapa = new LinkedHashMap<>();
        if (andraBlanco(andraNumero)) {
            andraMapa.put("numero", "Escribe el número de tu tarjeta.");
        }
        if (andraBlanco(andraTitular)) {
            andraMapa.put("titular", "Escribe el nombre como aparece en la tarjeta.");
        }
        if (andraBlanco(andraVencimiento)) {
            andraMapa.put("vencimiento", "Escribe el vencimiento.");
        }
        if (andraBlanco(andraCvv)) {
            andraMapa.put("cvv", "Escribe el CVV.");
        }
        if (andraBlanco(andraCorreo)) {
            andraMapa.put("correo", "Escribe un correo.");
        }
        if (!andraMapa.isEmpty()) {
            return andraMapa;
        }
        if (AndraTarjetaJemm.andraMarca(andraNumero) == null) {
            andraMapa.put("numero", "Usa una tarjeta Visa, Mastercard, American Express o Diners Club.");
        } else if (!AndraTarjetaJemm.andraLuhn(andraNumero)) {
            andraMapa.put("numero", "El número de tarjeta no es válido. Revísalo.");
        } else if (!AndraTarjetaJemm.andraVencimientoValido(andraVencimiento, andraMesActual)) {
            andraMapa.put("vencimiento", "La fecha de vencimiento no es válida.");
        } else if (!AndraValidacion.andraCvv(andraCvv)) {
            andraMapa.put("cvv", "El CVV tiene 3 o 4 dígitos.");
        }
        return andraMapa;
    }

    private static boolean andraBlanco(String andraTexto) {
        return andraTexto == null || andraTexto.isBlank();
    }

    public static List<AndraElementoPago> andraElementosPara(AndraTramite andraTramite, int andraCantidad) {
        int andraNumero = andraTramite.isAndraPorCantidad() ? Math.max(1, Math.min(20, andraCantidad)) : 1;
        String andraConcepto = andraTramite.getAndraNombre() + (andraTramite.isAndraPorCantidad() && andraNumero > 1 ? " (" + andraNumero + " cursos)" : "");
        return new ArrayList<>(List.of(new AndraElementoPago(andraConcepto, andraNumero, andraTramite.getAndraMonto())));
    }

    @Transactional(readOnly = true)
    public String andraSiguienteCodigoOrden() {
        int andraCantidad = 5600 + (int) andraPagos.count();
        String andraTexto;
        do {
            andraTexto = "PAG-2026-00" + andraCantidad++;
        } while (andraPagos.findByAndraCodigo(andraTexto).isPresent());
        return andraTexto;
    }

    @Transactional
    public AndraPago andraCrearOrden(AndraTramite andraTramite, AndraDatosSolicitante andraSol, int andraCantidad, String andraMotivo, String andraSedeCodigo, byte[] andraFoto) {
        AndraSede andraSede = andraCatalogo.andraSede(andraSedeCodigo).orElseThrow();
        List<AndraElementoPago> andraElementos = andraElementosPara(andraTramite, andraCantidad);
        String andraOrdenJemm = andraSiguienteCodigoOrden();
        AndraTipoPago andraTipo = andraTramite.andraEsCarne() ? AndraTipoPago.CARNE : andraTramite.andraGeneraDocumento() ? AndraTipoPago.DOCUMENTO : AndraTipoPago.TRAMITE;
        AndraPago andraPago = andraPagos.save(AndraPago.builder().andraCodigo(andraOrdenJemm).andraPersona(andraSesion.andraEstudiante()).andraTipo(andraTipo).andraTramite(andraTramite)
                .andraConcepto(andraTramite.getAndraNombre()).andraEstado(AndraEstadoPago.PENDIENTE).andraCreado(AndraFechas.andraAhora()).andraVence(AndraFechas.andraHoy().plusDays(3))
                .andraSede(andraSede).andraSolicitante(andraSol).andraMotivo(andraMotivo).andraFoto(andraFoto).andraElementos(andraElementos).build());
        String andraCodigoExp = andraSiguienteCodigoExpediente("SOL-2026-0", 85400);
        AndraExpediente andraExpediente = AndraExpediente.builder().andraCodigo(andraCodigoExp).andraPersona(andraSesion.andraEstudiante()).andraSede(andraSede).andraClase(AndraClaseExpediente.DOC)
                .andraTramite(andraTramite).andraPago(andraPago).andraGrupo(AndraGrupoExpediente.TRAMITE).andraIcono(andraTramite.andraEsCarne() ? "idcard" : "doc").andraRotuloIcono("PAGO")
                .andraTono("warning").andraEstado("Pago pendiente en Andra Pay").andraTitulo(andraTramite.getAndraNombre())
                .andraDescripcion("Documento a nombre de " + andraSol.getAndraNombre() + " (" + andraSol.getAndraTipo() + "). Uso: " + andraMotivo + ".")
                .andraPaso(1).andraSolicitante(andraSol).andraFecha(AndraFechas.andraAhora()).build();
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Fecha de registro", "Hoy, " + AndraFechas.andraDmy(AndraFechas.andraHoy())));
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Derecho / Tasa", AndraMontos.andraSoles(andraPago.andraObtenerTotal()) + " (Pendiente)"));
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Recojo", "Mesa de Partes · " + andraSede.getAndraNombreCompleto()));
        andraExpediente.andraPonerAcciones("primary", "pay-sol", "Pagar en Andra Pay", "secondary", "route-sol", "Seguimiento en vivo",
                "danger", "desist-sol", "Anular trámite");
        andraExpedientes.save(andraExpediente);
        andraNotificaciones.andraNotificarAdministrador("Nueva orden " + andraOrdenJemm + ": " + andraTramite.getAndraNombre() + " (" + andraSol.getAndraNombre() + ").", "a-tramites");
        andraConexion.andraEncolar("Solicitar " + andraTramite.getAndraNombre().toLowerCase());
        return andraPago;
    }

    @Transactional(readOnly = true)
    public String andraSiguienteCodigoExpediente(String andraPrefijo, int andraBase) {
        int andraCantidad = andraBase + (int) andraExpedientes.count();
        String andraTexto;
        do {
            andraTexto = andraPrefijo + andraCantidad++;
        } while (andraExpedientes.existsByAndraCodigo(andraTexto));
        return andraTexto;
    }

    public static String andraNuevaOperacion(AndraMetodoPago andraMetodoPago) {
        return andraMetodoPago.andraObtenerPrefijo() + (10000000 + ANDRA_AZAR_JEMM.nextInt(89999999));
    }

    @Transactional
    public AndraPago andraCompletar(String andraCodigo, AndraMetodoPago andraMetodo) {
        if (andraConexion.andraSinConexion()) {
            throw new AndraNegocioExcepcion("Necesitas conexión para pagar");
        }
        AndraPago andraPago = andraObtener(andraCodigo);
        if (andraPago.andraEsPagado()) {
            return andraPago;
        }
        if (!andraPago.andraEsPendiente()) {
            throw new AndraNegocioExcepcion("Esta orden ya no se puede pagar.");
        }
        AndraTramite andraTramite = andraPago.getAndraTramite();
        andraPago.setAndraEstado(AndraEstadoPago.PAGADO);
        andraPago.setAndraMetodo(andraMetodo);
        andraPago.setAndraPagado(AndraFechas.andraAhora());
        andraPago.setAndraOperacion(andraNuevaOperacion(andraMetodo));
        andraPago.setAndraBoleta("B001-00" + (4000 + andraPagos.countByAndraBoletaIsNotNull()));
        AndraRecojo andraRecojo = andraRecojo(andraTramite, andraPago.getAndraPagado());
        andraPago.setAndraRecojoFecha(andraRecojo.andraFecha());
        andraPago.setAndraRecojoTexto(andraRecojo.andraTexto());
        andraPagos.save(andraPago);
        String andraMonto = AndraMontos.andraSoles(andraPago.andraObtenerTotal());
        andraExpedientes.findFirstByAndraPago(andraPago).ifPresent(andraSol -> {
            boolean andraListo = andraTramite != null && "mismo".equals(andraTramite.getAndraEntrega());
            boolean andraInmediato = andraTramite != null && "inmediato".equals(andraTramite.getAndraEntrega());
            andraSol.setAndraTono("success");
            andraSol.setAndraRotuloIcono("PAGADO");
            andraSol.andraPonerDato(1, "Derecho / Tasa", andraMonto + " (Pagado con " + andraMetodo.andraObtenerEtiqueta() + ")");
            andraSol.andraPonerDato(2, "Recojo", andraRecojo.andraFecha() != null ? AndraFechas.andraDmy(andraRecojo.andraFecha()) + " · " + andraPago.getAndraSede().getAndraNombre() : "No requiere");
            andraSol.setAndraGrupo(andraListo || andraInmediato ? AndraGrupoExpediente.LISTO : AndraGrupoExpediente.TRAMITE);
            andraSol.setAndraEstado(andraInmediato ? "Pagado · Habilitado" : andraListo ? "Pagado · Listo para recoger" : "Pagado · En emisión");
            andraSol.setAndraPaso(andraListo || andraInmediato ? 4 : 2);
            andraSol.getAndraEtiquetas().clear();
            andraSol.getAndraEtiquetas().add("Comprobante " + andraPago.getAndraBoleta());
            andraSol.getAndraEtiquetas().add("Operación " + andraPago.getAndraOperacion());
            if (andraTramite != null && andraTramite.andraGeneraDocumento()) {
                andraSol.andraPonerAcciones("primary", "gen-sol", "Ver documento generado", "secondary", "rcpt-sol", "Ver comprobante",
                        "tertiary", "route-sol", "Seguimiento en vivo");
            } else {
                andraSol.andraPonerAcciones("primary", "rcpt-sol", "Ver comprobante", "secondary", "route-sol", "Seguimiento en vivo");
            }
            andraExpedientes.save(andraSol);
            andraSeguimiento.andraRegistrar(andraSol, "Andra: recibimos tu pago de " + andraMonto + " por " + andraPago.getAndraConcepto() + ". "
                    + (andraRecojo.andraFecha() != null ? "Recojo: " + andraRecojo.andraTexto() + "." : ""));
        });
        AndraMatricula andraMatricula = andraMatricula();
        if (andraPago.getAndraTipo() == AndraTipoPago.MATRICULA && andraMatricula.getAndraPago().getAndraId().equals(andraPago.getAndraId())) {
            andraMatricula.setAndraEstado(AndraEstadoMatricula.CONFIRMADA);
            andraMatriculas.save(andraMatricula);
            andraNotificaciones.andraNotificarEstudiante("Tu matrícula " + andraMatricula.getAndraPeriodo() + " quedó confirmada. Pago " + andraPago.getAndraBoleta() + ".", "pay");
        } else {
            andraNotificaciones.andraNotificarEstudiante("Pago confirmado: " + andraPago.getAndraConcepto() + " (" + andraMonto + ")."
                    + (andraRecojo.andraFecha() != null ? " Recojo: " + AndraFechas.andraDmy(andraRecojo.andraFecha()) + "." : ""), "pay");
        }
        andraNotificaciones.andraNotificarAdministrador("Pago confirmado por " + andraMetodo.andraObtenerEtiqueta() + ": " + andraPago.getAndraConcepto() + " · " + andraMonto + ".", "a-pagos");
        return andraPago;
    }

    @Transactional
    public void andraAnular(AndraPago andraPago) {
        if (andraPago != null && andraPago.andraEsPendiente()) {
            AndraPago andraActual = andraObtener(andraPago.getAndraCodigo());
            andraActual.setAndraEstado(AndraEstadoPago.ANULADO);
            andraPagos.save(andraActual);
        }
    }
}
