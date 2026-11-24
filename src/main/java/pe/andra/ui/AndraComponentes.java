package pe.andra.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.service.AndraIdiomaServicio;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AndraComponentes {

    private static final Map<String, String> ANDRA_TONOS = Map.ofEntries(
            Map.entry("Aprobado", "success"), Map.entry("Disponible", "success"), Map.entry("Pagado", "success"),
            Map.entry("Listo", "success"), Map.entry("Confirmado", "success"), Map.entry("Acceso concedido", "success"),
            Map.entry("Devuelto", "neutral"), Map.entry("Anulado", "neutral"), Map.entry("Reembolsado", "neutral"),
            Map.entry("Entregado", "neutral"), Map.entry("Venció", "neutral"), Map.entry("Cancelado", "neutral"),
            Map.entry("Pendiente", "warning"), Map.entry("Por pagar", "warning"), Map.entry("En revisión", "warning"),
            Map.entry("Rechazado", "error"), Map.entry("Observado", "error"), Map.entry("Ocupado", "error"),
            Map.entry("Vencido", "error"), Map.entry("En uso", "info"), Map.entry("Tu sede", "info"));

    private static Image andraLogoImagen;

    private AndraComponentes() {
    }

    public static void andraFijar(Labeled andraRotulo, String andraTexto) {
        andraRotulo.textProperty().unbind();
        andraRotulo.textProperty().bind(AndraIdiomaServicio.andraEnlace(andraTexto == null ? "" : andraTexto));
    }

    public static void andraIndicacion(TextInputControl andraCampo, String andraTexto) {
        andraCampo.promptTextProperty().unbind();
        andraCampo.promptTextProperty().bind(AndraIdiomaServicio.andraEnlace(andraTexto));
    }

    public static void andraAccesible(Node andraNodo, String andraTexto) {
        andraNodo.accessibleTextProperty().unbind();
        andraNodo.accessibleTextProperty().bind(AndraIdiomaServicio.andraEnlace(andraTexto));
    }

    public static void andraAyuda(Control andraControl, String andraTexto) {
        Tooltip andraAyuda = new Tooltip();
        andraAyuda.textProperty().bind(AndraIdiomaServicio.andraEnlace(andraTexto));
        andraControl.setTooltip(andraAyuda);
    }

    public static Label andraEtiqueta(String andraTexto, String... andraClases) {
        Label andraEtiqueta = new Label();
        andraFijar(andraEtiqueta, andraTexto);
        andraEtiqueta.getStyleClass().addAll(andraClases);
        andraEtiqueta.setMnemonicParsing(false);
        return andraEtiqueta;
    }

    public static Label andraMayus(String andraTexto, String... andraClases) {
        Label andraEtiqueta = new Label();
        andraEtiqueta.textProperty().bind(AndraIdiomaServicio.andraEnlaceMayus(andraTexto));
        andraEtiqueta.getStyleClass().addAll(andraClases);
        andraEtiqueta.setMnemonicParsing(false);
        return andraEtiqueta;
    }

    public static Label andraParrafo(String andraTexto, String... andraClases) {
        Label andraEtiqueta = andraEtiqueta(andraTexto, andraClases);
        andraEtiqueta.setWrapText(true);
        andraEtiqueta.setMinHeight(Region.USE_PREF_SIZE);
        return andraEtiqueta;
    }

    public static Label andraLiteral(String andraTexto, String... andraClases) {
        Label andraEtiqueta = new Label(andraTexto);
        andraEtiqueta.getStyleClass().addAll(andraClases);
        andraEtiqueta.setMnemonicParsing(false);
        return andraEtiqueta;
    }

    public static Label andraLiteralParrafo(String andraTexto, String... andraClases) {
        Label andraEtiqueta = andraLiteral(andraTexto, andraClases);
        andraEtiqueta.setWrapText(true);
        andraEtiqueta.setMinHeight(Region.USE_PREF_SIZE);
        return andraEtiqueta;
    }

    public static Text andraTexto(String andraContenido, String... andraClases) {
        Text andraTexto = new Text();
        andraTexto.textProperty().bind(AndraIdiomaServicio.andraEnlace(andraContenido));
        andraTexto.getStyleClass().addAll(andraClases);
        return andraTexto;
    }

    public static TextFlow andraFlujo(Node... andraPartes) {
        TextFlow andraFlujoTexto = new TextFlow(andraPartes);
        andraFlujoTexto.setMinHeight(Region.USE_PREF_SIZE);
        return andraFlujoTexto;
    }

    public static Button andraBoton(String andraTexto, String andraEstilo, String andraIcono, EventHandler<ActionEvent> andraAccion) {
        Button andraBoton = new Button();
        if (andraTexto != null) {
            andraFijar(andraBoton, andraTexto);
        }
        if (andraIcono != null) {
            andraBoton.setGraphic(new FontIcon(andraIcono));
        }
        andraBoton.getStyleClass().addAll("btn", andraEstilo);
        andraBoton.setMnemonicParsing(false);
        andraBoton.setMinWidth(Region.USE_PREF_SIZE);
        if (andraAccion != null) {
            andraBoton.setOnAction(andraAccion);
        }
        return andraBoton;
    }

    public static Button andraBotonSm(String andraTexto, String andraEstilo, String andraIcono, EventHandler<ActionEvent> andraAccion) {
        Button andraBoton = andraBoton(andraTexto, andraEstilo, andraIcono, andraAccion);
        andraBoton.getStyleClass().add("btn-sm");
        return andraBoton;
    }

    public static Button andraBloque(Button andraBoton) {
        andraBoton.setMaxWidth(Double.MAX_VALUE);
        andraBoton.setMinWidth(0);
        return andraBoton;
    }

    public static Button andraBotonIcono(String andraIcono, String andraDescripcion, EventHandler<ActionEvent> andraAccion) {
        Button andraBoton = andraBoton(null, "btn-secondary", andraIcono, andraAccion);
        andraBoton.getStyleClass().addAll("btn-sm", "btn-icon");
        andraAccesible(andraBoton, andraDescripcion);
        andraAyuda(andraBoton, andraDescripcion);
        return andraBoton;
    }

    public static Button andraIconoRedondo(String andraIcono, String andraDescripcion, EventHandler<ActionEvent> andraAccion) {
        Button andraBoton = new Button(null, new FontIcon(andraIcono));
        andraBoton.getStyleClass().add("icon-btn");
        andraAccesible(andraBoton, andraDescripcion);
        if (andraAccion != null) {
            andraBoton.setOnAction(andraAccion);
        }
        return andraBoton;
    }

    public static Button andraEnlaceFlecha(String andraTexto, EventHandler<ActionEvent> andraAccion) {
        Button andraBoton = new Button(null, new FontIcon("fth-arrow-right"));
        andraFijar(andraBoton, andraTexto);
        andraBoton.getStyleClass().add("link-arrow");
        andraBoton.setMnemonicParsing(false);
        andraBoton.setOnAction(andraAccion);
        return andraBoton;
    }

    public static Button andraEstrella(boolean andraMarcada, String andraNombre, Runnable andraAccion) {
        Button andraBoton = new Button(null, new FontIcon("fth-star"));
        andraBoton.getStyleClass().add("star-btn");
        if (andraMarcada) {
            andraBoton.getStyleClass().add("on");
        }
        andraAccesible(andraBoton, (andraMarcada ? "Quitar de" : "Agregar a") + " favoritos: " + andraNombre);
        andraBoton.setOnAction(andraEvento -> {
            andraEvento.consume();
            andraAccion.run();
        });
        return andraBoton;
    }

    public static FontIcon andraIcono(String andraLiteral, String... andraClases) {
        FontIcon andraIcono = new FontIcon(andraLiteral);
        andraIcono.getStyleClass().addAll(andraClases);
        return andraIcono;
    }

    public static Label andraPildora(String andraTexto, String andraTono) {
        Circle andraPunto = new Circle(3);
        andraPunto.getStyleClass().add("punto");
        Label andraEtiqueta = andraEtiqueta(andraTexto, "pill", andraTono == null ? "neutral" : andraTono);
        andraEtiqueta.setGraphic(andraPunto);
        andraEtiqueta.setMinWidth(Region.USE_PREF_SIZE);
        return andraEtiqueta;
    }

    public static Label andraPildora(String andraTexto) {
        return andraPildora(andraTexto, andraTono(andraTexto));
    }

    public static String andraTono(String andraEstado) {
        return ANDRA_TONOS.getOrDefault(andraEstado, "neutral");
    }

    public static Label andraPildoraPlano(String andraTexto, String andraTono) {
        Label andraEtiqueta = andraEtiqueta(andraTexto, "pill", andraTono);
        andraEtiqueta.setMinWidth(Region.USE_PREF_SIZE);
        return andraEtiqueta;
    }

    public static Label andraDistintivo(String andraTexto) {
        Label andraEtiqueta = andraEtiqueta(andraTexto, "tag");
        andraEtiqueta.setMinWidth(Region.USE_PREF_SIZE);
        return andraEtiqueta;
    }

    public static Label andraConteo(long andraCantidad) {
        Label andraEtiqueta = andraLiteral(String.valueOf(andraCantidad), "count");
        andraEtiqueta.setMinWidth(Region.USE_PREF_SIZE);
        return andraEtiqueta;
    }

    public static VBox andraVacio(String andraIcono, String andraTitulo, String andraSub, Node... andraExtra) {
        VBox andraCaja = new VBox(andraIcono(andraIcono), andraEtiqueta(andraTitulo, "empty-titulo"), andraParrafo(andraSub, "empty-sub"));
        if (andraExtra.length > 0) {
            HBox andraAcciones = new HBox(8, andraExtra);
            andraAcciones.setAlignment(Pos.CENTER);
            andraAcciones.setPadding(new Insets(16, 0, 0, 0));
            andraCaja.getChildren().add(andraAcciones);
        }
        andraCaja.getStyleClass().add("empty");
        andraCaja.setMaxWidth(Double.MAX_VALUE);
        return andraCaja;
    }

    public static HBox andraMetaLinea(String andraIcono, String andraTexto) {
        Label andraEtiqueta = andraParrafo(andraTexto);
        HBox andraFila = new HBox(andraIcono(andraIcono), andraEtiqueta);
        andraFila.getStyleClass().add("meta-line");
        return andraFila;
    }

    public static VBox andraPortada(String andraAntetitulo, String andraTitulo, String andraSubtitulo, Node andraLineaContexto) {
        VBox andraPortada = new VBox();
        andraPortada.getStyleClass().add("page-hero");
        if (andraAntetitulo != null) {
            Label andraAntetitulo2 = andraParrafo(andraAntetitulo, "eyebrow");
            andraAntetitulo2.setAlignment(Pos.CENTER);
            andraPortada.getChildren().add(andraAntetitulo2);
        }
        Label andraTituloPrincipal = andraParrafo(andraTitulo, "hero-titulo");
        andraTituloPrincipal.setAlignment(Pos.CENTER);
        andraPortada.getChildren().add(andraTituloPrincipal);
        if (andraSubtitulo != null) {
            Label andraEtiqueta = andraParrafo(andraSubtitulo, "hero-sub");
            andraEtiqueta.setAlignment(Pos.CENTER);
            andraPortada.getChildren().add(andraEtiqueta);
        }
        if (andraLineaContexto != null) {
            andraPortada.getChildren().add(andraLineaContexto);
        }
        return andraPortada;
    }

    public static VBox andraPortadaAdministrador(String andraTitulo, String andraSubtitulo, Node andraLineaContexto) {
        VBox andraAlto = andraPortada(null, andraTitulo, andraSubtitulo, andraLineaContexto);
        Label andraAntetitulo = andraEtiqueta("Panel administrativo", "eyebrow");
        andraAntetitulo.setGraphic(andraIcono("fth-shield"));
        andraAlto.getChildren().add(0, andraAntetitulo);
        return andraAlto;
    }

    public static HBox andraLineaContexto(String andraSede, String andraEscuela, String andraRutaCambiar) {
        HBox andraLinea = new HBox();
        andraLinea.getStyleClass().add("ctx-line");
        if (andraSede != null) {
            Label andraEtiqueta = andraEtiqueta(andraSede);
            andraEtiqueta.setGraphic(new FontIcon("fth-map-pin"));
            andraLinea.getChildren().add(andraEtiqueta);
        }
        Label andraEtiqueta2 = andraEtiqueta(andraEscuela);
        andraEtiqueta2.setGraphic(new FontIcon("fth-book-open"));
        andraLinea.getChildren().add(andraEtiqueta2);
        Button andraCambiar = andraBotonSm("Cambiar", "btn-tertiary", null, null);
        andraCambiar.setOnAction(andraEvento -> AndraNavegacion.andraIr(andraCambiar, andraRutaCambiar));
        andraLinea.getChildren().add(andraCambiar);
        return andraLinea;
    }

    public static HBox andraCabeceraSeccion(String andraTitulo, String andraSubtitulo, Node... andraAcciones) {
        VBox andraTextos = new VBox(andraParrafo(andraTitulo, "h2"));
        if (andraSubtitulo != null) {
            andraTextos.getChildren().add(andraParrafo(andraSubtitulo, "sub"));
        }
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        andraTextos.setMaxWidth(Double.MAX_VALUE);
        HBox andraCabecera = new HBox(andraTextos);
        if (andraAcciones.length > 0) {
            HBox andraAcc = new HBox(8, andraAcciones);
            andraAcc.setAlignment(Pos.BOTTOM_RIGHT);
            andraAcc.setMinWidth(Region.USE_PREF_SIZE);
            andraCabecera.getChildren().add(andraAcc);
        }
        andraCabecera.getStyleClass().add("section-h");
        return andraCabecera;
    }

    public static VBox andraSeccion(Node... andraHijos) {
        VBox andraSeccion = new VBox(andraHijos);
        andraSeccion.getStyleClass().add("section");
        andraSeccion.setMaxWidth(1168);
        return andraSeccion;
    }

    public static VBox andraLista(List<? extends Node> andraFilas) {
        VBox andraLista = new VBox();
        andraLista.getStyleClass().add("list");
        andraLista.getChildren().setAll(andraFilas);
        andraMarcarUltima(andraLista);
        return andraLista;
    }

    public static void andraMarcarUltima(Pane andraContenedor) {
        andraContenedor.getChildren().forEach(andraNodo -> andraNodo.getStyleClass().remove("ultima"));
        if (!andraContenedor.getChildren().isEmpty()) {
            andraContenedor.getChildren().get(andraContenedor.getChildren().size() - 1).getStyleClass().add("ultima");
        }
    }

    public static HBox andraFila(Node andraPrincipal, Node... andraAcciones) {
        HBox.setHgrow(andraPrincipal, Priority.ALWAYS);
        if (andraPrincipal instanceof Region andraRegion) {
            andraRegion.setMaxWidth(Double.MAX_VALUE);
        }
        HBox andraAcc = new HBox(andraAcciones);
        andraAcc.getStyleClass().add("row-actions");
        andraAcc.setMinWidth(Region.USE_PREF_SIZE);
        HBox andraCajaHorizontal = new HBox(andraPrincipal, andraAcc);
        andraCajaHorizontal.getStyleClass().add("row");
        return andraCajaHorizontal;
    }

    public static VBox andraTextosFila(String andraTitulo, String andraSub) {
        VBox andraCajaVertical = new VBox(2, andraParrafo(andraTitulo, "row-title"));
        if (andraSub != null) {
            andraCajaVertical.getChildren().add(andraParrafo(andraSub, "row-sub"));
        }
        return andraCajaVertical;
    }

    public static GridPane andraKv(Object... andraPares) {
        GridPane andraRejilla = new GridPane();
        andraRejilla.getStyleClass().add("kv");
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(38);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(62);
        andraRestriccionColumna2.setHalignment(HPos.LEFT);
        andraRejilla.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        int andraFila = 0;
        for (int andraIndice = 0; andraIndice + 1 < andraPares.length; andraIndice += 2) {
            if (andraPares[andraIndice] == null) {
                continue;
            }
            Label andraTermino = andraParrafo(String.valueOf(andraPares[andraIndice]), "dt");
            Node andraDefinicion = andraPares[andraIndice + 1] instanceof Node andraNodo ? andraNodo : andraParrafo(String.valueOf(andraPares[andraIndice + 1]), "dd");
            andraRejilla.addRow(andraFila++, andraTermino, andraDefinicion);
        }
        return andraRejilla;
    }

    public static Image andraImagenLogo() {
        if (andraLogoImagen == null) {
            andraLogoImagen = new Image(Objects.requireNonNull(AndraComponentes.class.getResourceAsStream("/img/logo-andra.png")));
        }
        return andraLogoImagen;
    }

    public static ImageView andraLogo(double andraTamanio) {
        ImageView andraVista = new ImageView(andraImagenLogo());
        andraVista.setFitWidth(andraTamanio);
        andraVista.setFitHeight(andraTamanio);
        andraVista.setSmooth(true);
        Rectangle andraRecorte = new Rectangle(andraTamanio, andraTamanio);
        andraRecorte.setArcWidth(andraTamanio * 0.5);
        andraRecorte.setArcHeight(andraTamanio * 0.5);
        andraVista.setClip(andraRecorte);
        return andraVista;
    }

    public static void andraDesplazarHasta(ScrollPane andraPagina, Node andraDestino) {
        if (andraPagina == null || andraDestino == null || andraDestino.getScene() == null) {
            return;
        }
        Node andraContenido = andraPagina.getContent();
        double andraPosicionY = andraDestino.localToScene(0, 0).getY() - andraContenido.localToScene(0, 0).getY();
        double andraMaximo = andraContenido.getBoundsInLocal().getHeight() - andraPagina.getViewportBounds().getHeight();
        if (andraMaximo <= 0) {
            return;
        }
        double andraObjetivo = Math.max(0, Math.min(1, (andraPosicionY - 120) / andraMaximo));
        if (AndraMovimiento.andraReducido()) {
            andraPagina.setVvalue(andraObjetivo);
            return;
        }
        new Timeline(new KeyFrame(Duration.millis(320), new KeyValue(andraPagina.vvalueProperty(), andraObjetivo,
                Interpolator.SPLINE(0.25, 0.1, 0.25, 1)))).play();
    }

    public static Region andraEspacio() {
        Region andraRegion = new Region();
        HBox.setHgrow(andraRegion, Priority.ALWAYS);
        return andraRegion;
    }

    public static Region andraEspacioV() {
        Region andraRegion = new Region();
        VBox.setVgrow(andraRegion, Priority.ALWAYS);
        return andraRegion;
    }

    public static HBox andraVineta(String andraTexto, String... andraClases) {
        Label andraPunto = andraLiteral("•", andraClases);
        andraPunto.setMinWidth(Region.USE_PREF_SIZE);
        Label andraEtiqueta = andraParrafo(andraTexto, andraClases);
        HBox.setHgrow(andraEtiqueta, Priority.ALWAYS);
        HBox andraAlto = new HBox(8, andraPunto, andraEtiqueta);
        andraAlto.setAlignment(Pos.TOP_LEFT);
        return andraAlto;
    }

    public static FlowPane andraFichas(Node... andraHijos) {
        FlowPane andraPanelFlujo = new FlowPane(andraHijos);
        andraPanelFlujo.getStyleClass().add("chips");
        return andraPanelFlujo;
    }

    public static VBox andraIndicador(String andraEtiqueta, String andraValor, String andraTono, String andraAyuda) {
        Label andraEtiqueta2 = andraLiteral(andraValor, "kpi-valor");
        if (andraTono != null && !andraTono.isBlank()) {
            andraEtiqueta2.getStyleClass().add(andraTono);
        }
        VBox andraCajaVertical = new VBox(andraMayus(andraEtiqueta, "label-caps"), andraEtiqueta2, andraParrafo(andraAyuda, "hint"));
        andraCajaVertical.getStyleClass().add("kpi");
        return andraCajaVertical;
    }
}
