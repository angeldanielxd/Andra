package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.AndraConsultaAsesorRepositorio;
import pe.andra.util.AndraFechas;

@Service
@RequiredArgsConstructor
public class AndraConsultaAsesorServicio {

    public static final int ANDRA_CUPO = 3;

    private final AndraConsultaAsesorRepositorio andraRepositorio;
    private final AndraExpedienteServicio andraExpedientes;

    public static boolean andraDisponible(AndraExpediente andraExpediente) {
        return andraExpediente.getAndraGrupo() == AndraGrupoExpediente.TRAMITE;
    }

    public static String andraContexto(AndraExpediente andraExpediente) {
        AndraSeguimientoServicio.AndraSeguimientoCjsv andraSeguimientoCjsv = AndraSeguimientoServicio.andraCalcular(andraExpediente);
        String andraEtapa = andraSeguimientoCjsv.andraActual() < andraSeguimientoCjsv.andraEtapas().size() ? andraSeguimientoCjsv.andraEtapas().get(andraSeguimientoCjsv.andraActual()).andraTitulo() : "Finalizado";
        StringBuilder andraTexto = new StringBuilder("Consulta sobre el expediente ").append(andraExpediente.getAndraCodigo()).append(" · ").append(andraExpediente.getAndraTitulo())
                .append(". Estado: ").append(andraExpediente.getAndraEstado()).append(". Etapa actual: ").append(andraEtapa).append(". ").append(andraSeguimientoCjsv.andraEstimado()).append('.');
        if (andraExpediente.andraEsConPagoPendiente()) {
            andraTexto.append(" Tiene un pago pendiente.");
        }
        return andraTexto.toString();
    }

