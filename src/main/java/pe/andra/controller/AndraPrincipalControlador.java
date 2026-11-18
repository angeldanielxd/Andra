package pe.andra.controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AndraPrincipalControlador {

    private final ApplicationContext andraContexto;
    private final AndraNotificador andraNotificador;
    private final DixonContextoServicio andraContextoServicio;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonNotificacionServicio andraNotificaciones;
    private final DixonDemoServicio andraDemo;
    private final DixonSesionServicio andraSesion;
    private final DixonIdiomaServicio andraIdioma;
    private final DixonAjustesServicio andraAjustes;
    private final DixonConexionServicio andraConexion;

    @FXML private StackPane andraRaiz;
    @FXML private StackPane andraAplicacion;
    @FXML private StackPane andraContenido;
    @FXML private VBox andraCapaCabecera;
    @FXML private Pane andraCapaFlotante;
    @FXML private StackPane andraCapaModal;
    @FXML private VBox andraZonaAvisos;

    private Stage andraVentana;
    private final ToggleGroup andraGrupoNavegacion = new ToggleGroup();
    private final Map<String, AndraVista> andraControladores = new HashMap<>();
    private final Map<String, Parent> andraVistas = new HashMap<>();
    private String andraRutaActual;
    private AndraVidrioDasm andraVidrio;
    private HBox andraNavegacion;
    private Label andraInsignia;
    private Circle andraPuntoEscuela;
    private Button andraBotonEscuela;
    private Button andraBotonSede;
    private Button andraBotonCampana;
    private Button andraBotonUsuario;
    private Button andraBotonIdioma;
    private Label andraLema;
    private HBox andraBarraSinConexion;
    private boolean andraSilencioNavegacion;

    @FXML
    public void initialize() {
        andraNotificador.andraRegistrar(andraRaiz, andraAplicacion, andraCapaFlotante, andraCapaModal, andraZonaAvisos);
        andraRaiz.addEventHandler(AndraNavegacion.ANDRA_IR, andraNavegacion2 -> andraNavegar(andraNavegacion2.andraObtenerRuta()));
        DixonAjustes andraAjustes2 = andraAjustes.dixonObtener();
        andraIdioma.dixonAsignarIdioma(andraAjustes2.getDixonIdioma());
        AndraMovimiento.andraReducidoPropiedad().set(andraAjustes2.isDixonReducirMovimiento());
        andraConstruirCabecera();
        andraConexion.dixonEnLineaPropiedad().addListener((andraObservable, andraAntes, andraAhora) -> andraAlCambiarConexion(andraAntes, andraAhora));
        andraConexion.dixonPendientesPropiedad().addListener((andraObservable, andraPosicionX, andraPosicionY) -> andraActualizarBarraSinConexion());
        Timeline andraRefresco = new Timeline(new KeyFrame(Duration.millis(500), andraEvento -> andraVidrio.andraMarcar()));
        andraRefresco.setCycleCount(Timeline.INDEFINITE);
        andraRefresco.play();
        andraGrupoNavegacion.selectedToggleProperty().addListener((andraObs, andraAnterior, andraActual) -> {
            if (andraActual == null) {
                if (andraAnterior != null && !andraSilencioNavegacion) {
                    andraGrupoNavegacion.selectToggle(andraAnterior);
                }
                return;
            }
            if (!andraSilencioNavegacion) {
                andraMostrar((String) andraActual.getUserData());
            }
        });
    }

    public void andraIniciar(Stage andraVentana) {
        this.andraVentana = andraVentana;
        andraVentana.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::andraAtajos);
        andraRaiz.widthProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> andraLema.setVisible(andraNumeroB.doubleValue() > 1120));
        andraArrancarSesion();
    }

    public void andraGuardarVentana() {
        if (andraVentana == null) {
            return;
        }
        andraAjustes.dixonCambiar(andraAjustes2 -> {
            andraAjustes2.setDixonMaximizada(andraVentana.isMaximized());
            if (!andraVentana.isMaximized()) {
                andraAjustes2.setDixonVentanaX(andraVentana.getX());
                andraAjustes2.setDixonVentanaY(andraVentana.getY());
                andraAjustes2.setDixonVentanaAncho(andraVentana.getWidth());
                andraAjustes2.setDixonVentanaAlto(andraVentana.getScene().getHeight());
            }
        });
    }

    private void andraAtajos(KeyEvent andraEvento) {
        if (andraEvento.getCode() == KeyCode.ESCAPE) {
            if (andraNotificador.andraCerrarArriba()) {
                andraEvento.consume();
            }
            return;
        }
        boolean andraMod = andraEvento.isShortcutDown();
        if (!andraMod || !andraSesion.dixonConectado()) {
            return;
        }
        if (andraEvento.getCode() == KeyCode.F) {
            AndraVista andraVista = andraControladores.get(andraRutaActual);
            if (andraVista != null) {
                andraVista.andraEnfocarBusqueda();
                andraEvento.consume();
            }
            return;
        }
        if (andraEvento.getCode().isDigitKey()) {
            int andraCantidad = andraEvento.getCode().getCode() - KeyCode.DIGIT1.getCode();
            List<DixonSesionServicio.DixonRuta> andraRutas = DixonSesionServicio.dixonRutas(andraSesion.dixonRol());
            if (andraCantidad >= 0 && andraCantidad < andraRutas.size() && !andraNotificador.andraHayModal()) {
                andraIrA(andraRutas.get(andraCantidad).dixonClave());
                andraEvento.consume();
            }
        }
    }

    private void andraArrancarSesion() {
        andraControladores.values().forEach(AndraVista::andraSalir);
        andraControladores.clear();
        andraVistas.clear();
        andraNotificador.andraCerrarArriba();
        andraConstruirNavegacion();
        boolean andraConectado = andraSesion.dixonConectado();
        andraCapaCabecera.setVisible(andraConectado);
        andraCapaCabecera.setManaged(andraConectado);
        if (!andraConectado) {
            andraMostrar("login");
            return;
        }
        andraActualizarCabecera();
        andraIrA(DixonSesionServicio.dixonRutaInicial(andraSesion.dixonRol()));
    }

    public void andraAlIngresar() {
        andraArrancarSesion();
        if (andraSesion.dixonEsAdministrador()) {
            andraNotificador.andraExito("Hola, " + andraSesion.dixonAdministrador().dixonObtenerPrimerNombre() + ". Bienvenida al panel.");
        } else {
            andraNotificador.andraExito("Hola, " + andraSesion.dixonEstudiante().dixonObtenerPrimerNombre() + ".");
        }
    }

    private void andraConstruirCabecera() {
        andraVidrio = new AndraVidrioDasm("header");
        andraVidrio.andraAsignarFuente(() -> andraContenido.getChildren().isEmpty() ? null : andraContenido.getChildren().get(0));

        StackPane andraLogo = new StackPane(AndraComponentes.andraLogo(34));
        VBox andraTextos = new VBox(AndraComponentes.andraLiteral("Andra", "brand-name"), AndraComponentes.andraLiteral("Institución", "brand-sub"));
        andraTextos.setAlignment(Pos.CENTER_LEFT);
        andraLema = AndraComponentes.andraLiteral("tus trámites más simples", "brand-tag");
        andraLema.managedProperty().bind(andraLema.visibleProperty());
        HBox andraMarca = new HBox(andraLogo, andraTextos, andraLema);
        andraMarca.getStyleClass().add("brand");
        AndraComponentes.andraAccesible(andraMarca, "Andra, tus trámites más simples. Ir al inicio");
        andraMarca.setOnMouseClicked(andraEvento -> andraIrA(DixonSesionServicio.dixonRutaInicial(andraSesion.dixonRol())));

        andraBotonIdioma = AndraComponentes.andraIconoRedondo("fth-globe", "Idioma", andraEvento -> andraEmergenteIdioma());
        andraBotonCampana = AndraComponentes.andraIconoRedondo("fth-bell", "Notificaciones", andraEvento -> andraEmergenteNotificaciones());
        andraInsignia = AndraComponentes.andraLiteral("", "dot-badge");
        andraInsignia.setMouseTransparent(true);
        StackPane andraCampana = new StackPane(andraBotonCampana, andraInsignia);
        StackPane.setAlignment(andraInsignia, Pos.TOP_RIGHT);
        StackPane.setMargin(andraInsignia, new Insets(4, 4, 0, 0));
        andraBotonEscuela = AndraComponentes.andraIconoRedondo("fth-home", "Escuela profesional", andraEvento -> andraEmergenteEscuela());
        andraPuntoEscuela = new Circle(4);
        andraPuntoEscuela.getStyleClass().add("fac-dot");
        andraPuntoEscuela.setMouseTransparent(true);
        StackPane andraEscuela = new StackPane(andraBotonEscuela, andraPuntoEscuela);
        StackPane.setAlignment(andraPuntoEscuela, Pos.TOP_RIGHT);
        StackPane.setMargin(andraPuntoEscuela, new Insets(8, 8, 0, 0));
        andraBotonSede = new Button();
        andraBotonSede.getStyleClass().add("ctx-btn");
        andraBotonSede.setGraphic(new FontIcon("fth-map-pin"));
        andraBotonSede.setOnAction(andraEvento -> andraEmergenteSede());
        HBox.setMargin(andraBotonSede, new Insets(0, 4, 0, 4));
        andraBotonUsuario = AndraComponentes.andraIconoRedondo("fth-user", "Mi cuenta", andraEvento -> andraEmergenteUsuario());
        HBox andraAcciones = new HBox(andraBotonIdioma, andraCampana, andraEscuela, andraBotonSede, andraBotonUsuario);
        andraAcciones.getStyleClass().add("header-actions");

        HBox andraFila = new HBox(andraMarca, AndraComponentes.andraEspacio(), andraAcciones);
        andraFila.getStyleClass().add("header-in");
        andraFila.setMaxWidth(1168);
        HBox andraCentrada = new HBox(andraFila);
        andraCentrada.setAlignment(Pos.CENTER);
        HBox.setHgrow(andraFila, Priority.ALWAYS);

        andraNavegacion = new HBox();
        andraNavegacion.getStyleClass().add("nav");
        ScrollPane andraNavegacionDesplazamiento = new ScrollPane(andraNavegacion);
        andraNavegacionDesplazamiento.getStyleClass().add("nav-scroll");
        andraNavegacionDesplazamiento.setFitToHeight(true);
        andraNavegacionDesplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        andraNavegacionDesplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        andraNavegacion.minWidthProperty().bind(andraNavegacionDesplazamiento.widthProperty());
        HBox andraSubnavegacion = new HBox(andraNavegacionDesplazamiento);
        HBox.setHgrow(andraNavegacionDesplazamiento, Priority.ALWAYS);
        andraSubnavegacion.getStyleClass().add("subnav");

        VBox andraCabecera = new VBox(andraCentrada, andraSubnavegacion);
        andraVidrio.getChildren().add(andraCabecera);

        andraBarraSinConexion = new HBox();
        andraBarraSinConexion.getStyleClass().add("off-bar");
        andraBarraSinConexion.setVisible(false);
        andraBarraSinConexion.setManaged(false);
        andraCapaCabecera.getChildren().setAll(andraVidrio, andraBarraSinConexion);
    }

    private void andraConstruirNavegacion() {
        andraSilencioNavegacion = true;
        andraGrupoNavegacion.getToggles().clear();
        andraNavegacion.getChildren().clear();
        andraSilencioNavegacion = false;
        DixonRol andraRol = andraSesion.dixonRol();
        if (andraRol == null) {
            return;
        }
        for (DixonSesionServicio.DixonRuta andraRuta : DixonSesionServicio.dixonRutas(andraRol)) {
            ToggleButton andraBotonOpcion = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcion, andraRuta.dixonEtiqueta());
            andraBotonOpcion.setUserData(andraRuta.dixonClave());
            andraBotonOpcion.setToggleGroup(andraGrupoNavegacion);
            andraBotonOpcion.getStyleClass().add("nav-link");
            andraBotonOpcion.setMnemonicParsing(false);
            andraNavegacion.getChildren().add(andraBotonOpcion);
        }
    }

    public void andraIrA(String andraRuta) {
        andraNotificador.andraCerrarEmergente();
        DixonRol andraRol = andraSesion.dixonRol();
        if (!DixonSesionServicio.dixonPuedeAcceder(andraRol, andraRuta)) {
            andraRuta = DixonSesionServicio.dixonRutaInicial(andraRol);
        }
        String andraDestino = andraRuta;
        Toggle andraOpcion = andraGrupoNavegacion.getToggles().stream().filter(andraPosicionX -> andraDestino.equals(andraPosicionX.getUserData())).findFirst().orElse(null);
        if (andraOpcion == null) {
            andraSilencioNavegacion = true;
            andraGrupoNavegacion.selectToggle(null);
            andraSilencioNavegacion = false;
            andraMostrar(andraDestino);
        } else if (andraGrupoNavegacion.getSelectedToggle() == andraOpcion) {
            andraMostrar(andraDestino);
        } else {
            andraGrupoNavegacion.selectToggle(andraOpcion);
        }
    }

    private void andraNavegar(String andraRuta) {
        switch (andraRuta) {
            case "ctx:sede" -> andraEmergenteSede();
            case "ctx:escuela" -> andraEmergenteEscuela();
            case "contacto" -> {
                andraIrA("ayuda");
                if (andraControladores.get("ayuda") instanceof AndraAyudaControlador andraAyudaControlador) {
                    Platform.runLater(andraAyudaControlador::andraIrACanales);
                }
            }
            case "mapa" -> {
                andraIrA("reservas");
                if (andraControladores.get("reservas") instanceof AndraReservasControlador andraReservasControlador) {
                    andraReservasControlador.andraMostrarMapa();
                }
            }
            case "sesion:entrar" -> andraAlIngresar();
            case "telefono" -> andraContexto.getBean(AndraPieDePagina.class).andraModalTelefono();
            default -> andraIrA(andraRuta);
        }
    }

    private void andraMostrar(String andraRuta) {
        if (andraRutaActual != null && andraControladores.get(andraRutaActual) != null) {
            andraControladores.get(andraRutaActual).andraSalir();
        }
        andraRutaActual = andraRuta;
        Parent andraVista = andraVistas.computeIfAbsent(andraRuta, this::andraCargar);
        andraContenido.getChildren().setAll(andraVista);
        AndraVista andraVista2 = andraControladores.get(andraRuta);
        if (andraVista2 != null) {
            andraVista2.andraRefrescar();
        }
        if (andraVista instanceof ScrollPane andraDesplazable) {
            andraDesplazable.setVvalue(0);
            andraDesplazable.vvalueProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> andraVidrio.andraMarcar());
            if (andraDesplazable.getContent() != null) {
                andraDesplazable.getContent().layoutBoundsProperty().addListener((andraObservable, andraLimitesA, andraLimitesB) -> andraVidrio.andraMarcar());
            }
        }
        andraActualizarBarraSinConexion();
        AndraMovimiento.andraAparecer(andraVista, Duration.millis(220), Duration.ZERO);
        andraActualizarTitulo();
        andraVidrio.andraMarcar();
        Platform.runLater(andraContenido::requestFocus);
    }

    private Parent andraCargar(String andraRuta) {
        FXMLLoader andraCargador = new FXMLLoader(getClass().getResource("/view/" + andraRuta + "-view.fxml"));
        andraCargador.setControllerFactory(andraContexto::getBean);
        andraCargador.setClassLoader(getClass().getClassLoader());
        try {
            Parent andraNodo = andraCargador.load();
            if (andraCargador.getController() instanceof AndraVista andraVista) {
                andraControladores.put(andraRuta, andraVista);
            }
            return andraNodo;
        } catch (IOException andraError) {
            throw new UncheckedIOException("No se pudo cargar la vista " + andraRuta, andraError);
        }
    }

    private void andraActualizarTitulo() {
        if (andraVentana == null) {
            return;
        }
        String andraTitulo;
        if ("login".equals(andraRutaActual)) {
            andraTitulo = "Andra · Ingresar";
        } else if ("inicio".equals(andraRutaActual)) {
            andraTitulo = "Andra";
        } else if ("admin".equals(andraRutaActual)) {
            andraTitulo = "Andra · Panel";
        } else {
            andraTitulo = "Andra · " + DixonSesionServicio.dixonEtiqueta(andraRutaActual);
        }
        andraVentana.titleProperty().unbind();
        andraVentana.titleProperty().bind(DixonIdiomaServicio.dixonEnlace(andraTitulo));
    }

    private void andraRefrescarActual() {
        AndraVista andraVista = andraControladores.get(andraRutaActual);
        if (andraVista != null) {
            andraVista.andraRefrescar();
        }
    }

    private void andraActualizarCabecera() {
        if (!andraSesion.dixonConectado()) {
            return;
        }
        AndraComponentes.andraFijar(andraBotonSede, andraContextoServicio.dixonNombreSede());
        AndraComponentes.andraAccesible(andraBotonSede, "Sede: " + andraContextoServicio.dixonNombreSede() + ". Cambiar sede");
        DixonEscuelaProfesional andraEscuelaProfesional = andraContextoServicio.dixonEscuela();
        andraPuntoEscuela.setVisible(andraEscuelaProfesional != null);
        AndraComponentes.andraAyuda(andraBotonEscuela, andraEscuelaProfesional == null ? "Todas las escuelas" : andraEscuelaProfesional.getDixonNombre());
        AndraComponentes.andraAccesible(andraBotonEscuela, "Escuela profesional: " + (andraEscuelaProfesional == null ? "todas" : andraEscuelaProfesional.getDixonNombre()) + ". Cambiar escuela");
        andraActualizarCampana();
    }

    private void andraActualizarCampana() {
        long andraCantidad = andraNotificaciones.dixonNoLeidas(andraSesion.dixonRol());
        andraInsignia.setText(String.valueOf(andraCantidad));
        andraInsignia.setVisible(andraCantidad > 0);
        AndraComponentes.andraAccesible(andraBotonCampana, "Notificaciones" + (andraCantidad > 0 ? ", " + andraCantidad + " sin leer" : ""));
    }

    @EventListener
    public void andraAlCambiarDatos(DixonCambioDatos andraEvento) {
        if (andraInsignia == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            andraActualizarCampana();
        } else {
            try {
                Platform.runLater(this::andraActualizarCampana);
            } catch (IllegalStateException andraIgnorado) {
                return;
            }
        }
    }

    private void andraAlCambiarConexion(boolean andraAntes, boolean andraAhora) {
        andraActualizarBarraSinConexion();
        if (!andraSesion.dixonConectado()) {
            return;
        }
        if (andraAntes && !andraAhora && !andraConexion.dixonEsSimulado()) {
            andraNotificador.andraInformacion("Te quedaste sin conexión. Andra sigue funcionando con tus datos guardados.");
        }
        if (!andraAntes && andraAhora) {
            andraSincronizar();
        }
    }

    private void andraSincronizar() {
        int andraCantidad = andraConexion.dixonContarPendientes();
        andraActualizarBarraSinConexion();
        if (andraCantidad == 0) {
            return;
        }
        String andraTexto = andraCantidad > 1 ? "s" : "";
        andraNotificador.andraInformacion("Conexión recuperada. Sincronizando " + andraCantidad + " cambio" + andraTexto + "…");
        PauseTransition andraPausa = new PauseTransition(Duration.millis(1200));
        andraPausa.setOnFinished(andraEvento -> {
            andraConexion.dixonSincronizar();
            andraActualizarBarraSinConexion();
            andraNotificador.andraExito("Listo: " + andraCantidad + " cambio" + andraTexto + " sincronizado" + andraTexto + ".");
        });
        andraPausa.play();
    }

    private void andraActualizarBarraSinConexion() {
        boolean andraVisible = andraConexion.dixonSinConexion() && andraSesion.dixonConectado();
        andraBarraSinConexion.setVisible(andraVisible);
        andraBarraSinConexion.setManaged(andraVisible);
        if (andraVisible) {
            int andraCantidad = andraConexion.dixonContarPendientes();
            Label andraTexto = AndraComponentes.andraParrafo("Puedes ver tus documentos, tu carné y tus reservas. Lo que hagas se enviará al volver la conexión.");
            Label andraNegrita = AndraComponentes.andraEtiqueta("Sin conexión.", "off-negrita");
            HBox andraMensaje = new HBox(6, andraNegrita, andraTexto);
            andraMensaje.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(andraMensaje, Priority.ALWAYS);
            andraBarraSinConexion.getChildren().setAll(new FontIcon("fth-wifi-off"), andraMensaje);
            if (andraCantidad > 0) {
                andraBarraSinConexion.getChildren().add(AndraComponentes.andraPildoraPlano(andraCantidad + " cambio" + (andraCantidad > 1 ? "s" : "") + " por sincronizar", "warning"));
            }
            if (andraConexion.dixonEsSimulado()) {
                andraBarraSinConexion.getChildren().add(AndraComponentes.andraBotonSm("Volver a conectarse", "btn-secondary", null,
                        andraEvento -> andraConexion.dixonSimularSinConexion(false)));
            }
        }
        if (!andraContenido.getChildren().isEmpty() && andraContenido.getChildren().get(0) instanceof ScrollPane andraDesplazable
                && andraDesplazable.getContent() != null) {
            andraDesplazable.getContent().getStyleClass().remove("con-barra");
            if (andraVisible) {
                andraDesplazable.getContent().getStyleClass().add("con-barra");
            }
        }
    }

    private VBox andraCaja() {
        VBox andraCaja = new VBox();
        andraCaja.getStyleClass().add("pop");
        return andraCaja;
    }

    private HBox andraCabeceraEmergente(String andraTitulo, Node andraAccion) {
        HBox andraCajaHorizontal = new HBox(AndraComponentes.andraEtiqueta(andraTitulo, "pop-titulo"), AndraComponentes.andraEspacio());
        if (andraAccion != null) {
            andraCajaHorizontal.getChildren().add(andraAccion);
        }
        andraCajaHorizontal.getStyleClass().add("pop-h");
        return andraCajaHorizontal;
    }

    private HBox andraOpcion(String andraTitulo, String andraSubtitulo, boolean andraElegida, boolean andraLiteral, Runnable andraAccion) {
        FontIcon andraVerificacion = AndraComponentes.andraIcono("fth-check", "ck");
        andraVerificacion.setVisible(andraElegida);
        VBox andraTextos = new VBox(1, andraLiteral ? AndraComponentes.andraLiteralParrafo(andraTitulo, "opt-titulo") : AndraComponentes.andraParrafo(andraTitulo, "opt-titulo"));
        if (andraSubtitulo != null) {
            andraTextos.getChildren().add(AndraComponentes.andraParrafo(andraSubtitulo, "opt-sub"));
        }
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        HBox andraFila = new HBox(andraVerificacion, andraTextos);
        HBox.setMargin(andraVerificacion, new Insets(2, 0, 0, 0));
        andraFila.getStyleClass().add("opt");
        if (andraElegida) {
            andraFila.getStyleClass().add("on");
        }
        andraFila.setFocusTraversable(true);
        andraFila.setOnMouseClicked(andraEvento -> andraAccion.run());
        andraFila.setOnKeyPressed(andraEvento -> {
            if (andraEvento.getCode() == KeyCode.ENTER || andraEvento.getCode() == KeyCode.SPACE) {
                andraAccion.run();
            }
        });
        return andraFila;
    }

    private HBox andraOpcionMenu(String andraTexto, boolean andraPeligro, Runnable andraAccion) {
        Label andraEtiqueta = AndraComponentes.andraEtiqueta(andraTexto, "notif-texto");
        if (andraPeligro) {
            andraEtiqueta.getStyleClass().add("peligro");
        }
        HBox andraFila = new HBox(andraEtiqueta);
        andraFila.getStyleClass().addAll("notif", "read");
        andraFila.setAlignment(Pos.CENTER_LEFT);
        andraFila.setFocusTraversable(true);
        Runnable andraEjecutar = () -> {
            andraNotificador.andraCerrarEmergente();
            andraAccion.run();
        };
        andraFila.setOnMouseClicked(andraEvento -> andraEjecutar.run());
        andraFila.setOnKeyPressed(andraEvento -> {
            if (andraEvento.getCode() == KeyCode.ENTER || andraEvento.getCode() == KeyCode.SPACE) {
                andraEjecutar.run();
            }
        });
        return andraFila;
    }

    private Region andraSeparador() {
        Region andraRegion = new Region();
        andraRegion.getStyleClass().add("pop-sep");
        VBox.setMargin(andraRegion, new Insets(6, 0, 6, 0));
        return andraRegion;
    }

    private void andraEmergenteIdioma() {
        VBox andraCajaVertical = andraCaja();
        andraCajaVertical.getChildren().add(andraCabeceraEmergente("Idioma", null));
        for (String[] andraTextos : DixonIdiomaServicio.DIXON_IDIOMAS) {
            andraCajaVertical.getChildren().add(andraOpcion(andraTextos[1], andraTextos[2], andraTextos[0].equals(andraIdioma.dixonObtenerIdioma()), true, () -> andraCambiarIdioma(andraTextos[0])));
        }
        Label andraNota = AndraComponentes.andraParrafo("Traducción de demostración: el quechua y el aimara deben ser revisados por hablantes nativos.", "hint");
        andraNota.setPadding(new Insets(8, 12, 4, 12));
        andraCajaVertical.getChildren().add(andraNota);
        andraNotificador.andraEmergente(andraCajaVertical, andraBotonIdioma);
    }

    public void andraCambiarIdioma(String andraCodigo) {
        andraNotificador.andraCerrarEmergente();
        andraIdioma.dixonAsignarIdioma(andraCodigo);
        andraAjustes.dixonCambiar(andraAjustes2 -> andraAjustes2.setDixonIdioma(andraCodigo));
        String andraAviso = switch (andraCodigo) {
            case "en" -> "Language: English";
            case "qu" -> "Simi: Runasimi";
            case "ay" -> "Aru: Aymar aru";
            default -> "Idioma: Español";
        };
        andraNotificador.andraInformacion(andraAviso);
    }

    private void andraEmergenteNotificaciones() {
        VBox andraCajaVertical = andraCaja();
        DixonRol andraRol = andraSesion.dixonRol();
        Button andraMarcar = AndraComponentes.andraBotonSm("Marcar todo como leído", "btn-tertiary", null, andraEvento -> {
            andraNotificaciones.dixonMarcarTodas(andraRol);
            andraEmergenteNotificaciones();
        });
        andraCajaVertical.getChildren().add(andraCabeceraEmergente("Notificaciones", andraMarcar));
        List<DixonNotificacion> andraLista = andraNotificaciones.dixonListar(andraRol);
        if (andraLista.isEmpty()) {
            Label andraVacio = AndraComponentes.andraEtiqueta("No tienes notificaciones.", "muted", "small");
            andraVacio.setPadding(new Insets(12));
            andraCajaVertical.getChildren().add(andraVacio);
        }
        for (DixonNotificacion andraNotificacion : andraLista) {
            Circle andraPunto = new Circle(4);
            andraPunto.getStyleClass().add("bullet");
            Label andraTexto = AndraComponentes.andraParrafo(andraNotificacion.getDixonTexto(), "notif-texto");
            HBox.setHgrow(andraTexto, Priority.ALWAYS);
            HBox andraFila = new HBox(andraPunto, andraTexto);
            HBox.setMargin(andraPunto, new Insets(7, 0, 0, 0));
            andraFila.getStyleClass().add("notif");
            andraFila.setFocusTraversable(true);
            if (andraNotificacion.isDixonLeida()) {
                andraFila.getStyleClass().add("read");
            }
            Runnable andraAbrir = () -> {
                String andraDestino = andraNotificaciones.dixonAbrir(andraNotificacion.getDixonId());
                andraNotificador.andraCerrarEmergente();
                andraIrA(andraDestino);
            };
            andraFila.setOnMouseClicked(andraEvento -> andraAbrir.run());
            andraFila.setOnKeyPressed(andraEvento -> {
                if (andraEvento.getCode() == KeyCode.ENTER) {
                    andraAbrir.run();
                }
            });
            andraCajaVertical.getChildren().add(andraFila);
        }
        andraNotificador.andraEmergente(andraCajaVertical, andraBotonCampana);
    }

    private void andraEmergenteSede() {
        VBox andraCajaVertical = andraCaja();
        andraCajaVertical.getChildren().add(andraCabeceraEmergente("Sede", null));
        String andraActual = andraContextoServicio.dixonCodigoSede();
        if (andraSesion.dixonEsAdministrador()) {
            andraCajaVertical.getChildren().add(andraOpcion("Todas las sedes", "Vista consolidada", DixonAjustes.DIXON_TODAS.equals(andraActual), false,
                    () -> andraCambiarContexto(andraContextoServicio.dixonCambiarSede(DixonAjustes.DIXON_TODAS))));
        }
        for (DixonSede andraSede : andraCatalogo.dixonSedes()) {
            andraCajaVertical.getChildren().add(andraOpcion(andraSede.getDixonNombreCompleto(), andraSede.getDixonDescripcion(), andraSede.getDixonCodigo().equals(andraActual), false,
                    () -> andraCambiarContexto(andraContextoServicio.dixonCambiarSede(andraSede.getDixonCodigo()))));
        }
        andraNotificador.andraEmergente(andraCajaVertical, andraBotonSede);
    }

    private void andraEmergenteEscuela() {
        DixonEscuelaProfesional andraActual = andraContextoServicio.dixonEscuela();
        VBox andraOpciones = new VBox();
        andraOpciones.getChildren().add(andraOpcion("Todas las escuelas", "Ver todo lo de la sede " + andraContextoServicio.dixonNombreSede(),
                andraActual == null, false, () -> andraCambiarContexto(andraContextoServicio.dixonCambiarEscuela(null))));
        for (DixonSede andraSede : andraCatalogo.dixonSedes()) {
            List<DixonEscuelaProfesional> andraEscuelas = andraCatalogo.dixonEscuelasDe(andraSede);
            if (andraEscuelas.isEmpty()) {
                continue;
            }
            andraOpciones.getChildren().add(AndraComponentes.andraMayus("Sede " + andraSede.getDixonNombre(), "pop-group"));
            for (DixonEscuelaProfesional andraEscuelaProfesional : andraEscuelas) {
                andraOpciones.getChildren().add(andraOpcion(andraEscuelaProfesional.getDixonNombre(), null, andraActual != null && andraActual.getDixonId().equals(andraEscuelaProfesional.getDixonId()), false,
                        () -> andraCambiarContexto(andraContextoServicio.dixonCambiarEscuela(andraEscuelaProfesional.getDixonCodigo()))));
            }
        }
        VBox andraCajaVertical = andraCaja();
        andraCajaVertical.getChildren().addAll(andraCabeceraEmergente("Escuela profesional", null), andraOpciones);
        andraNotificador.andraEmergente(andraCajaVertical, andraBotonEscuela);
    }

    private void andraCambiarContexto(String andraMensaje) {
        andraNotificador.andraCerrarEmergente();
        andraActualizarCabecera();
        andraRefrescarActual();
        andraNotificador.andraExito(andraMensaje);
    }

    private void andraEmergenteUsuario() {
        VBox andraCajaVertical = andraCaja();
        VBox andraDatos = new VBox(2);
        andraDatos.setPadding(new Insets(12, 12, 8, 12));
        boolean andraAdministrador = andraSesion.dixonEsAdministrador();
        if (andraAdministrador) {
            DixonAdministrador andraAdministrador2 = andraSesion.dixonAdministrador();
            andraDatos.getChildren().addAll(AndraComponentes.andraLiteral(andraAdministrador2.getDixonNombre(), "strong", "texto"),
                    AndraComponentes.andraParrafo(andraAdministrador2.getDixonCargo(), "muted", "small"), AndraComponentes.andraLiteral(andraAdministrador2.getDixonCorreo(), "muted", "small"));
        } else {
            DixonPersona andraPersona = andraSesion.dixonEstudiante();
            andraDatos.getChildren().addAll(AndraComponentes.andraLiteral(andraPersona.getDixonNombre(), "strong", "texto"),
                    AndraComponentes.andraEtiqueta("Código " + andraPersona.getDixonCodigo() + " · DNI " + andraPersona.getDixonDni(), "muted", "small"),
                    AndraComponentes.andraParrafo("EP " + andraPersona.getDixonEscuela().getDixonNombre() + " · Sede " + andraPersona.getDixonSede().getDixonNombre(), "muted", "small"));
        }
        Label andraRol = AndraComponentes.andraEtiqueta(andraAdministrador ? "Administrador" : "Estudiante", "user-role");
        VBox.setMargin(andraRol, new Insets(6, 0, 0, 0));
        andraDatos.getChildren().add(andraRol);
        andraCajaVertical.getChildren().addAll(andraDatos, andraSeparador());
        if (andraAdministrador) {
            andraCajaVertical.getChildren().addAll(andraOpcionMenu("Panel", false, () -> andraIrA("admin")),
                    andraOpcionMenu("Transacciones", false, () -> andraIrA("a-pagos")),
                    andraOpcionMenu("Análisis", false, () -> andraIrA("a-datos")));
        } else {
            andraCajaVertical.getChildren().addAll(andraOpcionMenu("Mis solicitudes", false, () -> andraIrA("solicitudes")),
                    andraOpcionMenu("Mi carné digital", false, () -> andraIrA("carne")),
                    andraOpcionMenu("Andra Pay", false, () -> andraIrA("pay")),
                    andraOpcionMenu("Mis favoritos", false, () -> andraIrA("favoritos")));
        }
        boolean andraSimulado = andraConexion.dixonEsSimulado();
        andraCajaVertical.getChildren().add(andraOpcionMenu(andraSimulado ? "Volver a conectarse" : "Probar modo sin conexión", false, () -> {
            andraConexion.dixonSimularSinConexion(!andraSimulado);
            if (!andraSimulado) {
                andraNotificador.andraInformacion("Modo sin conexión activado para la prueba.");
            }
        }));
        andraCajaVertical.getChildren().add(andraOpcionMenu(AndraMovimiento.andraReducido() ? "Reducir movimiento: activado" : "Reducir movimiento: desactivado", false, () -> {
            boolean andraNuevo = !AndraMovimiento.andraReducido();
            AndraMovimiento.andraReducidoPropiedad().set(andraNuevo);
            andraAjustes.dixonCambiar(andraAjustes2 -> andraAjustes2.setDixonReducirMovimiento(andraNuevo));
            andraNotificador.andraInformacion(andraNuevo ? "Reducir movimiento activado." : "Reducir movimiento desactivado.");
        }));
        andraCajaVertical.getChildren().add(andraOpcionMenu("Restablecer datos de demostración", false, this::andraRestablecerDemo));
        andraCajaVertical.getChildren().add(andraOpcionMenu("Cerrar sesión", true, this::andraCerrarSesion));
        andraNotificador.andraEmergente(andraCajaVertical, andraBotonUsuario);
    }

    private void andraRestablecerDemo() {
        andraNotificador.andraConfirmar("¿Restablecer datos de demostración?",
                "Se borrarán las reservas, solicitudes y cambios que hiciste en esta aplicación.", "Restablecer", true, () -> {
                    andraDemo.dixonRestablecer();
                    andraIdioma.dixonAsignarIdioma(andraAjustes.dixonObtener().getDixonIdioma());
                    andraActualizarCabecera();
                    andraRefrescarActual();
                    andraNotificador.andraExito("Datos restablecidos.");
                });
    }

    private void andraCerrarSesion() {
        andraNotificador.andraConfirmar("¿Cerrar sesión?", "Volverás a la pantalla de ingreso. Tus datos de demostración se mantienen.",
                "Cerrar sesión", false, () -> {
                    andraSesion.dixonCerrar();
                    andraArrancarSesion();
                });
    }
}
