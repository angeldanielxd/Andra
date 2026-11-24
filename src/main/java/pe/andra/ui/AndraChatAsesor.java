package pe.andra.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.AndraConsultaAsesorServicio;
import pe.andra.util.AndraFechas;

@Component
@RequiredArgsConstructor
public class AndraChatAsesor {

    private final AndraNotificador andraNotificador;
    private final AndraConsultaAsesorServicio andraConsultas;

    public void andraAbrir(AndraExpediente andraExpediente) {
        AndraConsultaAsesor andraInicial;
        try {
            andraInicial = andraConsultas.andraAbrir(andraExpediente.getAndraCodigo());
        } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
            andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            return;
        }
        VBox andraRegistro = new VBox(8);
        andraRegistro.getStyleClass().add("chat-log");
        ScrollPane andraDesplazable = new ScrollPane(andraRegistro);
        andraDesplazable.setFitToWidth(true);
        andraDesplazable.setPrefViewportHeight(280);
        andraDesplazable.getStyleClass().add("chat-scroll");
        Label andraCupo = AndraComponentes.andraEtiqueta("", "pill");
        andraCupo.setMinWidth(Region.USE_PREF_SIZE);
        TextField andraEntrada = new TextField();
        AndraComponentes.andraIndicacion(andraEntrada, "Escribe tu consulta sobre este trámite…");
        AndraComponentes.andraAccesible(andraEntrada, "Consulta al asesor");
        HBox.setHgrow(andraEntrada, Priority.ALWAYS);
        Button andraEnviar = AndraComponentes.andraBoton("Enviar", "btn-primary", null, null);
        Button andraAmpliar = AndraComponentes.andraBloque(AndraComponentes.andraBoton("Solicitar ampliar consulta", "btn-secondary", "fth-plus-circle", null));
        Label andraAyudaAmpliar = AndraComponentes.andraParrafo("", "hint");

        Runnable[] andraPintar = new Runnable[1];
        AndraConsultaAsesor[] andraActual = {andraInicial};
        andraPintar[0] = () -> {
            AndraConsultaAsesor andraConsultaAsesor = andraActual[0];
            andraRegistro.getChildren().clear();
            for (int andraIndice = 0; andraIndice < andraConsultaAsesor.getAndraMensajes().size(); andraIndice++) {
                AndraMensajeAsesor andraMensajeAsesor = andraConsultaAsesor.getAndraMensajes().get(andraIndice);
                andraRegistro.getChildren().add(andraIndice == 0 ? andraContexto(andraMensajeAsesor) : andraBurbuja(andraMensajeAsesor));
            }
            int andraQuedan = andraConsultaAsesor.andraRestantes();
            AndraComponentes.andraFijar(andraCupo, andraQuedan + " de " + andraConsultaAsesor.getAndraCupo() + " consultas disponibles");
            andraCupo.getStyleClass().removeAll("success", "warning", "error");
            andraCupo.getStyleClass().add(andraQuedan == 0 ? "error" : andraQuedan == 1 ? "warning" : "success");
            boolean andraPendiente = andraConsultaAsesor.getAndraAmpliacion() == AndraEstadoAmpliacion.PENDIENTE;
            andraEntrada.setDisable(andraQuedan == 0);
            andraEnviar.setDisable(andraQuedan == 0);
            AndraComponentes.andraIndicacion(andraEntrada, andraQuedan == 0 ? (andraPendiente ? "Esperando la evaluación del asesor…" : "Agotaste tus consultas")
                    : "Escribe tu consulta sobre este trámite…");
            andraAmpliar.setDisable(andraQuedan > 0 || andraConsultaAsesor.getAndraAmpliacion() != AndraEstadoAmpliacion.NINGUNA);
            AndraComponentes.andraFijar(andraAyudaAmpliar, switch (andraConsultaAsesor.getAndraAmpliacion()) {
                case PENDIENTE -> "Solicitud enviada. El asesor está evaluando si es necesario ampliar tu consulta.";
                case APROBADA -> "El asesor aprobó la ampliación: se sumaron 3 consultas adicionales.";
                case RECHAZADA -> "El asesor no consideró necesaria la ampliación.";
                default -> andraQuedan > 0 ? "Disponible al agotar tus 3 consultas. El asesor decide si otorga 3 oportunidades más."
                        : "Pide 3 oportunidades más; el asesor evaluará si es necesario.";
            });
            Platform.runLater(() -> andraDesplazable.setVvalue(1));
        };

