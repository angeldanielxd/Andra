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
import pe.andra.model.DixonExpediente;
import pe.andra.model.DixonSede;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonPortapapeles;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AndraAyudaControlador extends AndraVistaBase {

    private final DixonAyudaServicio andraAyuda;
    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonSesionServicio andraSesion;
    private final DixonExpedienteServicio andraExpedientes;
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
        List<DixonAyudaServicio.DixonArea> andraAreas = andraAyuda.dixonAreas(andraConsulta);
        List<DixonAyudaServicio.DixonGuia> andraGuias = andraAyuda.dixonGuias(andraConsulta);
        List<DixonAyudaServicio.DixonPregunta> andraPreguntas = andraAyuda.dixonPreguntas(andraConsulta);
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada(null, "Ayuda", "Centro de asistencia, preguntas frecuentes y contacto directo con encargados de Mesa de Partes.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")));
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
        for (String andraTexto : DixonAyudaServicio.DIXON_POPULARES) {
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

    private VBox andraAreas(List<DixonAyudaServicio.DixonArea> andraAreas) {
        AndraRejilla andraRejilla = new AndraRejilla(2, 340);
        for (DixonAyudaServicio.DixonArea andraArea : andraAreas) {
            StackPane andraCapa = new StackPane(new FontIcon(andraArea.dixonIcono()));
            andraCapa.getStyleClass().add("area-ic");
            HBox andraArriba = new HBox(10, andraCapa, AndraComponentes.andraMayus(andraArea.dixonEtiqueta(), "label-caps"), AndraComponentes.andraEspacio(), AndraComponentes.andraDistintivo("Guía oficial"));
            andraArriba.setAlignment(Pos.CENTER_LEFT);
            VBox andraPuntos = new VBox(4);
            andraArea.dixonPuntos().forEach(andraTexto -> andraPuntos.getChildren().add(AndraComponentes.andraVineta(andraTexto, "vineta")));
            VBox andraTarjeta = new VBox(12, andraArriba, AndraComponentes.andraParrafo(andraArea.dixonTitulo(), "h3"), AndraComponentes.andraParrafo(andraArea.dixonDescripcion(), "muted", "small"), andraPuntos,
                    AndraComponentes.andraEspacioV(), AndraComponentes.andraEnlaceFlecha(andraArea.dixonEnlace(), andraEvento -> andraModalArea(andraArea)));
            andraTarjeta.getStyleClass().add("area");
            andraRejilla.getChildren().add(andraTarjeta);
        }
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Explorar por áreas temáticas", "Selecciona una categoría para ver reglamentos y guías oficiales."), andraRejilla);
    }

    private void andraModalArea(DixonAyudaServicio.DixonArea andraArea) {
        VBox andraPuntos = new VBox(6);
        andraArea.dixonPuntos().forEach(andraTexto -> andraPuntos.getChildren().add(AndraComponentes.andraVineta(andraTexto, "small")));
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo(andraArea.dixonDescripcion(), "muted"), andraPuntos,
                AndraComponentes.andraParrafo("Guía oficial resumida. El reglamento completo está disponible en Documentos.", "hint"));
        andraNotificador.andraModal(andraArea.dixonTitulo(), andraCuerpo, false, AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Ir a Documentos", "btn-primary", null, andraEvento -> {
                    andraNotificador.andraCerrarModal();
                    andraIr("documentos");
                }));
    }

    private VBox andraCanales() {
        DixonSede andraSede = andraContexto.dixonSedeOPrincipal();
        VBox andraChat = andraCanal("fth-message-circle", AndraComponentes.andraPildora("En línea · Espera: 2 min", "success"), "Chat en vivo con Mesa de Partes",
                "Conversa en tiempo real con un operador académico para subsanar observaciones.", "Horario: lunes a viernes de 08:30 a 20:00 hrs",
                AndraComponentes.andraBoton("Iniciar chat con asesor", "btn-primary", null, andraEvento -> andraChat()));
        VBox andraLlamada = andraCanal("fth-phone", AndraComponentes.andraPildora("Central telefónica activa", "info"), "Línea telefónica y asesoría",
                "Llama directamente a la central para consultar el avance de tus solicitudes o solicita que un especialista de secretaría te devuelva la llamada.",
                "Central: " + andraInstitucion.getAndraTelefono() + " | Anexos: " + andraInstitucion.getAndraAnexos(),
                AndraComponentes.andraBoton("Solicitar llamada de un encargado", "btn-secondary", null, andraEvento -> andraLlamada()));
        VBox andraTurno = andraCanal("fth-tag", AndraComponentes.andraPildora("Turno digital sin colas", "neutral"), "Atención presencial en ventanilla",
                "Reserva un ticket digital para atención directa en ventanilla física.", "Sede " + andraSede.getDixonNombre() + " · " + andraSede.getDixonOficina(),
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
        Label andraSaludo = AndraComponentes.andraParrafo("Hola, " + andraSesion.dixonEstudiante().dixonObtenerPrimerNombre() + ". Soy el asesor de Mesa de Partes. ¿En qué te ayudo?", "msg", "op");
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
                andraRegistro.getChildren().add(andraBurbuja(AndraComponentes.andraParrafo(DixonAyudaServicio.dixonRespuestaChat(andraTexto), "msg", "op"), false));
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
        andraCatalogo.dixonSedes().forEach(andraSede -> andraSedes.add(AndraOpcion.andraDe(andraSede.getDixonCodigo(), andraSede.getDixonNombreCompleto() + " · " + andraSede.getDixonOficina())));
        AndraCampo<ComboBox<AndraOpcion>> andraSede2 = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Sede", andraSedes, null, andraContexto.dixonSedeOPrincipal().getDixonCodigo()));
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Fecha", DixonFechas.dixonHoy(), DixonFechas.dixonHoy(), null)).andraRequerido("Elige una fecha.");
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
                    DixonSede andraSede = andraCatalogo.dixonSede(AndraFormulario.andraValor(andraSede2.andraControl())).orElseThrow();
                    VBox andraCajaVertical = new VBox(6, AndraComponentes.andraMayus("Tu ticket", "label-caps"), AndraComponentes.andraLiteral(andraTicket, "dato-grande"),
                            AndraComponentes.andraParrafo(andraSede.getDixonNombreCompleto() + " · " + DixonFechas.dixonLargo(andraFecha.andraControl().getValue()) + " · " + AndraFormulario.andraValor(andraHora.andraControl()), "muted"),
                            AndraComponentes.andraParrafo("Preséntate 5 minutos antes con tu carné universitario.", "hint"));
                    andraCajaVertical.setAlignment(Pos.CENTER);
                    andraNotificador.andraModal("Turno reservado", andraCajaVertical, false, AndraComponentes.andraBoton("Listo", "btn-primary", null, andraPosicionX -> andraNotificador.andraCerrarModal()));
                }));
    }

    private VBox andraGuias(List<DixonAyudaServicio.DixonGuia> andraGuias) {
        AndraRejilla andraRejilla = new AndraRejilla(3, 260);
        for (DixonAyudaServicio.DixonGuia andraGuia : andraGuias) {
            VBox andraPasos = new VBox(4);
            for (int andraIndice = 0; andraIndice < andraGuia.dixonPasos().size(); andraIndice++) {
                Label andraEtiqueta = AndraComponentes.andraLiteral((andraIndice + 1) + ".", "vineta");
                Label andraEtiqueta2 = AndraComponentes.andraParrafo(andraGuia.dixonPasos().get(andraIndice), "vineta");
                HBox.setHgrow(andraEtiqueta2, Priority.ALWAYS);
                andraPasos.getChildren().add(new HBox(6, andraEtiqueta, andraEtiqueta2));
            }
            VBox andraTarjeta = new VBox(12, new HBox(AndraComponentes.andraDistintivo(andraGuia.dixonPaso())), AndraComponentes.andraParrafo(andraGuia.dixonTitulo(), "h3"), andraPasos, AndraComponentes.andraEspacioV(),
                    AndraComponentes.andraEnlaceFlecha(andraGuia.dixonEnlace(), andraEvento -> andraModalGuia(andraGuia)));
            andraTarjeta.getStyleClass().add("guide");
            andraRejilla.getChildren().add(andraTarjeta);
        }
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Guías esenciales de resolución rápida", "Procedimientos clave para gestionar tus trámites universitarios."), andraRejilla);
    }

    private void andraModalGuia(DixonAyudaServicio.DixonGuia andraGuia) {
        VBox andraLista = new VBox(10);
        for (int andraIndice = 0; andraIndice < andraGuia.dixonPasos().size(); andraIndice++) {
            StackPane andraCapa = new StackPane(AndraComponentes.andraLiteral(String.valueOf(andraIndice + 1)));
            andraCapa.getStyleClass().add("tl-dot");
            Label andraEtiqueta = AndraComponentes.andraParrafo(andraGuia.dixonPasos().get(andraIndice));
            HBox.setHgrow(andraEtiqueta, Priority.ALWAYS);
            HBox andraFila = new HBox(14, andraCapa, andraEtiqueta);
            andraFila.getStyleClass().add("tl-item");
            andraLista.getChildren().add(andraFila);
        }
        String andraDestino = "documentos".equals(andraGuia.dixonDestino()) ? "Documentos" : "Mis Solicitudes";
        andraNotificador.andraModal(andraGuia.dixonTitulo(), andraLista, false, AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Ir a " + andraDestino, "btn-primary", null, andraEvento -> {
                    andraNotificador.andraCerrarModal();
                    andraIr(andraGuia.dixonDestino());
                }));
    }

    private VBox andraPreguntas(List<DixonAyudaServicio.DixonPregunta> andraPreguntas, boolean andraAbrirTodas) {
        List<Node> andraFilas = new ArrayList<>();
        for (DixonAyudaServicio.DixonPregunta andraPregunta : andraPreguntas) {
            int andraIndice = DixonAyudaServicio.DIXON_PREGUNTAS.indexOf(andraPregunta);
            boolean andraAbierta = andraAbrirTodas || andraAbiertas.contains(andraIndice);
            VBox andraTextos = new VBox(2, AndraComponentes.andraMayus(andraPregunta.dixonCategoria(), "label-caps"), AndraComponentes.andraParrafo(andraPregunta.dixonPregunta(), "faq-pregunta"));
            HBox.setHgrow(andraTextos, Priority.ALWAYS);
            FontIcon andraFlecha = new FontIcon(andraAbierta ? "fth-chevron-up" : "fth-chevron-down");
            HBox andraCajaHorizontal = new HBox(andraTextos, andraFlecha);
            andraCajaHorizontal.getStyleClass().add("faq-q");
            andraCajaHorizontal.setFocusTraversable(true);
            Label andraRespuesta = AndraComponentes.andraParrafo(andraPregunta.dixonRespuesta(), "faq-a");
            andraRespuesta.setVisible(andraAbierta);
            andraRespuesta.setManaged(andraAbierta);
            AndraComponentes.andraAccesible(andraCajaHorizontal, andraPregunta.dixonPregunta());
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
        DixonSede andraActual = andraContexto.dixonSede();
        List<DixonSede> andraOrden = new ArrayList<>();
        if (andraActual != null) {
            andraOrden.add(andraActual);
        }
        andraCatalogo.dixonSedes().stream().filter(andraSede -> andraActual == null || !andraSede.getDixonId().equals(andraActual.getDixonId())).forEach(andraOrden::add);
        AndraRejilla andraRejilla = new AndraRejilla(2, 340);
        for (DixonSede andraSede : andraOrden) {
            HBox andraCab = new HBox(8, AndraComponentes.andraParrafo("Mesa de Partes · " + andraSede.getDixonNombreCompleto(), "h3"), AndraComponentes.andraEspacio());
            if (andraActual != null && andraSede.getDixonId().equals(andraActual.getDixonId())) {
                andraCab.getChildren().add(AndraComponentes.andraPildora("Tu sede", "info"));
            }
            String andraAtiende = String.join(", ", andraCatalogo.dixonEscuelasDe(andraSede).stream().map(andraEscuelaProfesional -> andraEscuelaProfesional.getDixonNombre()).toList());
            VBox andraCajaVertical = new VBox(12, andraCab, AndraComponentes.andraKv("Ubicación", andraSede.getDixonOficina(), "Horario", andraSede.getDixonHorario(),
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
            DixonPortapapeles.dixonCopiar(andraCorreo);
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
                        DixonExpediente andraPosicionX = andraExpedientes.dixonExtraordinaria(AndraFormulario.andraValor(andraTipo.andraControl()), andraDesc.andraControl().getText(), andraAdj.andraTexto());
                        andraNotificador.andraCerrarModal();
                        andraNotificador.andraExito("Expediente " + andraPosicionX.getDixonCodigo() + " registrado. Puedes seguirlo en Mis Solicitudes.");
                    } catch (pe.andra.exception.DixonNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                }));
    }
}
