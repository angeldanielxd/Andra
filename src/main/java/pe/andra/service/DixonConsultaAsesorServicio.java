package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.DixonConsultaAsesorRepositorio;
import pe.andra.util.DixonFechas;

@Service
@RequiredArgsConstructor
public class DixonConsultaAsesorServicio {

    public static final int DIXON_CUPO = 3;

    private final DixonConsultaAsesorRepositorio dixonRepositorio;
    private final DixonExpedienteServicio dixonExpedientes;

    public static boolean dixonDisponible(DixonExpediente dixonExpediente) {
        return dixonExpediente.getDixonGrupo() == DixonGrupoExpediente.TRAMITE;
    }

    public static String dixonContexto(DixonExpediente dixonExpediente) {
        DixonSeguimientoServicio.DixonSeguimientoCjsv dixonSeguimientoCjsv = DixonSeguimientoServicio.dixonCalcular(dixonExpediente);
        String dixonEtapa = dixonSeguimientoCjsv.dixonActual() < dixonSeguimientoCjsv.dixonEtapas().size() ? dixonSeguimientoCjsv.dixonEtapas().get(dixonSeguimientoCjsv.dixonActual()).dixonTitulo() : "Finalizado";
        StringBuilder dixonTexto = new StringBuilder("Consulta sobre el expediente ").append(dixonExpediente.getDixonCodigo()).append(" · ").append(dixonExpediente.getDixonTitulo())
                .append(". Estado: ").append(dixonExpediente.getDixonEstado()).append(". Etapa actual: ").append(dixonEtapa).append(". ").append(dixonSeguimientoCjsv.dixonEstimado()).append('.');
        if (dixonExpediente.dixonEsConPagoPendiente()) {
            dixonTexto.append(" Tiene un pago pendiente.");
        }
        return dixonTexto.toString();
    }

