package pe.andra.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.config.AndraDatosInstitucion;
import pe.andra.model.AndraExpediente;
import pe.andra.model.AndraSede;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraPortapapeles;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AndraAyudaControlador extends AndraVistaBase {

    private final AndraAyudaServicio andraAyuda;
    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraSesionServicio andraSesion;
    private final AndraExpedienteServicio andraExpedientes;
    private final AndraDatosInstitucion andraInstitucion;

    private TextField andraBusqueda;
    private final Set<Integer> andraAbiertas = new HashSet<>();
    private Boolean andraUtilidad;
    private VBox andraCanales;

    @Override
    public void andraEnfocarBusqueda() {
        if (andraBusqueda != null) {
            andraBusqueda.requestFocus();
        }
    }

    public void andraIrACanales() {
        if (andraCanales != null) {
            AndraComponentes.andraDesplazarHasta(andraDesplazable, andraCanales);
        }
    }

    private TextField andraBusqueda() {
        if (andraBusqueda == null) {
            andraBusqueda = new TextField();
            AndraComponentes.andraIndicacion(andraBusqueda, "Busca por trámite, tema o palabra clave (ej. 'subsanación', 'carné', 'firma SUNEDU', 'tasa')…");
            AndraComponentes.andraAccesible(andraBusqueda, "Buscar en la ayuda");
            PauseTransition andraEspera = new PauseTransition(Duration.millis(300));
            andraEspera.setOnFinished(andraEvento -> {
                int andraFlecha = andraBusqueda.getCaretPosition();
                andraRedibujar();
                Platform.runLater(() -> {
                    andraBusqueda.requestFocus();
                    andraBusqueda.positionCaret(andraFlecha);
                });
            });
            andraBusqueda.textProperty().addListener((andraObservable, andraTextoA, andraTextoB) -> andraEspera.playFromStart());
        }
        return andraBusqueda;
    }

    @Override
    protected List<Node> andraConstruir() {
        String andraConsulta = andraBusqueda().getText() == null ? "" : andraBusqueda.getText().trim();
        List<AndraAyudaServicio.AndraArea> andraAreas = andraAyuda.andraAreas(andraConsulta);
        List<AndraAyudaServicio.AndraGuia> andraGuias = andraAyuda.andraGuias(andraConsulta);
        List<AndraAyudaServicio.AndraPregunta> andraPreguntas = andraAyuda.andraPreguntas(andraConsulta);
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada(null, "Ayuda", "Centro de asistencia, preguntas frecuentes y contacto directo con encargados de Mesa de Partes.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")));
        andraNodos.add(andraBuscador(andraConsulta));
        andraCanales = null;
        if (!andraConsulta.isBlank() && andraAreas.isEmpty() && andraGuias.isEmpty() && andraPreguntas.isEmpty()) {
            andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraVacio("fth-search", "Sin resultados para «" + andraConsulta + "»",
                    "Prueba con otra palabra o escríbenos por el chat de Mesa de Partes.")));
        }
        if (!andraAreas.isEmpty()) {
            andraNodos.add(andraAreas(andraAreas));
        }
        if (andraConsulta.isBlank()) {
            andraCanales = andraCanales();
            andraNodos.add(andraCanales);
        }
        if (!andraGuias.isEmpty()) {
            andraNodos.add(andraGuias(andraGuias));
        }
        if (!andraPreguntas.isEmpty()) {
            andraNodos.add(andraPreguntas(andraPreguntas, !andraConsulta.isBlank()));
        }
        if (andraConsulta.isBlank()) {
            andraNodos.add(andraEstado());
            andraNodos.add(andraSedes());
        }
        andraNodos.add(andraExtraordinaria());
        return andraNodos;
    }

    private VBox andraBuscador(String andraConsulta) {
        Label andraTitulo = AndraComponentes.andraParrafo("¿En qué podemos ayudarte hoy?", "h2");
        andraTitulo.setAlignment(Pos.CENTER);
        andraTitulo.setMaxWidth(Double.MAX_VALUE);
        Label andraSub = AndraComponentes.andraParrafo("Busca trámites, normativas, requisitos o soluciones rápidas para tu gestión académica.", "muted", "small");
        andraSub.setAlignment(Pos.CENTER);
        andraSub.setMaxWidth(Double.MAX_VALUE);
        StackPane andraCaja = new StackPane(andraBusqueda(), AndraComponentes.andraIcono("fth-search"));
        StackPane.setAlignment(andraCaja.getChildren().get(1), Pos.CENTER_LEFT);
        StackPane.setMargin(andraCaja.getChildren().get(1), new Insets(0, 0, 0, 14));
        andraCaja.getStyleClass().addAll("search", "search-lg");
        FlowPane andraFichas = AndraComponentes.andraFichas(AndraComponentes.andraEtiqueta("Búsquedas populares:", "hint"));
        andraFichas.getStyleClass().add("centro");
        for (String andraTexto : AndraAyudaServicio.ANDRA_POPULARES) {
            ToggleButton andraBotonOpcion = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcion, andraTexto);
            andraBotonOpcion.getStyleClass().add("chip");
            andraBotonOpcion.setSelected(andraTexto.equals(andraConsulta));
            andraBotonOpcion.setOnAction(andraEvento -> andraBusqueda.setText(andraTexto.equals(andraConsulta) ? "" : andraTexto));
            andraFichas.getChildren().add(andraBotonOpcion);
        }
        if (!andraConsulta.isBlank()) {
            andraFichas.getChildren().add(AndraComponentes.andraBotonSm("Limpiar", "btn-tertiary", null, andraEvento -> andraBusqueda.setText("")));
        }
        VBox andraPanel = new VBox(12, andraTitulo, andraSub, andraCaja, andraFichas);
        andraPanel.getStyleClass().add("panel");
        VBox andraSeccion = AndraComponentes.andraSeccion(andraPanel);
        andraSeccion.getStyleClass().add("sin-arriba");
        return andraSeccion;
    }

    private VBox andraAreas(List<AndraAyudaServicio.AndraArea> andraAreas) {
        AndraRejilla andraRejilla = new AndraRejilla(2, 340);
        for (AndraAyudaServicio.AndraArea andraArea : andraAreas) {
            StackPane andraCapa = new StackPane(new FontIcon(andraArea.andraIcono()));
            andraCapa.getStyleClass().add("area-ic");
            HBox andraArriba = new HBox(10, andraCapa, AndraComponentes.andraMayus(andraArea.andraEtiqueta(), "label-caps"), AndraComponentes.andraEspacio(), AndraComponentes.andraDistintivo("Guía oficial"));
            andraArriba.setAlignment(Pos.CENTER_LEFT);
            VBox andraPuntos = new VBox(4);
            andraArea.andraPuntos().forEach(andraTexto -> andraPuntos.getChildren().add(AndraComponentes.andraVineta(andraTexto, "vineta")));
            VBox andraTarjeta = new VBox(12, andraArriba, AndraComponentes.andraParrafo(andraArea.andraTitulo(), "h3"), AndraComponentes.andraParrafo(andraArea.andraDescripcion(), "muted", "small"), andraPuntos,
                    AndraComponentes.andraEspacioV(), AndraComponentes.andraEnlaceFlecha(andraArea.andraEnlace(), andraEvento -> andraModalArea(andraArea)));
            andraTarjeta.getStyleClass().add("area");
            andraRejilla.getChildren().add(andraTarjeta);
        }
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Explorar por áreas temáticas", "Selecciona una categoría para ver reglamentos y guías oficiales."), andraRejilla);
    }

    private void andraModalArea(AndraAyudaServicio.AndraArea andraArea) {
        VBox andraPuntos = new VBox(6);
        andraArea.andraPuntos().forEach(andraTexto -> andraPuntos.getChildren().add(AndraComponentes.andraVineta(andraTexto, "small")));
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo(andraArea.andraDescripcion(), "muted"), andraPuntos,
                AndraComponentes.andraParrafo("Guía oficial resumida. El reglamento completo está disponible en Documentos.", "hint"));
        andraNotificador.andraModal(andraArea.andraTitulo(), andraCuerpo, false, AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Ir a Documentos", "btn-primary", null, andraEvento -> {
                    andraNotificador.andraCerrarModal();
                    andraIr("documentos");
                }));
    }

    private VBox andraCanales() {
        AndraSede andraSede = andraContexto.andraSedeOPrincipal();
        VBox andraChat = andraCanal("fth-message-circle", AndraComponentes.andraPildora("En línea · Espera: 2 min", "success"), "Chat en vivo con Mesa de Partes",
                "Conversa en tiempo real con un operador académico para subsanar observaciones.", "Horario: lunes a viernes de 08:30 a 20:00 hrs",
                AndraComponentes.andraBoton("Iniciar chat con asesor", "btn-primary", null, andraEvento -> andraChat()));
        VBox andraLlamada = andraCanal("fth-phone", AndraComponentes.andraPildora("Central telefónica activa", "info"), "Línea telefónica y asesoría",
                "Llama directamente a la central para consultar el avance de tus solicitudes o solicita que un especialista de secretaría te devuelva la llamada.",
                "Central: " + andraInstitucion.getAndraTelefono() + " | Anexos: " + andraInstitucion.getAndraAnexos(),
                AndraComponentes.andraBoton("Solicitar llamada de un encargado", "btn-secondary", null, andraEvento -> andraLlamada()));
        VBox andraTurno = andraCanal("fth-tag", AndraComponentes.andraPildora("Turno digital sin colas", "neutral"), "Atención presencial en ventanilla",
                "Reserva un ticket digital para atención directa en ventanilla física.", "Sede " + andraSede.getAndraNombre() + " · " + andraSede.getAndraOficina(),
                AndraComponentes.andraBoton("Reservar turno presencial", "btn-secondary", null, andraEvento -> andraTurno()));
        AndraRejilla andraRejilla = new AndraRejilla(3, 260);
        andraRejilla.getChildren().addAll(andraChat, andraLlamada, andraTurno);
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Comunícate con un asesor o encargado", "Canales oficiales de soporte para resolución de dudas e incidencias."), andraRejilla);
    }

    private VBox andraCanal(String andraIcono, Label andraEstado, String andraTitulo, String andraTexto, String andraPie, Button andraAccion) {
        HBox andraArriba = new HBox(AndraComponentes.andraIcono(andraIcono, "chan-icono"), AndraComponentes.andraEspacio(), andraEstado);
        andraArriba.setAlignment(Pos.CENTER_LEFT);
        VBox andraCajaVertical = new VBox(12, andraArriba, AndraComponentes.andraParrafo(andraTitulo, "h3"), AndraComponentes.andraParrafo(andraTexto, "muted", "small"), AndraComponentes.andraParrafo(andraPie, "hint"),
                AndraComponentes.andraEspacioV(), AndraComponentes.andraBloque(andraAccion));
        andraCajaVertical.getStyleClass().add("chan");
        return andraCajaVertical;
    }

    private void andraChat() {
        VBox andraRegistro = new VBox(8);
        andraRegistro.getStyleClass().add("chat-log");
        Label andraSaludo = AndraComponentes.andraParrafo("Hola, " + andraSesion.andraEstudiante().andraObtenerPrimerNombre() + ". Soy el asesor de Mesa de Partes. ¿En qué te ayudo?", "msg", "op");
        andraRegistro.getChildren().add(andraBurbuja(andraSaludo, false));
        ScrollPane andraDesplazable2 = new ScrollPane(andraRegistro);
        andraDesplazable2.setFitToWidth(true);
        andraDesplazable2.setPrefViewportHeight(260);
        andraDesplazable2.getStyleClass().add("chat-scroll");
        TextField andraEntrada = new TextField();
        AndraComponentes.andraIndicacion(andraEntrada, "Escribe tu mensaje…");
        AndraComponentes.andraAccesible(andraEntrada, "Mensaje");
        HBox.setHgrow(andraEntrada, Priority.ALWAYS);
        Button andraEnviar = AndraComponentes.andraBoton("Enviar", "btn-primary", null, null);
        Runnable andraMandar = () -> {
            String andraTexto = andraEntrada.getText().trim();
            if (andraTexto.isEmpty()) {
                return;
            }
            andraRegistro.getChildren().add(andraBurbuja(AndraComponentes.andraLiteralParrafo(andraTexto, "msg", "me"), true));
            andraEntrada.clear();
            Platform.runLater(() -> andraDesplazable2.setVvalue(1));
            PauseTransition andraPausa = new PauseTransition(Duration.millis(700));
            andraPausa.setOnFinished(andraEvento -> {
                andraRegistro.getChildren().add(andraBurbuja(AndraComponentes.andraParrafo(AndraAyudaServicio.andraRespuestaChat(andraTexto), "msg", "op"), false));
                Platform.runLater(() -> andraDesplazable2.setVvalue(1));
            });
            andraPausa.play();
        };
        andraEnviar.setOnAction(andraEvento -> andraMandar.run());
        andraEntrada.setOnAction(andraEvento -> andraMandar.run());
        HBox andraFila = new HBox(andraEntrada, andraEnviar);
        andraFila.getStyleClass().add("copy-row");
        VBox andraCuerpo = new VBox(10, AndraComponentes.andraParrafo("Simulación: las respuestas son automáticas de demostración.", "hint"), andraDesplazable2, andraFila);
        andraNotificador.andraModal("Chat con Mesa de Partes", andraCuerpo, false);
        Platform.runLater(andraEntrada::requestFocus);
    }

    private HBox andraBurbuja(Label andraEtiqueta, boolean andraMio) {
        andraEtiqueta.setMaxWidth(380);
        HBox andraAlto = new HBox(andraEtiqueta);
        andraAlto.setAlignment(andraMio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return andraAlto;
    }

    private void andraLlamada() {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextField> andraTel = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Número de celular", "9 dígitos", "", 9)).andraRequerido("Ingresa tu número.")
                .andraPatron("9\\d{8}", "Ingresa un celular válido de 9 dígitos que empiece con 9.");
        AndraCampo<ComboBox<AndraOpcion>> andraFranja = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Franja horaria", AndraOpcion.andraLista(List.of("08:30 - 11:00", "11:00 - 14:00",
                "14:00 - 17:00", "17:00 - 20:00")), "Seleccionar", null)).andraRequerido("Elige una franja.");
        AndraCampo<TextArea> andraTema = andraFormulario.andraAgregar(AndraFormulario.andraArea("Tema", null)).andraRequerido("Cuéntanos el tema de la llamada.").andraMinimo(10);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Un especialista de secretaría te llamará en la franja elegida.", "muted", "small"),
                andraTel.andraNodo(), andraFranja.andraNodo(), andraTema.andraNodo());
        andraNotificador.andraModal("Solicitar llamada", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Solicitar llamada", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar()) {
                        andraNotificador.andraCerrarModal();
                        andraNotificador.andraExito("Llamada programada (" + AndraFormulario.andraValor(andraFranja.andraControl()) + "). Te llamaremos al " + andraTel.andraControl().getText() + ".");
                    }
                }));
    }

    private void andraTurno() {
        AndraFormulario andraFormulario = new AndraFormulario();
        List<AndraOpcion> andraSedes = new ArrayList<>();
        andraCatalogo.andraSedes().forEach(andraSede -> andraSedes.add(AndraOpcion.andraDe(andraSede.getAndraCodigo(), andraSede.getAndraNombreCompleto() + " · " + andraSede.getAndraOficina())));
        AndraCampo<ComboBox<AndraOpcion>> andraSede2 = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Sede", andraSedes, null, andraContexto.andraSedeOPrincipal().getAndraCodigo()));
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Fecha", AndraFechas.andraHoy(), AndraFechas.andraHoy(), null)).andraRequerido("Elige una fecha.");
        AndraCampo<ComboBox<AndraOpcion>> andraHora = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Hora", AndraOpcion.andraLista(List.of("08:30", "09:30", "10:30", "11:30", "14:00", "15:00", "16:00", "17:00")),
                "Seleccionar", null)).andraRequerido("Elige una hora.");
        AndraCampo<ComboBox<AndraOpcion>> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Motivo", AndraOpcion.andraLista(List.of("Entrega de documentos", "Consulta de expediente",
                "Pagos y tasas", "Otro")), "Seleccionar", null)).andraRequerido("Elige el motivo.");
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Elige la sede donde quieres ser atendido.", "muted", "small"), andraSede2.andraNodo(),
                AndraFormulario.andraRejilla(2, andraFecha.andraNodo(), andraHora.andraNodo()), andraMotivo.andraNodo());
        andraNotificador.andraModal("Reservar turno presencial", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Reservar turno", "btn-primary", null, andraEvento -> {
                    if (!andraFormulario.andraValidar()) {
                        return;
                    }
                    String andraTicket = "T-" + (100 + new Random().nextInt(900));
                    AndraSede andraSede = andraCatalogo.andraSede(AndraFormulario.andraValor(andraSede2.andraControl())).orElseThrow();
                    VBox andraCajaVertical = new VBox(6, AndraComponentes.andraMayus("Tu ticket", "label-caps"), AndraComponentes.andraLiteral(andraTicket, "dato-grande"),
                            AndraComponentes.andraParrafo(andraSede.getAndraNombreCompleto() + " · " + AndraFechas.andraLargo(andraFecha.andraControl().getValue()) + " · " + AndraFormulario.andraValor(andraHora.andraControl()), "muted"),
                            AndraComponentes.andraParrafo("Preséntate 5 minutos antes con tu carné universitario.", "hint"));
                    andraCajaVertical.setAlignment(Pos.CENTER);
                    andraNotificador.andraModal("Turno reservado", andraCajaVertical, false, AndraComponentes.andraBoton("Listo", "btn-primary", null, andraPosicionX -> andraNotificador.andraCerrarModal()));
                }));
    }

    private VBox andraGuias(List<AndraAyudaServicio.AndraGuia> andraGuias) {
        AndraRejilla andraRejilla = new AndraRejilla(3, 260);
        for (AndraAyudaServicio.AndraGuia andraGuia : andraGuias) {
            VBox andraPasos = new VBox(4);
            for (int andraIndice = 0; andraIndice < andraGuia.andraPasos().size(); andraIndice++) {
                Label andraEtiqueta = AndraComponentes.andraLiteral((andraIndice + 1) + ".", "vineta");
                Label andraEtiqueta2 = AndraComponentes.andraParrafo(andraGuia.andraPasos().get(andraIndice), "vineta");
                HBox.setHgrow(andraEtiqueta2, Priority.ALWAYS);
                andraPasos.getChildren().add(new HBox(6, andraEtiqueta, andraEtiqueta2));
            }
            VBox andraTarjeta = new VBox(12, new HBox(AndraComponentes.andraDistintivo(andraGuia.andraPaso())), AndraComponentes.andraParrafo(andraGuia.andraTitulo(), "h3"), andraPasos, AndraComponentes.andraEspacioV(),
                    AndraComponentes.andraEnlaceFlecha(andraGuia.andraEnlace(), andraEvento -> andraModalGuia(andraGuia)));
            andraTarjeta.getStyleClass().add("guide");
            andraRejilla.getChildren().add(andraTarjeta);
        }
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Guías esenciales de resolución rápida", "Procedimientos clave para gestionar tus trámites universitarios."), andraRejilla);
    }

    private void andraModalGuia(AndraAyudaServicio.AndraGuia andraGuia) {
        VBox andraLista = new VBox(10);
        for (int andraIndice = 0; andraIndice < andraGuia.andraPasos().size(); andraIndice++) {
            StackPane andraCapa = new StackPane(AndraComponentes.andraLiteral(String.valueOf(andraIndice + 1)));
            andraCapa.getStyleClass().add("tl-dot");
            Label andraEtiqueta = AndraComponentes.andraParrafo(andraGuia.andraPasos().get(andraIndice));
            HBox.setHgrow(andraEtiqueta, Priority.ALWAYS);
            HBox andraFila = new HBox(14, andraCapa, andraEtiqueta);
            andraFila.getStyleClass().add("tl-item");
            andraLista.getChildren().add(andraFila);
        }
        String andraDestino = "documentos".equals(andraGuia.andraDestino()) ? "Documentos" : "Mis Solicitudes";
        andraNotificador.andraModal(andraGuia.andraTitulo(), andraLista, false, AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Ir a " + andraDestino, "btn-primary", null, andraEvento -> {
                    andraNotificador.andraCerrarModal();
                    andraIr(andraGuia.andraDestino());
                }));
    }

    private VBox andraPreguntas(List<AndraAyudaServicio.AndraPregunta> andraPreguntas, boolean andraAbrirTodas) {
        List<Node> andraFilas = new ArrayList<>();
        for (AndraAyudaServicio.AndraPregunta andraPregunta : andraPreguntas) {
            int andraIndice = AndraAyudaServicio.ANDRA_PREGUNTAS.indexOf(andraPregunta);
            boolean andraAbierta = andraAbrirTodas || andraAbiertas.contains(andraIndice);
            VBox andraTextos = new VBox(2, AndraComponentes.andraMayus(andraPregunta.andraCategoria(), "label-caps"), AndraComponentes.andraParrafo(andraPregunta.andraPregunta(), "faq-pregunta"));
            HBox.setHgrow(andraTextos, Priority.ALWAYS);
            FontIcon andraFlecha = new FontIcon(andraAbierta ? "fth-chevron-up" : "fth-chevron-down");
            HBox andraCajaHorizontal = new HBox(andraTextos, andraFlecha);
            andraCajaHorizontal.getStyleClass().add("faq-q");
            andraCajaHorizontal.setFocusTraversable(true);
            Label andraRespuesta = AndraComponentes.andraParrafo(andraPregunta.andraRespuesta(), "faq-a");
            andraRespuesta.setVisible(andraAbierta);
            andraRespuesta.setManaged(andraAbierta);
            AndraComponentes.andraAccesible(andraCajaHorizontal, andraPregunta.andraPregunta());
            Runnable andraAlternar = () -> {
                boolean andraAhora = !andraRespuesta.isVisible();
                andraRespuesta.setVisible(andraAhora);
                andraRespuesta.setManaged(andraAhora);
                andraFlecha.setIconLiteral(andraAhora ? "fth-chevron-up" : "fth-chevron-down");
                if (andraAhora) {
                    andraAbiertas.add(andraIndice);
                } else {
                    andraAbiertas.remove(andraIndice);
                }
            };
            andraCajaHorizontal.setOnMouseClicked(andraEvento -> andraAlternar.run());
            andraCajaHorizontal.setOnKeyPressed(andraEvento -> {
                if (andraEvento.getCode() == javafx.scene.input.KeyCode.ENTER || andraEvento.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    andraAlternar.run();
                }
            });
            VBox andraFaq = new VBox(andraCajaHorizontal, andraRespuesta);
            andraFaq.getStyleClass().add("faq");
            andraFilas.add(andraFaq);
        }
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Preguntas frecuentes sobre trámites y servicios", "Respuestas a las consultas más comunes de la comunidad universitaria."),
                AndraComponentes.andraLista(andraFilas));
    }

    private VBox andraEstado() {
        VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo("Todos los sistemas de atención y trámites se encuentran operativos al 100%", "status-titulo"),
                AndraComponentes.andraParrafo("Mesa de Partes Virtual, Validación Reniec, Pasarela de Pagos y Custodia funcionando sin demoras.", "small", "muted"));
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        FlowPane andraFichas = AndraComponentes.andraFichas();
        for (String andraTexto : List.of("Trámites", "SUNEDU API", "Reservas", "Equipos")) {
            andraFichas.getChildren().add(AndraComponentes.andraPildora(andraTexto + ": OK", "success"));
        }
        andraFichas.setMaxWidth(380);
        HBox andraBarra = new HBox(new FontIcon("fth-check-circle"), andraTextos, andraFichas);
        andraBarra.getStyleClass().add("status-bar");
        return AndraComponentes.andraSeccion(andraBarra);
    }

    private VBox andraSedes() {
        AndraSede andraActual = andraContexto.andraSede();
        List<AndraSede> andraOrden = new ArrayList<>();
        if (andraActual != null) {
            andraOrden.add(andraActual);
        }
        andraCatalogo.andraSedes().stream().filter(andraSede -> andraActual == null || !andraSede.getAndraId().equals(andraActual.getAndraId())).forEach(andraOrden::add);
        AndraRejilla andraRejilla = new AndraRejilla(2, 340);
        for (AndraSede andraSede : andraOrden) {
            HBox andraCab = new HBox(8, AndraComponentes.andraParrafo("Mesa de Partes · " + andraSede.getAndraNombreCompleto(), "h3"), AndraComponentes.andraEspacio());
            if (andraActual != null && andraSede.getAndraId().equals(andraActual.getAndraId())) {
                andraCab.getChildren().add(AndraComponentes.andraPildora("Tu sede", "info"));
            }
            String andraAtiende = String.join(", ", andraCatalogo.andraEscuelasDe(andraSede).stream().map(andraEscuelaProfesional -> andraEscuelaProfesional.getAndraNombre()).toList());
            VBox andraCajaVertical = new VBox(12, andraCab, AndraComponentes.andraKv("Ubicación", andraSede.getAndraOficina(), "Horario", andraSede.getAndraHorario(),
                    "Atiende a", andraAtiende.isBlank() ? "Auditorios, eventos y oficinas centrales" : andraAtiende, "Correo", andraCopiar(andraInstitucion.getAndraCorreoMesaPartes())));
            andraCajaVertical.getStyleClass().add("office");
            andraRejilla.getChildren().add(andraCajaVertical);
        }
        VBox andraSeccionSoporte = new VBox(12, AndraComponentes.andraParrafo("Defensoría Universitaria y Mesa de Ayuda de TI", "h3"), AndraComponentes.andraKv(
                "Ubicación", "Edificio de Bienestar Universitario · Piso 1 (Oficina 100)", "Atención técnica", "Lunes a viernes de 09:00 a 16:30 hrs",
                "Soporte y credenciales", andraCopiar(andraInstitucion.getAndraCorreoSoporte()), "Defensoría legal", andraCopiar(andraInstitucion.getAndraCorreoDefensoria())));
        andraSeccionSoporte.getStyleClass().add("office");
        andraRejilla.getChildren().add(andraSeccionSoporte);
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Sedes y directorio institucional",
                "Mesa de Partes en las sedes Central, Norte y Sur. Primero aparece tu sede seleccionada."), andraRejilla);
    }

    private HBox andraCopiar(String andraCorreo) {
        Button andraBoton = AndraComponentes.andraBotonSm("Copiar", "btn-tertiary", null, andraEvento -> {
            AndraPortapapeles.andraCopiar(andraCorreo);
            andraNotificador.andraExito("Correo copiado.");
        });
        HBox andraAlto = new HBox(6, AndraComponentes.andraLiteral(andraCorreo, "dd"), andraBoton);
        andraAlto.setAlignment(Pos.CENTER_LEFT);
        return andraAlto;
    }

    private VBox andraExtraordinaria() {
        VBox andraIzquierda = new VBox(8, AndraComponentes.andraParrafo("¿No encontraste la respuesta a tu trámite o consulta?", "cta-titulo"),
                AndraComponentes.andraParrafo("Si tu caso corresponde a una excepción académica, solicitud de gracia, convalidación especial o reclamo formal, ingresa un expediente extraordinario en Mesa de Partes Virtual para evaluación de Decanato.", "muted", "small"),
                new HBox(AndraComponentes.andraDistintivo("Tiempo promedio de respuesta: 3 a 5 días hábiles")));
        andraIzquierda.setMaxWidth(640);
        HBox.setHgrow(andraIzquierda, Priority.ALWAYS);
        Button andraIngresar = AndraComponentes.andraBoton("Ingresar solicitud extraordinaria", "btn-primary", "fth-arrow-right", andraEvento -> andraModalExtraordinaria());
        andraIngresar.setContentDisplay(ContentDisplay.RIGHT);
        VBox andraUtilidad2;
        if (andraUtilidad == null) {
            andraUtilidad2 = new VBox(6, AndraComponentes.andraEtiqueta("¿Te resultó útil esta sección de ayuda?", "hint"), new HBox(6,
                    AndraComponentes.andraBotonSm("Sí", "btn-secondary", null, andraEvento -> {
                        andraUtilidad = true;
                        andraRedibujar();
                    }), AndraComponentes.andraBotonSm("No", "btn-secondary", null, andraEvento -> {
                        andraUtilidad = false;
                        andraRedibujar();
                    })));
        } else {
            andraUtilidad2 = new VBox(AndraComponentes.andraParrafo(andraUtilidad ? "Gracias por tu respuesta." : "Gracias. Usaremos tu respuesta para mejorar esta sección.", "hint"));
        }
        VBox andraDerecha = new VBox(12, andraIngresar, andraUtilidad2);
        HBox andraBanda = new HBox(andraIzquierda, andraDerecha);
        andraBanda.getStyleClass().add("cta-band");
        return AndraComponentes.andraSeccion(andraBanda);
    }

    private void andraModalExtraordinaria() {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Tipo de caso", AndraOpcion.andraLista(List.of("Excepción académica", "Solicitud de gracia",
                "Convalidación especial", "Reclamo formal")), "Seleccionar", null)).andraRequerido("Elige el tipo de caso.");
        AndraCampo<TextArea> andraDesc = andraFormulario.andraAgregar(AndraFormulario.andraArea("Describe tu caso", null)).andraRequerido("Describe tu caso.").andraMinimo(20);
        AndraCampo<AndraFormulario.AndraArchivo> andraAdj = andraFormulario.andraAgregar(AndraFormulario.andraArchivo("Sustento (opcional)", "*.pdf *.jpg *.png", null, andraNotificador, null));
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Será evaluada por Decanato. Tiempo promedio de respuesta: 3 a 5 días hábiles.", "muted", "small"),
                andraTipo.andraNodo(), andraDesc.andraNodo(), andraAdj.andraNodo());
        andraNotificador.andraModal("Solicitud extraordinaria", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Enviar solicitud", "btn-primary", null, andraEvento -> {
                    if (!andraFormulario.andraValidar()) {
                        return;
                    }
                    try {
                        AndraExpediente andraPosicionX = andraExpedientes.andraExtraordinaria(AndraFormulario.andraValor(andraTipo.andraControl()), andraDesc.andraControl().getText(), andraAdj.andraTexto());
                        andraNotificador.andraCerrarModal();
                        andraNotificador.andraExito("Expediente " + andraPosicionX.getAndraCodigo() + " registrado. Puedes seguirlo en Mis Solicitudes.");
                    } catch (pe.andra.exception.AndraNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                }));
    }
}