    @Transactional
    public AndraConsultaAsesor andraAbrir(String andraCodigo) {
        AndraExpediente andraExpediente = andraExpedientes.andraObtener(andraCodigo);
        return andraRepositorio.findByAndraExpediente(andraExpediente).orElseGet(() -> {
            if (!andraDisponible(andraExpediente)) {
                throw new AndraNegocioExcepcion("La consulta en línea solo está disponible para trámites en curso.");
            }
            AndraConsultaAsesor andraConsultaAsesor = AndraConsultaAsesor.builder().andraExpediente(andraExpediente).andraUsadas(0).andraCupo(ANDRA_CUPO).build();
            andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(false, andraContexto(andraExpediente), AndraFechas.andraAhora()));
            andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(true, "Hola, " + andraExpediente.getAndraPersona().andraObtenerPrimerNombre() + ". Ya tengo a la vista tu expediente "
                    + andraExpediente.getAndraCodigo() + " (" + andraExpediente.getAndraTitulo() + "). ¿Qué deseas consultar? Dispones de " + ANDRA_CUPO + " consultas.", AndraFechas.andraAhora()));
            return andraRepositorio.save(andraConsultaAsesor);
        });
    }

    @Transactional
    public AndraConsultaAsesor andraPreguntar(String andraCodigo, String andraTexto) {
        AndraConsultaAsesor andraConsultaAsesor = andraAbrir(andraCodigo);
        String andraTexto2 = andraTexto == null ? "" : andraTexto.trim();
        if (andraTexto2.isEmpty()) {
            throw new AndraNegocioExcepcion("Escribe tu consulta.");
        }
        if (andraConsultaAsesor.andraRestantes() == 0) {
            throw new AndraNegocioExcepcion("Agotaste tus consultas. Solicita una ampliación al asesor.");
        }
        andraConsultaAsesor.setAndraUsadas(andraConsultaAsesor.getAndraUsadas() + 1);
        andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(false, andraTexto2, AndraFechas.andraAhora()));
        andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(true, andraRespuesta(andraConsultaAsesor.getAndraExpediente(), andraTexto2, andraConsultaAsesor.andraRestantes()), AndraFechas.andraAhora()));
        return andraRepositorio.save(andraConsultaAsesor);
    }

    @Transactional
    public AndraConsultaAsesor andraSolicitarAmpliacion(String andraCodigo) {
        AndraConsultaAsesor andraConsultaAsesor = andraAbrir(andraCodigo);
        if (andraConsultaAsesor.andraRestantes() > 0) {
            throw new AndraNegocioExcepcion("Aún te quedan consultas disponibles.");
        }
        if (andraConsultaAsesor.getAndraAmpliacion() != AndraEstadoAmpliacion.NINGUNA) {
            throw new AndraNegocioExcepcion("Ya solicitaste una ampliación para este trámite.");
        }
        andraConsultaAsesor.setAndraAmpliacion(AndraEstadoAmpliacion.PENDIENTE);
        andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(false, "Solicito ampliar la consulta (3 oportunidades adicionales).", AndraFechas.andraAhora()));
        return andraRepositorio.save(andraConsultaAsesor);
    }

    @Transactional
    public AndraConsultaAsesor andraEvaluarAmpliacion(String andraCodigo) {
        AndraConsultaAsesor andraConsultaAsesor = andraAbrir(andraCodigo);
        if (andraConsultaAsesor.getAndraAmpliacion() != AndraEstadoAmpliacion.PENDIENTE) {
            return andraConsultaAsesor;
        }
        if (andraDisponible(andraConsultaAsesor.getAndraExpediente())) {
            andraConsultaAsesor.setAndraAmpliacion(AndraEstadoAmpliacion.APROBADA);
            andraConsultaAsesor.setAndraCupo(andraConsultaAsesor.getAndraCupo() + ANDRA_CUPO);
            andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(true, "Revisé tu caso y considero necesario continuar la atención. Te habilité "
                    + ANDRA_CUPO + " consultas adicionales.", AndraFechas.andraAhora()));
        } else {
            andraConsultaAsesor.setAndraAmpliacion(AndraEstadoAmpliacion.RECHAZADA);
            andraConsultaAsesor.getAndraMensajes().add(new AndraMensajeAsesor(true, "Tu trámite ya no está en curso, por lo que no es necesario ampliar la consulta.", AndraFechas.andraAhora()));
        }
        return andraRepositorio.save(andraConsultaAsesor);
    }

    static String andraRespuesta(AndraExpediente andraExpediente, String andraMensaje, int andraRestantes) {
        AndraSeguimientoServicio.AndraSeguimientoCjsv andraSeguimientoCjsv = AndraSeguimientoServicio.andraCalcular(andraExpediente);
        String andraTexto = andraMensaje.toLowerCase();
        String andraResultado;
        if (andraTexto.matches("(?s).*(pago|tasa|pagar|monto).*")) {
            andraResultado = andraExpediente.andraEsConPagoPendiente() ? "Tu expediente tiene un pago pendiente; puedes completarlo desde Andra Pay y el trámite continuará automáticamente."
                    : "El pago de tu expediente ya figura registrado, no necesitas abonar nada adicional.";
        } else if (andraTexto.matches("(?s).*(cu[aá]ndo|demora|tiempo|plazo|tarda|listo|recoger|entrega).*")) {
            andraResultado = andraSeguimientoCjsv.andraEstimado() + ". Te avisaremos por tus canales de notificación cuando cambie de etapa.";
        } else if (andraTexto.matches("(?s).*(estado|etapa|avance|va mi|c[oó]mo va).*")) {
            String andraEtapa = andraSeguimientoCjsv.andraActual() < andraSeguimientoCjsv.andraEtapas().size() ? andraSeguimientoCjsv.andraEtapas().get(andraSeguimientoCjsv.andraActual()).andraTitulo() : "Finalizado";
            andraResultado = "Tu expediente está en la etapa \"" + andraEtapa + "\" (" + andraExpediente.getAndraEstado() + ").";
        } else if (andraTexto.matches("(?s).*(document|requisit|adjunt|pdf).*")) {
            andraResultado = "Por ahora tu expediente no registra documentos faltantes. Si Jefatura lo observa, verás el motivo en Mis Solicitudes.";
        } else if (andraTexto.matches("(?s).*(desist|anular|cancel|reemb).*")) {
            andraResultado = "Puedes desistir mientras siga en trámite; la tasa se reintegra en 5 días hábiles.";
        } else if (andraTexto.matches("(?s).*(ventanilla|d[oó]nde|sede|oficina).*")) {
            andraResultado = "Cuando esté listo lo recogerás en la ventanilla " + andraSeguimientoCjsv.andraVentanilla() + " de Mesa de Partes, sede " + andraExpediente.getAndraSede().getAndraNombre() + ".";
        } else {
            andraResultado = "Gracias por tu consulta sobre " + andraExpediente.getAndraCodigo() + ". La derivé al área encargada y te responderemos por este medio.";
        }
        return andraRestantes == 0 ? andraResultado + " Has usado todas tus consultas; si lo necesitas, puedes solicitar una ampliación." : andraResultado;
    }
}