    @Transactional
    public DixonConsultaAsesor dixonAbrir(String dixonCodigo) {
        DixonExpediente dixonExpediente = dixonExpedientes.dixonObtener(dixonCodigo);
        return dixonRepositorio.findByDixonExpediente(dixonExpediente).orElseGet(() -> {
            if (!dixonDisponible(dixonExpediente)) {
                throw new DixonNegocioExcepcion("La consulta en línea solo está disponible para trámites en curso.");
            }
            DixonConsultaAsesor dixonConsultaAsesor = DixonConsultaAsesor.builder().dixonExpediente(dixonExpediente).dixonUsadas(0).dixonCupo(DIXON_CUPO).build();
            dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(false, dixonContexto(dixonExpediente), DixonFechas.dixonAhora()));
            dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(true, "Hola, " + dixonExpediente.getDixonPersona().dixonObtenerPrimerNombre() + ". Ya tengo a la vista tu expediente "
                    + dixonExpediente.getDixonCodigo() + " (" + dixonExpediente.getDixonTitulo() + "). ¿Qué deseas consultar? Dispones de " + DIXON_CUPO + " consultas.", DixonFechas.dixonAhora()));
            return dixonRepositorio.save(dixonConsultaAsesor);
        });
    }

    @Transactional
    public DixonConsultaAsesor dixonPreguntar(String dixonCodigo, String dixonTexto) {
        DixonConsultaAsesor dixonConsultaAsesor = dixonAbrir(dixonCodigo);
        String dixonTexto2 = dixonTexto == null ? "" : dixonTexto.trim();
        if (dixonTexto2.isEmpty()) {
            throw new DixonNegocioExcepcion("Escribe tu consulta.");
        }
        if (dixonConsultaAsesor.dixonRestantes() == 0) {
            throw new DixonNegocioExcepcion("Agotaste tus consultas. Solicita una ampliación al asesor.");
        }
        dixonConsultaAsesor.setDixonUsadas(dixonConsultaAsesor.getDixonUsadas() + 1);
        dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(false, dixonTexto2, DixonFechas.dixonAhora()));
        dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(true, dixonRespuesta(dixonConsultaAsesor.getDixonExpediente(), dixonTexto2, dixonConsultaAsesor.dixonRestantes()), DixonFechas.dixonAhora()));
        return dixonRepositorio.save(dixonConsultaAsesor);
    }

    @Transactional
    public DixonConsultaAsesor dixonSolicitarAmpliacion(String dixonCodigo) {
        DixonConsultaAsesor dixonConsultaAsesor = dixonAbrir(dixonCodigo);
        if (dixonConsultaAsesor.dixonRestantes() > 0) {
            throw new DixonNegocioExcepcion("Aún te quedan consultas disponibles.");
        }
        if (dixonConsultaAsesor.getDixonAmpliacion() != DixonEstadoAmpliacion.NINGUNA) {
            throw new DixonNegocioExcepcion("Ya solicitaste una ampliación para este trámite.");
        }
        dixonConsultaAsesor.setDixonAmpliacion(DixonEstadoAmpliacion.PENDIENTE);
        dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(false, "Solicito ampliar la consulta (3 oportunidades adicionales).", DixonFechas.dixonAhora()));
        return dixonRepositorio.save(dixonConsultaAsesor);
    }

    @Transactional
    public DixonConsultaAsesor dixonEvaluarAmpliacion(String dixonCodigo) {
        DixonConsultaAsesor dixonConsultaAsesor = dixonAbrir(dixonCodigo);
        if (dixonConsultaAsesor.getDixonAmpliacion() != DixonEstadoAmpliacion.PENDIENTE) {
            return dixonConsultaAsesor;
        }
        if (dixonDisponible(dixonConsultaAsesor.getDixonExpediente())) {
            dixonConsultaAsesor.setDixonAmpliacion(DixonEstadoAmpliacion.APROBADA);
            dixonConsultaAsesor.setDixonCupo(dixonConsultaAsesor.getDixonCupo() + DIXON_CUPO);
            dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(true, "Revisé tu caso y considero necesario continuar la atención. Te habilité "
                    + DIXON_CUPO + " consultas adicionales.", DixonFechas.dixonAhora()));
        } else {
            dixonConsultaAsesor.setDixonAmpliacion(DixonEstadoAmpliacion.RECHAZADA);
            dixonConsultaAsesor.getDixonMensajes().add(new DixonMensajeAsesor(true, "Tu trámite ya no está en curso, por lo que no es necesario ampliar la consulta.", DixonFechas.dixonAhora()));
        }
        return dixonRepositorio.save(dixonConsultaAsesor);
    }

    static String dixonRespuesta(DixonExpediente dixonExpediente, String dixonMensaje, int dixonRestantes) {
        DixonSeguimientoServicio.DixonSeguimientoCjsv dixonSeguimientoCjsv = DixonSeguimientoServicio.dixonCalcular(dixonExpediente);
        String dixonTexto = dixonMensaje.toLowerCase();
        String dixonResultado;
        if (dixonTexto.matches("(?s).*(pago|tasa|pagar|monto).*")) {
            dixonResultado = dixonExpediente.dixonEsConPagoPendiente() ? "Tu expediente tiene un pago pendiente; puedes completarlo desde Andra Pay y el trámite continuará automáticamente."
                    : "El pago de tu expediente ya figura registrado, no necesitas abonar nada adicional.";
        } else if (dixonTexto.matches("(?s).*(cu[aá]ndo|demora|tiempo|plazo|tarda|listo|recoger|entrega).*")) {
            dixonResultado = dixonSeguimientoCjsv.dixonEstimado() + ". Te avisaremos por tus canales de notificación cuando cambie de etapa.";
        } else if (dixonTexto.matches("(?s).*(estado|etapa|avance|va mi|c[oó]mo va).*")) {
            String dixonEtapa = dixonSeguimientoCjsv.dixonActual() < dixonSeguimientoCjsv.dixonEtapas().size() ? dixonSeguimientoCjsv.dixonEtapas().get(dixonSeguimientoCjsv.dixonActual()).dixonTitulo() : "Finalizado";
            dixonResultado = "Tu expediente está en la etapa \"" + dixonEtapa + "\" (" + dixonExpediente.getDixonEstado() + ").";
        } else if (dixonTexto.matches("(?s).*(document|requisit|adjunt|pdf).*")) {
            dixonResultado = "Por ahora tu expediente no registra documentos faltantes. Si Jefatura lo observa, verás el motivo en Mis Solicitudes.";
        } else if (dixonTexto.matches("(?s).*(desist|anular|cancel|reemb).*")) {
            dixonResultado = "Puedes desistir mientras siga en trámite; la tasa se reintegra en 5 días hábiles.";
        } else if (dixonTexto.matches("(?s).*(ventanilla|d[oó]nde|sede|oficina).*")) {
            dixonResultado = "Cuando esté listo lo recogerás en la ventanilla " + dixonSeguimientoCjsv.dixonVentanilla() + " de Mesa de Partes, sede " + dixonExpediente.getDixonSede().getDixonNombre() + ".";
        } else {
            dixonResultado = "Gracias por tu consulta sobre " + dixonExpediente.getDixonCodigo() + ". La derivé al área encargada y te responderemos por este medio.";
        }
        return dixonRestantes == 0 ? dixonResultado + " Has usado todas tus consultas; si lo necesitas, puedes solicitar una ampliación." : dixonResultado;
    }
}
