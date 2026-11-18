package pe.andra.ui;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class AndraTarjetaGaleria extends VBox {

    private final Label andraAccion;
    private final Node andraObjetivoGiro;
    private RotateTransition andraGiro;

    private static final class AndraArte extends StackPane {
        private final boolean andraDestacado;

        AndraArte(boolean andraDestacado, Node... andraHijos) {
            super(andraHijos);
            this.andraDestacado = andraDestacado;
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected double computePrefHeight(double andraAncho) {
            double andraAncho2 = andraAncho < 0 ? 250 : andraAncho;
            return andraDestacado ? Math.max(260, andraAncho2 * 0.5) : andraAncho2 * 0.625;
        }

        @Override
        protected double computeMinHeight(double andraAncho) {
            return andraDestacado ? 260 : computePrefHeight(andraAncho);
        }
    }

    public AndraTarjetaGaleria(Node andraArte, Node andraObjetivoGiro, String andraTipo, Label andraEstado, String andraTitulo, Node andraMeta, Node andraPieIzquierdo,
                          boolean andraDestacado, String andraBandera, Button andraEstrella, String andraAccesible, Runnable andraAlAbrir, int andraIndice) {
        this.andraObjetivoGiro = andraObjetivoGiro;
        getStyleClass().add("tile");
        if (andraDestacado) {
            getStyleClass().add("featured");
            AndraGaleria.andraDestacar(this);
        }
        AndraArte andraMarco = new AndraArte(andraDestacado, andraArte);
        andraMarco.getStyleClass().add("tile-art");
        if (andraBandera != null) {
            Label andraBandera2 = AndraComponentes.andraEtiqueta(andraBandera, "tile-flag");
            StackPane.setAlignment(andraBandera2, Pos.TOP_LEFT);
            StackPane.setMargin(andraBandera2, new Insets(12));
            andraMarco.getChildren().add(andraBandera2);
        }
        if (andraEstrella != null) {
            StackPane.setAlignment(andraEstrella, Pos.TOP_RIGHT);
            StackPane.setMargin(andraEstrella, new Insets(12));
            andraMarco.getChildren().add(andraEstrella);
        }
        Rectangle andraRecorte = new Rectangle();
        andraRecorte.setArcWidth(36);
        andraRecorte.setArcHeight(36);
        andraRecorte.widthProperty().bind(andraMarco.widthProperty());
        andraRecorte.heightProperty().bind(andraMarco.heightProperty().add(40));
        andraMarco.setClip(andraRecorte);
        if (andraDestacado) {
            VBox.setVgrow(andraMarco, Priority.ALWAYS);
        }

        HBox andraArriba = new HBox(8, AndraComponentes.andraMayus(andraTipo, "label-caps"), AndraComponentes.andraEspacio(), andraEstado);
        andraArriba.setAlignment(Pos.CENTER_LEFT);
        Label andraEtiquetaTitulo = AndraComponentes.andraLiteralParrafo(andraTitulo, "tile-titulo");
        andraAccion = AndraComponentes.andraEtiqueta("Ver detalles", "tile-cta");
        andraAccion.setGraphic(new FontIcon("fth-arrow-right"));
        andraAccion.setOpacity(0);
        HBox andraPie = new HBox(8, andraPieIzquierdo, AndraComponentes.andraEspacio(), andraAccion);
        andraPie.setAlignment(Pos.CENTER_LEFT);
        andraPie.setPadding(new Insets(8, 0, 0, 0));
        VBox andraCuerpo = new VBox(andraArriba, andraEtiquetaTitulo, andraMeta, andraPie);
        andraCuerpo.getStyleClass().add("tile-body");
        getChildren().addAll(andraMarco, andraCuerpo);
        setFocusTraversable(true);
        AndraComponentes.andraAccesible(this, andraAccesible);
        setOnMouseClicked(andraEvento -> andraAlAbrir.run());
        setOnKeyPressed(andraEvento -> {
            if (andraEvento.getCode() == KeyCode.ENTER || andraEvento.getCode() == KeyCode.SPACE) {
                andraAlAbrir.run();
                andraEvento.consume();
            }
        });
        setOnMouseEntered(andraEvento -> andraActivar());
        setOnMouseExited(andraEvento -> andraDesactivar());
        focusedProperty().addListener((andraObservable, andraBanderaA, andraBanderaB) -> {
            if (andraBanderaB) {
                getStyleClass().add("foco");
                andraActivar();
            } else {
                getStyleClass().remove("foco");
                andraDesactivar();
            }
        });
        setCursor(javafx.scene.Cursor.HAND);
        AndraMovimiento.andraAparecer(this, Duration.millis(450), Duration.millis(40L * andraIndice));
    }

    private void andraActivar() {
        AndraMovimiento.andraOpacidad(andraAccion, 1);
        if (AndraMovimiento.andraReducido() || andraObjetivoGiro == null) {
            return;
        }
        if (andraGiro == null) {
            andraGiro = new RotateTransition(Duration.millis(1150), andraObjetivoGiro);
            andraGiro.setAxis(Rotate.Y_AXIS);
            andraGiro.setFromAngle(0);
            andraGiro.setToAngle(360);
            andraGiro.setInterpolator(AndraMovimiento.ANDRA_GIRO);
            andraGiro.setOnFinished(andraEvento -> andraObjetivoGiro.setRotate(0));
        }
        if (andraGiro.getStatus() != Animation.Status.RUNNING) {
            andraGiro.playFromStart();
        }
    }

    private void andraDesactivar() {
        if (!isFocused() && !isHover()) {
            AndraMovimiento.andraOpacidad(andraAccion, 0);
        }
    }
}
