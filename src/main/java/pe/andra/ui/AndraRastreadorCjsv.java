package pe.andra.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.service.DixonSeguimientoServicio.DixonEtapa;
import pe.andra.service.DixonSeguimientoServicio.DixonSeguimientoCjsv;
import pe.andra.util.DixonFechas;

public final class AndraRastreadorCjsv {

    private AndraRastreadorCjsv() {
    }

    public static VBox andraMini(DixonSeguimientoCjsv andraSeguimientoCjsv) {
        GridPane andraPasos = new GridPane();
        andraPasos.getStyleClass().add("trk");
        int andraCantidad = andraSeguimientoCjsv.dixonEtapas().size();
        for (int andraIndice = 0; andraIndice < andraCantidad; andraIndice++) {
            ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
            andraRestriccionColumna.setPercentWidth(100.0 / andraCantidad);
            andraRestriccionColumna.setHalignment(javafx.geometry.HPos.CENTER);
            andraPasos.getColumnConstraints().add(andraRestriccionColumna);
        }
        for (int andraIndice = 0; andraIndice < andraCantidad; andraIndice++) {
            DixonEtapa andraEtapa = andraSeguimientoCjsv.dixonEtapas().get(andraIndice);
            String andraEstado = andraIndice < andraSeguimientoCjsv.dixonActual() ? "done" : andraIndice == andraSeguimientoCjsv.dixonActual() ? "cur" : "";
            StackPane andraPunto = andraPunto(andraIndice < andraSeguimientoCjsv.dixonActual() ? "fth-check" : andraEtapa.dixonIcono(), andraIndice == andraSeguimientoCjsv.dixonActual() && !andraSeguimientoCjsv.dixonObservado());
            Label andraEtiqueta = AndraComponentes.andraParrafo(andraEtapa.dixonTitulo(), "trk-l");
            andraEtiqueta.setAlignment(Pos.CENTER);
            andraEtiqueta.setMaxWidth(Double.MAX_VALUE);
            VBox andraPaso = new VBox(6, andraPunto, andraEtiqueta);
            andraPaso.setAlignment(Pos.TOP_CENTER);
            andraPaso.getStyleClass().add("trk-s");
            if (!andraEstado.isEmpty()) {
                andraPaso.getStyleClass().add(andraEstado);
            }
            StackPane andraCelda = new StackPane();
            if (andraIndice > 0) {
                Region andraLinea = new Region();
                andraLinea.getStyleClass().add("trk-linea");
                if (andraIndice <= andraSeguimientoCjsv.dixonActual()) {
                    andraLinea.getStyleClass().add("done");
                }
                andraLinea.setManaged(false);
                andraCelda.getChildren().add(andraLinea);
                andraCelda.widthProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> {
                    andraLinea.resizeRelocate(-andraNumeroB.doubleValue() / 2 + 16, 14.5, Math.max(0, andraNumeroB.doubleValue() - 32), 3);
                });
            }
            andraCelda.getChildren().add(andraPaso);
            StackPane.setAlignment(andraPaso, Pos.TOP_CENTER);
            andraPasos.add(andraCelda, andraIndice, 0);
        }
        Label andraEta = AndraComponentes.andraParrafo(andraSeguimientoCjsv.dixonEstimado());
        HBox andraFila = new HBox(new FontIcon(andraSeguimientoCjsv.dixonObservado() ? "fth-alert-triangle" : "fth-clock"), andraEta);
        andraFila.getStyleClass().add("trk-eta");
        VBox andraCajaVertical = new VBox(andraPasos, andraFila);
        if (andraSeguimientoCjsv.dixonObservado()) {
            andraCajaVertical.getStyleClass().add("trk-obs");
        }
        AndraComponentes.andraAccesible(andraCajaVertical, "Seguimiento: " + andraSeguimientoCjsv.dixonEstimado());
        return andraCajaVertical;
    }

    private static StackPane andraPunto(String andraIcono, boolean andraPulso) {
        StackPane andraCapa = new StackPane(new FontIcon(andraIcono));
        andraCapa.getStyleClass().add("trk-dot");
        if (andraPulso && !AndraMovimiento.andraReducido()) {
            Circle andraHalo = new Circle(16);
            andraHalo.getStyleClass().add("trk-halo");
            andraHalo.setManaged(false);
            andraHalo.setMouseTransparent(true);
            andraHalo.setCenterX(16);
            andraHalo.setCenterY(16);
            StackPane andraCont = new StackPane(andraCapa);
            andraCont.getChildren().add(0, andraHalo);
            ScaleTransition andraEscala = new ScaleTransition(Duration.millis(1800), andraHalo);
            andraEscala.setFromX(1);
            andraEscala.setFromY(1);
            andraEscala.setToX(1.7);
            andraEscala.setToY(1.7);
            FadeTransition andraDesvanecido = new FadeTransition(Duration.millis(1800), andraHalo);
            andraDesvanecido.setFromValue(0.9);
            andraDesvanecido.setToValue(0);
            ParallelTransition andraAnimacion = new ParallelTransition(andraEscala, andraDesvanecido);
            andraAnimacion.setCycleCount(Animation.INDEFINITE);
            andraAnimacion.play();
            andraCont.setMaxSize(32, 32);
            return andraCont;
        }
        return andraCapa;
    }

    public static VBox andraLineaTiempo(DixonSeguimientoCjsv andraSeguimientoCjsv) {
        VBox andraCajaVertical = new VBox(0);
        for (int andraIndice = 0; andraIndice < andraSeguimientoCjsv.dixonEtapas().size(); andraIndice++) {
            DixonEtapa andraEtapa = andraSeguimientoCjsv.dixonEtapas().get(andraIndice);
            boolean andraHecho = andraIndice < andraSeguimientoCjsv.dixonActual();
            boolean andraActual = andraIndice == andraSeguimientoCjsv.dixonActual();
            StackPane andraPunto = new StackPane(new FontIcon(andraHecho ? "fth-check" : andraEtapa.dixonIcono()));
            andraPunto.getStyleClass().add("tl-dot");
            String andraDetalle = andraHecho ? DixonFechas.dixonDmy(andraSeguimientoCjsv.dixonTiempos().get(andraIndice).toLocalDate()) + " · " + DixonFechas.dixonHm(andraSeguimientoCjsv.dixonTiempos().get(andraIndice))
                    : andraActual ? (andraSeguimientoCjsv.dixonObservado() ? "Observado" : "En curso") : "Pendiente";
            VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraEtapa.dixonTitulo(), "strong"), AndraComponentes.andraEtiqueta(andraDetalle, "hint"));
            Region andraLinea = new Region();
            andraLinea.getStyleClass().add("tl-linea");
            andraLinea.setMinHeight(18);
            VBox andraCol = new VBox(andraPunto);
            andraCol.setAlignment(Pos.TOP_CENTER);
            if (andraIndice < andraSeguimientoCjsv.dixonEtapas().size() - 1) {
                andraCol.getChildren().add(andraLinea);
                VBox.setVgrow(andraLinea, Priority.ALWAYS);
            }
            HBox andraElemento = new HBox(14, andraCol, andraTextos);
            andraElemento.getStyleClass().add("tl-item");
            if (andraHecho) {
                andraElemento.getStyleClass().add("done");
            } else if (andraActual) {
                andraElemento.getStyleClass().add("current");
            }
            andraTextos.setPadding(new Insets(2, 0, 14, 0));
            andraCajaVertical.getChildren().add(andraElemento);
        }
        return andraCajaVertical;
    }
}
