package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.AndraExpedienteRepositorio;
import pe.andra.util.AndraFechas;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class AndraExpedienteServicio {

    public static final List<String[]> ANDRA_PREDEFINIDOS = List.of(new String[]{"pay", "Con pago pendiente"},
            new String[]{"act", "Requieren acción"}, new String[]{"rl", "Reservas y préstamos"},
            new String[]{"doc", "Documentos"});
    public static final List<String[]> ANDRA_PASOS = List.of(new String[]{"1", "Mesa de Partes"},
            new String[]{"2", "Validación de Escuela"}, new String[]{"3", "Firma Digital"}, new String[]{"4", "Listo"});

    public record AndraGrupoOpciones(String andraGrupo, List<String> andraOpciones) {
    }

    public static final List<AndraGrupoOpciones> ANDRA_TIPOS_SOLICITUD = List.of(
            new AndraGrupoOpciones("Certificados y constancias", List.of("Certificado de estudios", "Certificado de notas",
                    "Constancia de estudios", "Constancia de matrícula", "Constancia de egresado",
                    "Constancia de orden de mérito / tercio superior", "Constancia de no adeudo")),
            new AndraGrupoOpciones("Académicos", List.of("Récord académico", "Sílabos certificados", "Malla curricular certificada",
                    "Convalidación de cursos", "Traslado interno", "Reserva de matrícula", "Retiro de curso",
                    "Justificación de inasistencia")),
            new AndraGrupoOpciones("Grados y títulos", List.of("Grado de bachiller", "Título profesional", "Duplicado de diploma")),
            new AndraGrupoOpciones("Otros", List.of("Devolución de tasa", "Carné universitario", "Otro trámite")));

    private final AndraExpedienteRepositorio andraRepositorio;
    private final AndraSesionServicio andraSesion;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraPagoServicio andraPagos;
    private final AndraNotificacionServicio andraNotificaciones;
    private final AndraConexionServicio andraConexion;

    @Transactional(readOnly = true)
    public List<AndraExpediente> andraMios() {
        return andraRepositorio.findByAndraPersonaOrderByAndraIdDesc(andraSesion.andraEstudiante());
    }

    @Transactional(readOnly = true)
    public List<AndraExpediente> andraTodos() {
        return andraRepositorio.findAllByOrderByAndraIdDesc();
    }

    @Transactional(readOnly = true)
    public AndraExpediente andraObtener(String andraCodigo) {
        return andraRepositorio.findByAndraCodigo(andraCodigo).orElseThrow(() -> new AndraNegocioExcepcion("El expediente no existe."));
    }

    @Transactional(readOnly = true)
    public Optional<AndraExpediente> andraDePago(AndraPago andraPago) {
        return andraRepositorio.findFirstByAndraPago(andraPago);
    }

    public static long andraContar(List<AndraExpediente> andraLista, AndraGrupoExpediente andraGrupo) {
        return andraLista.stream().filter(andraExpediente -> andraGrupo == null || andraExpediente.getAndraGrupo() == andraGrupo).count();
    }

    public static Map<String, Predicate<AndraExpediente>> andraPredicados() {
        return Map.of("pay", AndraExpediente::andraEsConPagoPendiente,
                "act", andraExpediente -> andraExpediente.getAndraGrupo() == AndraGrupoExpediente.OBSERVADO || andraExpediente.andraEsConPagoPendiente(),
                "rl", andraExpediente -> andraExpediente.getAndraClase().andraEsReservaOPrestamo(),
                "doc", andraExpediente -> andraExpediente.getAndraClase() == AndraClaseExpediente.DOC);
    }

    public static String andraTextoBusqueda(AndraExpediente andraExpediente) {
        StringBuilder andraTexto = new StringBuilder(andraExpediente.getAndraCodigo() + " " + andraExpediente.getAndraTitulo() + " " + andraExpediente.getAndraEstado());
        andraExpediente.getAndraDatos().forEach(andraDatoExpediente -> andraTexto.append(' ').append(andraDatoExpediente.getAndraValor()));
        return andraTexto.toString();
    }

    public Optional<AndraTramite> andraTramitePara(String andraTipo) {
        return andraCatalogo.andraTramites().stream().filter(andraTramite -> andraTramite.getAndraNombre().equals(andraTipo)
                || ("Certificado de notas".equals(andraTipo) && "t-cert".equals(andraTramite.getAndraCodigo()))
                || ("Grado de bachiller".equals(andraTipo) && "t-bach".equals(andraTramite.getAndraCodigo()))).findFirst();
    }

    private static String andraHoyTexto() {
        LocalDate andraFecha = AndraFechas.andraHoy();
        return "Hoy, " + andraFecha.getDayOfMonth() + " de " + AndraFechas.ANDRA_MESES[andraFecha.getMonthValue() - 1] + ", " + andraFecha.getYear();
    }

    @Transactional
    public AndraExpediente andraRegistrar(String andraTipo, String andraUnidad, String andraSedeCodigo, String andraDetalle, String andraAdjunto) {
        if (andraTipo == null || andraTipo.isBlank()) {
            throw new AndraNegocioExcepcion("Elige el tipo de trámite.");
        }
        if (andraUnidad == null || andraUnidad.isBlank()) {
            throw new AndraNegocioExcepcion("Elige la unidad académica.");
        }
        if (andraDetalle == null || andraDetalle.trim().length() < 15) {
            throw new AndraNegocioExcepcion("Escribe al menos 15 caracteres.");
        }
        AndraSede andraSede = andraCatalogo.andraSede(andraSedeCodigo).orElseGet(() -> andraSesion.andraEstudiante().getAndraSede());
        String andraCodigo = andraPagos.andraSiguienteCodigoExpediente("SOL-2026-0", 85200);
        AndraExpediente andraExpediente = AndraExpediente.builder().andraCodigo(andraCodigo).andraPersona(andraSesion.andraEstudiante()).andraSede(andraSede).andraClase(AndraClaseExpediente.DOC)
                .andraGrupo(AndraGrupoExpediente.TRAMITE).andraIcono("doc").andraRotuloIcono("EN CURSO").andraTono("warning")
                .andraEstado("Registrado en Mesa de Partes").andraTitulo(andraTipo).andraDescripcion(andraDetalle.trim()).andraPaso(1)
                .andraNota(andraAdjunto != null && !andraAdjunto.isBlank() ? "Adjunto: " + andraAdjunto : null).andraFecha(AndraFechas.andraAhora()).build();
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Fecha de registro", andraHoyTexto()));
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Sede de atención", andraSede.getAndraNombreCompleto()));
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Unidad académica", andraUnidad));
        andraExpediente.andraPonerAcciones("primary", "route-sol", "Seguimiento en vivo", "secondary", "state-sol", "Consultar Estado",
                "danger", "desist-sol", "Desistir / Anular Trámite");
        andraRepositorio.save(andraExpediente);
        andraNotificaciones.andraNotificarEstudiante("Expediente " + andraCodigo + " registrado en Mesa de Partes.", "solicitudes");
        andraConexion.andraEncolar("Nueva solicitud");
        return andraExpediente;
    }

    @Transactional
    public AndraExpediente andraExtraordinaria(String andraTipo, String andraDescripcion, String andraAdjunto) {
        if (andraTipo == null || andraTipo.isBlank()) {
            throw new AndraNegocioExcepcion("Elige el tipo de caso.");
        }
        if (andraDescripcion == null || andraDescripcion.trim().length() < 20) {
            throw new AndraNegocioExcepcion("Escribe al menos 20 caracteres.");
        }
        String andraCodigo = andraPagos.andraSiguienteCodigoExpediente("SOL-2026-EX-0", 10);
        AndraExpediente andraExpediente = AndraExpediente.builder().andraCodigo(andraCodigo).andraPersona(andraSesion.andraEstudiante()).andraSede(andraSesion.andraEstudiante().getAndraSede())
                .andraClase(AndraClaseExpediente.DOC).andraGrupo(AndraGrupoExpediente.TRAMITE).andraIcono("doc").andraRotuloIcono("DECANATO")
                .andraTono("warning").andraEstado("En evaluación por Decanato").andraTitulo(andraTipo).andraDescripcion(andraDescripcion.trim())
                .andraPaso(1).andraNota(andraAdjunto != null && !andraAdjunto.isBlank() ? "Adjunto: " + andraAdjunto : null).andraFecha(AndraFechas.andraAhora()).build();
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Fecha de registro", andraHoyTexto()));
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Tipo", "Expediente extraordinario"));
        andraExpediente.getAndraDatos().add(new AndraDatoExpediente("Unidad", "Decanato"));
        andraExpediente.andraPonerAcciones("primary", "route-sol", "Seguimiento en vivo", "secondary", "state-sol", "Consultar Estado",
                "danger", "desist-sol", "Desistir / Anular Trámite");
        andraConexion.andraEncolar("Solicitud extraordinaria");
        return andraRepositorio.save(andraExpediente);
    }

    @Transactional
    public AndraExpediente andraSubsanar(String andraCodigo, String andraArchivo) {
        if (andraArchivo == null || andraArchivo.isBlank()) {
            throw new AndraNegocioExcepcion("Adjunta el documento en PDF.");
        }
        AndraExpediente andraExpediente = andraObtener(andraCodigo);
        andraExpediente.setAndraGrupo(AndraGrupoExpediente.TRAMITE);
        andraExpediente.setAndraTono("warning");
        andraExpediente.setAndraDestacado(false);
        andraExpediente.setAndraEstado("Subsanación enviada · En revisión");
        andraExpediente.setAndraRotuloIcono("EN CURSO");
        andraExpediente.setAndraIcono("doc");
        andraExpediente.setAndraNota("Archivo enviado: " + andraArchivo);
        andraExpediente.andraPonerAcciones("primary", "route-sol", "Seguimiento en vivo", "secondary", "state-sol", "Consultar Estado",
                "tertiary", "ask-sol", "Consultar a Secretaría");
        if (andraExpediente.getAndraDatos().size() > 2) {
            andraExpediente.andraPonerDato(2, "Plazo subsanación", "Subsanado dentro del plazo");
        }
        andraRepositorio.save(andraExpediente);
        andraNotificaciones.andraQuitarPorTexto(andraCodigo + " fue observado");
        andraConexion.andraEncolar("Subsanar documento");
        return andraExpediente;
    }

    @Transactional
    public AndraExpediente andraDesistir(String andraCodigo) {
        AndraExpediente andraExpediente = andraObtener(andraCodigo);
        andraExpediente.setAndraGrupo(AndraGrupoExpediente.ANULADO);
        andraExpediente.setAndraTono("neutral");
        andraExpediente.setAndraEstado("Trámite anulado por el solicitante");
        andraExpediente.setAndraRotuloIcono("ANULADO");
        andraExpediente.setAndraDestacado(false);
        andraExpediente.andraPonerAcciones("secondary", "route-sol", "Seguimiento en vivo");
        if (andraExpediente.getAndraPago() != null) {
            andraPagos.andraAnular(andraExpediente.getAndraPago());
        }
        andraConexion.andraEncolar("¿Desistir de este trámite?");
        return andraRepositorio.save(andraExpediente);
    }

    @Transactional
    public AndraExpediente andraCancelar(String andraCodigo) {
        AndraExpediente andraExpediente = andraObtener(andraCodigo);
        andraExpediente.setAndraGrupo(AndraGrupoExpediente.ANULADO);
        andraExpediente.setAndraTono("neutral");
        andraExpediente.setAndraEstado(andraExpediente.getAndraClase() == AndraClaseExpediente.LAB ? "Reserva cancelada" : "Préstamo cancelado");
        andraExpediente.andraPonerAcciones("secondary", "cargo-sol", "Ver Cargo");
        andraConexion.andraEncolar("Cancelar");
        return andraRepositorio.save(andraExpediente);
    }

    @Transactional
    public AndraExpediente andraReprogramar(String andraCodigo, LocalDate andraFecha, String andraHorario) {
        if (andraFecha == null) {
            throw new AndraNegocioExcepcion("Elige una fecha.");
        }
        if (andraFecha.isBefore(AndraFechas.andraHoy())) {
            throw new AndraNegocioExcepcion("Elige hoy o una fecha posterior.");
        }
        if (andraHorario == null || andraHorario.isBlank()) {
            throw new AndraNegocioExcepcion("Elige un horario.");
        }
        AndraExpediente andraExpediente = andraObtener(andraCodigo);
        String andraEtiqueta = andraExpediente.getAndraDatos().isEmpty() ? "Fecha" : andraExpediente.getAndraDatos().get(0).getAndraEtiqueta();
        andraExpediente.andraPonerDato(0, andraEtiqueta, andraFecha.getDayOfMonth() + " de " + AndraFechas.ANDRA_MESES[andraFecha.getMonthValue() - 1] + ", "
                + andraFecha.getYear() + " (" + andraHorario + ")");
        andraConexion.andraEncolar("Modificar horario");
        return andraRepositorio.save(andraExpediente);
    }

    public static List<String> andraHorariosReprogramacion(AndraExpediente andraExpediente) {
        return andraExpediente.getAndraClase() == AndraClaseExpediente.LAB ? List.of("08:00 - 12:00", "12:00 - 16:00", "16:00 - 20:00")
                : List.of("10:00 - 14:00", "14:00 - 18:00");
    }
}
