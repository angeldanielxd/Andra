package pe.andra.ui;

import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AndraNotificador {

    public static final double ANDRA_ANCHO_MODAL = 560;
    public static final double ANDRA_ANCHO_MODAL_AMPLIO = 720;
    private static final Duration ANDRA_DURACION_AVISO = Duration.millis(3800);

    private StackPane andraRaiz;
    private Node andraFondo;
    private Pane andraCapaFlotante;
    private StackPane andraCapaModal;
    private VBox andraZonaAvisos;
    private Node andraFocoPrevio;
    private VBox andraModalActual;
    private Runnable andraAlCerrarModal;
    private Region andraEmergenteActual;
    private Region andraAtrapaClics;

    public void andraRegistrar(StackPane andraRaiz, Node andraFondo, Pane andraCapaFlotante, StackPane andraCapaModal, VBox andraZonaAvisos) {
        this.andraRaiz = andraRaiz;
        this.andraFondo = andraFondo;
        this.andraCapaFlotante = andraCapaFlotante;
        this.andraCapaModal = andraCapaModal;
        this.andraZonaAvisos = andraZonaAvisos;
        andraCapaModal.setVisible(false);
        andraCapaModal.addEventFilter(KeyEvent.KEY_PRESSED, andraEvento -> {
            if (andraEvento.getCode() == KeyCode.TAB && andraModalActual != null) {
                andraCiclarFoco(andraEvento.isShiftDown());
                andraEvento.consume();
            }
        });
    }

    public Window andraVentana() {
        return andraRaiz == null || andraRaiz.getScene() == null ? null : andraRaiz.getScene().getWindow();
    }

    public void andraExito(String andraMensaje) {
        andraAvisoFlotante(andraMensaje, "fth-check", "success");
    }

    public void andraError(String andraMensaje) {
        andraAvisoFlotante(andraMensaje, "fth-alert-triangle", "error");
    }

    public void andraInformacion(String andraMensaje) {
        andraAvisoFlotante(andraMensaje, "fth-info", "info");
    }

    private void andraAvisoFlotante(String andraMensaje, String andraIcono, String andraTono) {
        if (andraZonaAvisos == null) {
            return;
        }
        Label andraTexto = AndraComponentes.andraParrafo(andraMensaje);
        HBox.setHgrow(andraTexto, Priority.ALWAYS);
        HBox andraAvisoFlotante = new HBox(new FontIcon(andraIcono), andraTexto);
        andraAvisoFlotante.getStyleClass().addAll("toast", andraTono);
        andraAvisoFlotante.setMaxHeight(Region.USE_PREF_SIZE);
        andraZonaAvisos.getChildren().add(andraAvisoFlotante);
        AndraMovimiento.andraSubir(andraAvisoFlotante);
        PauseTransition andraEspera = new PauseTransition(ANDRA_DURACION_AVISO);
        andraEspera.setOnFinished(andraEvento -> AndraMovimiento.andraDesvanecer(andraAvisoFlotante, () -> andraZonaAvisos.getChildren().remove(andraAvisoFlotante)));
        andraEspera.play();
    }

    public VBox andraModal(String andraTitulo, Node andraCuerpo, boolean andraAmplio, Node... andraPie) {
        return andraModal(andraTitulo, andraCuerpo, andraAmplio, null, andraPie);
    }

    public VBox andraModal(String andraTitulo, Node andraCuerpo, boolean andraAmplio, Runnable andraAlCerrar, Node... andraPie) {
        andraCerrarEmergente();
        if (andraModalActual == null) {
            andraFocoPrevio = andraRaiz.getScene() == null ? null : andraRaiz.getScene().getFocusOwner();
        } else if (this.andraAlCerrarModal != null) {
            Runnable andraPrevio = this.andraAlCerrarModal;
            this.andraAlCerrarModal = null;
            andraPrevio.run();
        }
        this.andraAlCerrarModal = andraAlCerrar;
        Label andraEtiquetaTitulo = AndraComponentes.andraParrafo(andraTitulo, "modal-titulo");
        HBox.setHgrow(andraEtiquetaTitulo, Priority.ALWAYS);
        andraEtiquetaTitulo.setMaxWidth(Double.MAX_VALUE);
        Button andraCerrar = AndraComponentes.andraIconoRedondo("fth-x", "Cerrar", andraEvento -> andraCerrarModal());
        HBox andraCabecera = new HBox(andraEtiquetaTitulo, andraCerrar);
        andraCabecera.getStyleClass().add("modal-h");

        VBox andraCuerpoCaja = new VBox(andraCuerpo);
        andraCuerpoCaja.getStyleClass().add("modal-b");
        ScrollPane andraDesplazable = new ScrollPane(andraCuerpoCaja);
        andraDesplazable.setFitToWidth(true);
        andraDesplazable.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        andraDesplazable.getStyleClass().add("modal-scroll");
        VBox.setVgrow(andraDesplazable, Priority.ALWAYS);

        VBox andraModal = new VBox(andraCabecera, andraDesplazable);
        if (andraPie.length > 0) {
            HBox andraPiePagina = new HBox(andraPie);
            andraPiePagina.getStyleClass().add("modal-f");
            andraModal.getChildren().add(andraPiePagina);
        }
        andraModal.getStyleClass().add("modal");
        double andraAncho = andraAmplio ? ANDRA_ANCHO_MODAL_AMPLIO : ANDRA_ANCHO_MODAL;
        andraModal.setPrefWidth(andraAncho);
        andraModal.setMaxWidth(andraAncho);
        andraModal.maxWidthProperty().bind(Bindings.min(andraAncho, andraCapaModal.widthProperty().subtract(32)));
        andraModal.setMaxHeight(Region.USE_PREF_SIZE);
        andraDesplazable.prefViewportHeightProperty().bind(Bindings.min(andraCuerpoCaja.heightProperty(),
                andraCapaModal.heightProperty().subtract(190)));

        Region andraVelo = new Region();
        andraVelo.getStyleClass().add("velo");
        andraVelo.setOnMouseClicked(andraEvento -> andraCerrarModal());
        andraCapaModal.getChildren().setAll(andraVelo, andraModal);
        StackPane.setAlignment(andraModal, Pos.CENTER);
        andraCapaModal.setVisible(true);
        andraFondo.setEffect(new GaussianBlur(8));
        andraFondo.setMouseTransparent(true);
        andraModalActual = andraModal;
        AndraMovimiento.andraSubir(andraModal);
        javafx.application.Platform.runLater(() -> {
            Node andraPrimero = andraEnfocables(andraModal).stream().filter(andraNodo -> andraNodo != andraCerrar).findFirst().orElse(andraCerrar);
            andraPrimero.requestFocus();
        });
        return andraModal;
    }

    public boolean andraHayModal() {
        return andraModalActual != null;
    }

    public void andraCerrarModal() {
        if (andraModalActual == null) {
            return;
        }
        andraModalActual = null;
        andraCapaModal.getChildren().clear();
        andraCapaModal.setVisible(false);
        andraFondo.setEffect(null);
        andraFondo.setMouseTransparent(false);
        Runnable andraAccion = andraAlCerrarModal;
        andraAlCerrarModal = null;
        if (andraAccion != null) {
            andraAccion.run();
        }
        if (andraFocoPrevio != null && andraFocoPrevio.getScene() != null) {
            andraFocoPrevio.requestFocus();
        }
    }

    public void andraConfirmar(String andraTitulo, String andraTexto, String andraAceptar, boolean andraPeligro, Runnable andraAlAceptar) {
        Label andraMensaje = AndraComponentes.andraParrafo(andraTexto, "muted");
        Button andraVolver = AndraComponentes.andraBoton("Volver", "btn-secondary", null, andraEvento -> andraCerrarModal());
        Button andraCorrecto = AndraComponentes.andraBoton(andraAceptar, "btn-primary", null, null);
        andraCorrecto.setOnAction(andraEvento -> {
            andraCerrarModal();
            andraAlAceptar.run();
        });
        if (andraPeligro) {
            andraCorrecto.getStyleClass().add("peligro");
        }
        andraModal(andraTitulo, andraMensaje, false, andraVolver, andraCorrecto);
        javafx.application.Platform.runLater(andraCorrecto::requestFocus);
    }

    public void andraAviso(String andraTitulo, Node andraCuerpo, String andraTextoBoton) {
        andraModal(andraTitulo, andraCuerpo, false, AndraComponentes.andraBoton(andraTextoBoton, "btn-primary", null, andraEvento -> andraCerrarModal()));
    }

    public void andraEmergente(Region andraContenido, Node andraAncla) {
        andraCerrarEmergente();
        if (andraAncla == null || andraAncla.getScene() == null) {
            return;
        }
        Bounds andraLimites = andraCapaFlotante.sceneToLocal(andraAncla.localToScene(andraAncla.getBoundsInLocal()));
        andraAtrapaClics = new Region();
        andraAtrapaClics.setPrefSize(andraCapaFlotante.getWidth(), andraCapaFlotante.getHeight());
        andraAtrapaClics.resize(andraCapaFlotante.getWidth(), andraCapaFlotante.getHeight());
        andraAtrapaClics.setOnMousePressed(andraEvento -> {
            andraCerrarEmergente();
            andraEvento.consume();
        });
        ScrollPane andraEnvoltura = new ScrollPane(andraContenido);
        andraEnvoltura.setFitToWidth(true);
        andraEnvoltura.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        andraEnvoltura.getStyleClass().add("pop-scroll");
        AndraVidrioDasm andraVidrio = new AndraVidrioDasm("pop-vidrio", false);
        andraVidrio.andraAsignarRadio(16);
        andraVidrio.andraAsignarFuente(() -> andraFondo);
        StackPane andraMarco = new StackPane(andraVidrio, andraEnvoltura);
        andraMarco.getStyleClass().add("pop-marco");
        andraMarco.setPickOnBounds(true);
        andraMarco.translateYProperty().addListener((andraObservable, andraAntes, andraAhora) -> andraVidrio.andraMarcar());
        andraCapaFlotante.getChildren().setAll(andraAtrapaClics, andraMarco);
        andraMarco.applyCss();
        double andraAlto = andraCapaFlotante.getHeight();
        double andraMaximoAlto = Math.max(160, Math.min(680, andraAlto - andraLimites.getMaxY() - 24));
        double andraAncho = Math.min(andraContenido.prefWidth(-1), andraCapaFlotante.getWidth() - 32);
        double andraAltoContenido = andraContenido.prefHeight(andraAncho);
        andraEnvoltura.setPrefViewportWidth(andraAncho);
        boolean andraCabe = andraAltoContenido <= andraMaximoAlto;
        andraEnvoltura.setVbarPolicy(andraCabe ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
        andraEnvoltura.setFitToHeight(andraCabe);
        andraEnvoltura.setPrefViewportHeight(Math.ceil(Math.min(andraMaximoAlto, andraAltoContenido)));
        double andraPosicionX = Math.max(16, Math.min(andraLimites.getMaxX() - andraAncho, andraCapaFlotante.getWidth() - andraAncho - 16));
        double andraPosicionY = andraLimites.getMaxY() + 8;
        andraMarco.autosize();
        andraMarco.relocate(andraPosicionX, andraPosicionY);
        andraEmergenteActual = andraMarco;
        AndraMovimiento.andraSubir(andraMarco);
        javafx.application.Platform.runLater(() -> andraEnfocables(andraContenido).stream().findFirst().ifPresent(Node::requestFocus));
    }

    public boolean andraHayEmergente() {
        return andraEmergenteActual != null;
    }

    public void andraCerrarEmergente() {
        if (andraCapaFlotante != null) {
            andraCapaFlotante.getChildren().clear();
        }
        andraEmergenteActual = null;
        andraAtrapaClics = null;
    }

    public boolean andraCerrarArriba() {
        if (andraHayEmergente()) {
            andraCerrarEmergente();
            return true;
        }
        if (andraHayModal()) {
            andraCerrarModal();
            return true;
        }
        return false;
    }

    private void andraCiclarFoco(boolean andraAtras) {
        List<Node> andraNodos = andraEnfocables(andraModalActual);
        if (andraNodos.isEmpty()) {
            return;
        }
        Node andraActual = andraRaiz.getScene().getFocusOwner();
        int andraIndice = andraNodos.indexOf(andraActual);
        int andraSiguiente = andraIndice < 0 ? 0 : Math.floorMod(andraIndice + (andraAtras ? -1 : 1), andraNodos.size());
        andraNodos.get(andraSiguiente).requestFocus();
    }

    private static List<Node> andraEnfocables(Node andraRaizBusqueda) {
        List<Node> andraLista = new ArrayList<>();
        andraRecolectar(andraRaizBusqueda, andraLista);
        return andraLista;
    }

    private static void andraRecolectar(Node andraNodo, List<Node> andraLista) {
        if (andraNodo == null || !andraNodo.isVisible() || andraNodo.isDisabled()) {
            return;
        }
        if (andraNodo instanceof Control && andraNodo.isFocusTraversable() && !(andraNodo instanceof ScrollPane)) {
            andraLista.add(andraNodo);
        }
        if (andraNodo instanceof Parent andraPadre) {
            for (Node andraAlto : andraPadre.getChildrenUnmodifiable()) {
                andraRecolectar(andraAlto, andraLista);
            }
        }
    }
}