        Runnable andraMandar = () -> {
            String andraTexto = andraEntrada.getText() == null ? "" : andraEntrada.getText().trim();
            if (andraTexto.isEmpty()) {
                return;
            }
            try {
                andraActual[0] = andraConsultas.andraPreguntar(andraExpediente.getAndraCodigo(), andraTexto);
                andraEntrada.clear();
                andraPintar[0].run();
                if (!andraEntrada.isDisabled()) {
                    Platform.runLater(andraEntrada::requestFocus);
                }
            } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        };
        andraEnviar.setOnAction(andraEvento -> andraMandar.run());
        andraEntrada.setOnAction(andraEvento -> andraMandar.run());
        andraAmpliar.setOnAction(andraEvento -> {
            try {
                andraActual[0] = andraConsultas.andraSolicitarAmpliacion(andraExpediente.getAndraCodigo());
                andraPintar[0].run();
                PauseTransition andraPausa = new PauseTransition(Duration.millis(1500));
                andraPausa.setOnFinished(andraPosicionX -> {
                    andraActual[0] = andraConsultas.andraEvaluarAmpliacion(andraExpediente.getAndraCodigo());
                    andraPintar[0].run();
                    if (andraActual[0].getAndraAmpliacion() == AndraEstadoAmpliacion.APROBADA) {
                        andraNotificador.andraExito("El asesor amplió tu consulta: 3 oportunidades más.");
                    }
                });
                andraPausa.play();
            } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        });

        andraPintar[0].run();
        HBox andraCabecera = new HBox(8, AndraComponentes.andraPildora("Asesor en línea", "success"), AndraComponentes.andraEspacio(), andraCupo);
        andraCabecera.setAlignment(Pos.CENTER_LEFT);
        HBox andraFila = new HBox(andraEntrada, andraEnviar);
        andraFila.getStyleClass().add("copy-row");
        VBox andraCuerpo = new VBox(10, andraCabecera, andraDesplazable, andraFila, new Separator(), andraAmpliar, andraAyudaAmpliar,
                AndraComponentes.andraParrafo("Simulación: las respuestas del asesor son automáticas de demostración.", "hint"));
        andraNotificador.andraModal("Consulta en línea · " + andraExpediente.getAndraCodigo(), andraCuerpo, false);
        if (!andraEntrada.isDisabled()) {
            Platform.runLater(andraEntrada::requestFocus);
        }
    }

    private VBox andraContexto(AndraMensajeAsesor andraMensajeAsesor) {
        VBox andraCajaVertical = new VBox(4, AndraComponentes.andraMayus("Contexto enviado automáticamente", "label-caps"),
                AndraComponentes.andraLiteralParrafo(andraMensajeAsesor.getAndraTexto(), "small"));
        andraCajaVertical.getStyleClass().add("wa-msg");
        andraCajaVertical.getStyleClass().add("mail");
        return andraCajaVertical;
    }

    private HBox andraBurbuja(AndraMensajeAsesor andraMensajeAsesor) {
        Label andraEtiqueta = AndraComponentes.andraLiteralParrafo(andraMensajeAsesor.getAndraTexto(), "msg", andraMensajeAsesor.isAndraDeAsesor() ? "op" : "me");
        andraEtiqueta.setMaxWidth(380);
        VBox andraCajaVertical = new VBox(2, andraEtiqueta, AndraComponentes.andraEtiqueta((andraMensajeAsesor.isAndraDeAsesor() ? "Asesor · " : "Tú · ") + AndraFechas.andraHm(andraMensajeAsesor.getAndraEnviado()), "hint"));
        andraCajaVertical.setAlignment(andraMensajeAsesor.isAndraDeAsesor() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        HBox andraAlto = new HBox(andraCajaVertical);
        andraAlto.setAlignment(andraMensajeAsesor.isAndraDeAsesor() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return andraAlto;
    }
}
