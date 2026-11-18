package pe.andra.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.DixonConsultaAsesorServicio;
import pe.andra.util.DixonFechas;

@Component
@RequiredArgsConstructor
public class AndraChatAsesor {

    private final AndraNotificador andraNotificador;
    private final DixonConsultaAsesorServicio andraConsultas;

    public void andraAbrir(DixonExpediente andraExpediente) {
        DixonConsultaAsesor andraInicial;
        try {
            andraInicial = andraConsultas.dixonAbrir(andraExpediente.getDixonCodigo());
        } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
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
        DixonConsultaAsesor[] andraActual = {andraInicial};
        andraPintar[0] = () -> {
            DixonConsultaAsesor andraConsultaAsesor = andraActual[0];
            andraRegistro.getChildren().clear();
            for (int andraIndice = 0; andraIndice < andraConsultaAsesor.getDixonMensajes().size(); andraIndice++) {
                DixonMensajeAsesor andraMensajeAsesor = andraConsultaAsesor.getDixonMensajes().get(andraIndice);
                andraRegistro.getChildren().add(andraIndice == 0 ? andraContexto(andraMensajeAsesor) : andraBurbuja(andraMensajeAsesor));
            }
            int andraQuedan = andraConsultaAsesor.dixonRestantes();
            AndraComponentes.andraFijar(andraCupo, andraQuedan + " de " + andraConsultaAsesor.getDixonCupo() + " consultas disponibles");
            andraCupo.getStyleClass().removeAll("success", "warning", "error");
            andraCupo.getStyleClass().add(andraQuedan == 0 ? "error" : andraQuedan == 1 ? "warning" : "success");
            boolean andraPendiente = andraConsultaAsesor.getDixonAmpliacion() == DixonEstadoAmpliacion.PENDIENTE;
            andraEntrada.setDisable(andraQuedan == 0);
            andraEnviar.setDisable(andraQuedan == 0);
            AndraComponentes.andraIndicacion(andraEntrada, andraQuedan == 0 ? (andraPendiente ? "Esperando la evaluación del asesor…" : "Agotaste tus consultas")
                    : "Escribe tu consulta sobre este trámite…");
            andraAmpliar.setDisable(andraQuedan > 0 || andraConsultaAsesor.getDixonAmpliacion() != DixonEstadoAmpliacion.NINGUNA);
            AndraComponentes.andraFijar(andraAyudaAmpliar, switch (andraConsultaAsesor.getDixonAmpliacion()) {
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
                andraActual[0] = andraConsultas.dixonPreguntar(andraExpediente.getDixonCodigo(), andraTexto);
                andraEntrada.clear();
                andraPintar[0].run();
                if (!andraEntrada.isDisabled()) {
                    Platform.runLater(andraEntrada::requestFocus);
                }
            } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        };
        andraEnviar.setOnAction(andraEvento -> andraMandar.run());
        andraEntrada.setOnAction(andraEvento -> andraMandar.run());
        andraAmpliar.setOnAction(andraEvento -> {
            try {
                andraActual[0] = andraConsultas.dixonSolicitarAmpliacion(andraExpediente.getDixonCodigo());
                andraPintar[0].run();
                PauseTransition andraPausa = new PauseTransition(Duration.millis(1500));
                andraPausa.setOnFinished(andraPosicionX -> {
                    andraActual[0] = andraConsultas.dixonEvaluarAmpliacion(andraExpediente.getDixonCodigo());
                    andraPintar[0].run();
                    if (andraActual[0].getDixonAmpliacion() == DixonEstadoAmpliacion.APROBADA) {
                        andraNotificador.andraExito("El asesor amplió tu consulta: 3 oportunidades más.");
                    }
                });
                andraPausa.play();
            } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
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
        andraNotificador.andraModal("Consulta en línea · " + andraExpediente.getDixonCodigo(), andraCuerpo, false);
        if (!andraEntrada.isDisabled()) {
            Platform.runLater(andraEntrada::requestFocus);
        }
    }

    private VBox andraContexto(DixonMensajeAsesor andraMensajeAsesor) {
        VBox andraCajaVertical = new VBox(4, AndraComponentes.andraMayus("Contexto enviado automáticamente", "label-caps"),
                AndraComponentes.andraLiteralParrafo(andraMensajeAsesor.getDixonTexto(), "small"));
        andraCajaVertical.getStyleClass().add("wa-msg");
        andraCajaVertical.getStyleClass().add("mail");
        return andraCajaVertical;
    }

    private HBox andraBurbuja(DixonMensajeAsesor andraMensajeAsesor) {
        Label andraEtiqueta = AndraComponentes.andraLiteralParrafo(andraMensajeAsesor.getDixonTexto(), "msg", andraMensajeAsesor.isDixonDeAsesor() ? "op" : "me");
        andraEtiqueta.setMaxWidth(380);
        VBox andraCajaVertical = new VBox(2, andraEtiqueta, AndraComponentes.andraEtiqueta((andraMensajeAsesor.isDixonDeAsesor() ? "Asesor · " : "Tú · ") + DixonFechas.dixonHm(andraMensajeAsesor.getDixonEnviado()), "hint"));
        andraCajaVertical.setAlignment(andraMensajeAsesor.isDixonDeAsesor() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        HBox andraAlto = new HBox(andraCajaVertical);
        andraAlto.setAlignment(andraMensajeAsesor.isDixonDeAsesor() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return andraAlto;
    }
}
