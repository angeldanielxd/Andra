package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.DixonExpedienteRepositorio;
import pe.andra.util.DixonFechas;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class DixonExpedienteServicio {

    public static final List<String[]> DIXON_PREDEFINIDOS = List.of(new String[]{"pay", "Con pago pendiente"},
            new String[]{"act", "Requieren acción"}, new String[]{"rl", "Reservas y préstamos"},
            new String[]{"doc", "Documentos"});
    public static final List<String[]> DIXON_PASOS = List.of(new String[]{"1", "Mesa de Partes"},
            new String[]{"2", "Validación de Escuela"}, new String[]{"3", "Firma Digital"}, new String[]{"4", "Listo"});

    public record DixonGrupoOpciones(String dixonGrupo, List<String> dixonOpciones) {
    }

    public static final List<DixonGrupoOpciones> DIXON_TIPOS_SOLICITUD = List.of(
            new DixonGrupoOpciones("Certificados y constancias", List.of("Certificado de estudios", "Certificado de notas",
                    "Constancia de estudios", "Constancia de matrícula", "Constancia de egresado",
                    "Constancia de orden de mérito / tercio superior", "Constancia de no adeudo")),
            new DixonGrupoOpciones("Académicos", List.of("Récord académico", "Sílabos certificados", "Malla curricular certificada",
                    "Convalidación de cursos", "Traslado interno", "Reserva de matrícula", "Retiro de curso",
                    "Justificación de inasistencia")),
            new DixonGrupoOpciones("Grados y títulos", List.of("Grado de bachiller", "Título profesional", "Duplicado de diploma")),
            new DixonGrupoOpciones("Otros", List.of("Devolución de tasa", "Carné universitario", "Otro trámite")));

    private final DixonExpedienteRepositorio dixonRepositorio;
    private final DixonSesionServicio dixonSesion;
    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonPagoServicio dixonPagos;
    private final DixonNotificacionServicio dixonNotificaciones;
    private final DixonConexionServicio dixonConexion;

    @Transactional(readOnly = true)
    public List<DixonExpediente> dixonMios() {
        return dixonRepositorio.findByDixonPersonaOrderByDixonIdDesc(dixonSesion.dixonEstudiante());
    }

    @Transactional(readOnly = true)
    public List<DixonExpediente> dixonTodos() {
        return dixonRepositorio.findAllByOrderByDixonIdDesc();
    }

    @Transactional(readOnly = true)
    public DixonExpediente dixonObtener(String dixonCodigo) {
        return dixonRepositorio.findByDixonCodigo(dixonCodigo).orElseThrow(() -> new DixonNegocioExcepcion("El expediente no existe."));
    }

    @Transactional(readOnly = true)
    public Optional<DixonExpediente> dixonDePago(DixonPago dixonPago) {
        return dixonRepositorio.findFirstByDixonPago(dixonPago);
    }

    public static long dixonContar(List<DixonExpediente> dixonLista, DixonGrupoExpediente dixonGrupo) {
        return dixonLista.stream().filter(dixonExpediente -> dixonGrupo == null || dixonExpediente.getDixonGrupo() == dixonGrupo).count();
    }

    public static Map<String, Predicate<DixonExpediente>> dixonPredicados() {
        return Map.of("pay", DixonExpediente::dixonEsConPagoPendiente,
                "act", dixonExpediente -> dixonExpediente.getDixonGrupo() == DixonGrupoExpediente.OBSERVADO || dixonExpediente.dixonEsConPagoPendiente(),
                "rl", dixonExpediente -> dixonExpediente.getDixonClase().dixonEsReservaOPrestamo(),
                "doc", dixonExpediente -> dixonExpediente.getDixonClase() == DixonClaseExpediente.DOC);
    }

    public static String dixonTextoBusqueda(DixonExpediente dixonExpediente) {
        StringBuilder dixonTexto = new StringBuilder(dixonExpediente.getDixonCodigo() + " " + dixonExpediente.getDixonTitulo() + " " + dixonExpediente.getDixonEstado());
        dixonExpediente.getDixonDatos().forEach(dixonDatoExpediente -> dixonTexto.append(' ').append(dixonDatoExpediente.getDixonValor()));
        return dixonTexto.toString();
    }

    public Optional<DixonTramite> dixonTramitePara(String dixonTipo) {
        return dixonCatalogo.dixonTramites().stream().filter(dixonTramite -> dixonTramite.getDixonNombre().equals(dixonTipo)
                || ("Certificado de notas".equals(dixonTipo) && "t-cert".equals(dixonTramite.getDixonCodigo()))
                || ("Grado de bachiller".equals(dixonTipo) && "t-bach".equals(dixonTramite.getDixonCodigo()))).findFirst();
    }

    private static String dixonHoyTexto() {
        LocalDate dixonFecha = DixonFechas.dixonHoy();
        return "Hoy, " + dixonFecha.getDayOfMonth() + " de " + DixonFechas.DIXON_MESES[dixonFecha.getMonthValue() - 1] + ", " + dixonFecha.getYear();
    }

    @Transactional
    public DixonExpediente dixonRegistrar(String dixonTipo, String dixonUnidad, String dixonSedeCodigo, String dixonDetalle, String dixonAdjunto) {
        if (dixonTipo == null || dixonTipo.isBlank()) {
            throw new DixonNegocioExcepcion("Elige el tipo de trámite.");
        }
        if (dixonUnidad == null || dixonUnidad.isBlank()) {
            throw new DixonNegocioExcepcion("Elige la unidad académica.");
        }
        if (dixonDetalle == null || dixonDetalle.trim().length() < 15) {
            throw new DixonNegocioExcepcion("Escribe al menos 15 caracteres.");
        }
        DixonSede dixonSede = dixonCatalogo.dixonSede(dixonSedeCodigo).orElseGet(() -> dixonSesion.dixonEstudiante().getDixonSede());
        String dixonCodigo = dixonPagos.dixonSiguienteCodigoExpediente("SOL-2026-0", 85200);
        DixonExpediente dixonExpediente = DixonExpediente.builder().dixonCodigo(dixonCodigo).dixonPersona(dixonSesion.dixonEstudiante()).dixonSede(dixonSede).dixonClase(DixonClaseExpediente.DOC)
                .dixonGrupo(DixonGrupoExpediente.TRAMITE).dixonIcono("doc").dixonRotuloIcono("EN CURSO").dixonTono("warning")
                .dixonEstado("Registrado en Mesa de Partes").dixonTitulo(dixonTipo).dixonDescripcion(dixonDetalle.trim()).dixonPaso(1)
                .dixonNota(dixonAdjunto != null && !dixonAdjunto.isBlank() ? "Adjunto: " + dixonAdjunto : null).dixonFecha(DixonFechas.dixonAhora()).build();
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Fecha de registro", dixonHoyTexto()));
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Sede de atención", dixonSede.getDixonNombreCompleto()));
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Unidad académica", dixonUnidad));
        dixonExpediente.dixonPonerAcciones("primary", "route-sol", "Seguimiento en vivo", "secondary", "state-sol", "Consultar Estado",
                "danger", "desist-sol", "Desistir / Anular Trámite");
        dixonRepositorio.save(dixonExpediente);
        dixonNotificaciones.dixonNotificarEstudiante("Expediente " + dixonCodigo + " registrado en Mesa de Partes.", "solicitudes");
        dixonConexion.dixonEncolar("Nueva solicitud");
        return dixonExpediente;
    }

    @Transactional
    public DixonExpediente dixonExtraordinaria(String dixonTipo, String dixonDescripcion, String dixonAdjunto) {
        if (dixonTipo == null || dixonTipo.isBlank()) {
            throw new DixonNegocioExcepcion("Elige el tipo de caso.");
        }
        if (dixonDescripcion == null || dixonDescripcion.trim().length() < 20) {
            throw new DixonNegocioExcepcion("Escribe al menos 20 caracteres.");
        }
        String dixonCodigo = dixonPagos.dixonSiguienteCodigoExpediente("SOL-2026-EX-0", 10);
        DixonExpediente dixonExpediente = DixonExpediente.builder().dixonCodigo(dixonCodigo).dixonPersona(dixonSesion.dixonEstudiante()).dixonSede(dixonSesion.dixonEstudiante().getDixonSede())
                .dixonClase(DixonClaseExpediente.DOC).dixonGrupo(DixonGrupoExpediente.TRAMITE).dixonIcono("doc").dixonRotuloIcono("DECANATO")
                .dixonTono("warning").dixonEstado("En evaluación por Decanato").dixonTitulo(dixonTipo).dixonDescripcion(dixonDescripcion.trim())
                .dixonPaso(1).dixonNota(dixonAdjunto != null && !dixonAdjunto.isBlank() ? "Adjunto: " + dixonAdjunto : null).dixonFecha(DixonFechas.dixonAhora()).build();
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Fecha de registro", dixonHoyTexto()));
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Tipo", "Expediente extraordinario"));
        dixonExpediente.getDixonDatos().add(new DixonDatoExpediente("Unidad", "Decanato"));
        dixonExpediente.dixonPonerAcciones("primary", "route-sol", "Seguimiento en vivo", "secondary", "state-sol", "Consultar Estado",
                "danger", "desist-sol", "Desistir / Anular Trámite");
        dixonConexion.dixonEncolar("Solicitud extraordinaria");
        return dixonRepositorio.save(dixonExpediente);
    }

    @Transactional
    public DixonExpediente dixonSubsanar(String dixonCodigo, String dixonArchivo) {
        if (dixonArchivo == null || dixonArchivo.isBlank()) {
            throw new DixonNegocioExcepcion("Adjunta el documento en PDF.");
        }
        DixonExpediente dixonExpediente = dixonObtener(dixonCodigo);
        dixonExpediente.setDixonGrupo(DixonGrupoExpediente.TRAMITE);
        dixonExpediente.setDixonTono("warning");
        dixonExpediente.setDixonDestacado(false);
        dixonExpediente.setDixonEstado("Subsanación enviada · En revisión");
        dixonExpediente.setDixonRotuloIcono("EN CURSO");
        dixonExpediente.setDixonIcono("doc");
        dixonExpediente.setDixonNota("Archivo enviado: " + dixonArchivo);
        dixonExpediente.dixonPonerAcciones("primary", "route-sol", "Seguimiento en vivo", "secondary", "state-sol", "Consultar Estado",
                "tertiary", "ask-sol", "Consultar a Secretaría");
        if (dixonExpediente.getDixonDatos().size() > 2) {
            dixonExpediente.dixonPonerDato(2, "Plazo subsanación", "Subsanado dentro del plazo");
        }
        dixonRepositorio.save(dixonExpediente);
        dixonNotificaciones.dixonQuitarPorTexto(dixonCodigo + " fue observado");
        dixonConexion.dixonEncolar("Subsanar documento");
        return dixonExpediente;
    }

    @Transactional
    public DixonExpediente dixonDesistir(String dixonCodigo) {
        DixonExpediente dixonExpediente = dixonObtener(dixonCodigo);
        dixonExpediente.setDixonGrupo(DixonGrupoExpediente.ANULADO);
        dixonExpediente.setDixonTono("neutral");
        dixonExpediente.setDixonEstado("Trámite anulado por el solicitante");
        dixonExpediente.setDixonRotuloIcono("ANULADO");
        dixonExpediente.setDixonDestacado(false);
        dixonExpediente.dixonPonerAcciones("secondary", "route-sol", "Seguimiento en vivo");
        if (dixonExpediente.getDixonPago() != null) {
            dixonPagos.dixonAnular(dixonExpediente.getDixonPago());
        }
        dixonConexion.dixonEncolar("¿Desistir de este trámite?");
        return dixonRepositorio.save(dixonExpediente);
    }

    @Transactional
    public DixonExpediente dixonCancelar(String dixonCodigo) {
        DixonExpediente dixonExpediente = dixonObtener(dixonCodigo);
        dixonExpediente.setDixonGrupo(DixonGrupoExpediente.ANULADO);
        dixonExpediente.setDixonTono("neutral");
        dixonExpediente.setDixonEstado(dixonExpediente.getDixonClase() == DixonClaseExpediente.LAB ? "Reserva cancelada" : "Préstamo cancelado");
        dixonExpediente.dixonPonerAcciones("secondary", "cargo-sol", "Ver Cargo");
        dixonConexion.dixonEncolar("Cancelar");
        return dixonRepositorio.save(dixonExpediente);
    }

    @Transactional
    public DixonExpediente dixonReprogramar(String dixonCodigo, LocalDate dixonFecha, String dixonHorario) {
        if (dixonFecha == null) {
            throw new DixonNegocioExcepcion("Elige una fecha.");
        }
        if (dixonFecha.isBefore(DixonFechas.dixonHoy())) {
            throw new DixonNegocioExcepcion("Elige hoy o una fecha posterior.");
        }
        if (dixonHorario == null || dixonHorario.isBlank()) {
            throw new DixonNegocioExcepcion("Elige un horario.");
        }
        DixonExpediente dixonExpediente = dixonObtener(dixonCodigo);
        String dixonEtiqueta = dixonExpediente.getDixonDatos().isEmpty() ? "Fecha" : dixonExpediente.getDixonDatos().get(0).getDixonEtiqueta();
        dixonExpediente.dixonPonerDato(0, dixonEtiqueta, dixonFecha.getDayOfMonth() + " de " + DixonFechas.DIXON_MESES[dixonFecha.getMonthValue() - 1] + ", "
                + dixonFecha.getYear() + " (" + dixonHorario + ")");
        dixonConexion.dixonEncolar("Modificar horario");
        return dixonRepositorio.save(dixonExpediente);
    }

    public static List<String> dixonHorariosReprogramacion(DixonExpediente dixonExpediente) {
        return dixonExpediente.getDixonClase() == DixonClaseExpediente.LAB ? List.of("08:00 - 12:00", "12:00 - 16:00", "16:00 - 20:00")
                : List.of("10:00 - 14:00", "14:00 - 18:00");
    }
}
