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
import pe.andra.service.AndraSeguimientoServicio.AndraEtapa;
import pe.andra.service.AndraSeguimientoServicio.AndraSeguimientoCjsv;
import pe.andra.util.AndraFechas;

public final class AndraRastreadorCjsv {

    private AndraRastreadorCjsv() {
    }

    public static VBox andraMini(AndraSeguimientoCjsv andraSeguimientoCjsv) {
        GridPane andraPasos = new GridPane();
        andraPasos.getStyleClass().add("trk");
        int andraCantidad = andraSeguimientoCjsv.andraEtapas().size();
        for (int andraIndice = 0; andraIndice < andraCantidad; andraIndice++) {
            ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
            andraRestriccionColumna.setPercentWidth(100.0 / andraCantidad);
            andraRestriccionColumna.setHalignment(javafx.geometry.HPos.CENTER);
            andraPasos.getColumnConstraints().add(andraRestriccionColumna);
        }
        for (int andraIndice = 0; andraIndice < andraCantidad; andraIndice++) {
            AndraEtapa andraEtapa = andraSeguimientoCjsv.andraEtapas().get(andraIndice);
            String andraEstado = andraIndice < andraSeguimientoCjsv.andraActual() ? "done" : andraIndice == andraSeguimientoCjsv.andraActual() ? "cur" : "";
            StackPane andraPunto = andraPunto(andraIndice < andraSeguimientoCjsv.andraActual() ? "fth-check" : andraEtapa.andraIcono(), andraIndice == andraSeguimientoCjsv.andraActual() && !andraSeguimientoCjsv.andraObservado());
            Label andraEtiqueta = AndraComponentes.andraParrafo(andraEtapa.andraTitulo(), "trk-l");
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
                if (andraIndice <= andraSeguimientoCjsv.andraActual()) {
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
        Label andraEta = AndraComponentes.andraParrafo(andraSeguimientoCjsv.andraEstimado());
        HBox andraFila = new HBox(new FontIcon(andraSeguimientoCjsv.andraObservado() ? "fth-alert-triangle" : "fth-clock"), andraEta);
        andraFila.getStyleClass().add("trk-eta");
        VBox andraCajaVertical = new VBox(andraPasos, andraFila);
        if (andraSeguimientoCjsv.andraObservado()) {
            andraCajaVertical.getStyleClass().add("trk-obs");
        }
        AndraComponentes.andraAccesible(andraCajaVertical, "Seguimiento: " + andraSeguimientoCjsv.andraEstimado());
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

    public static VBox andraLineaTiempo(AndraSeguimientoCjsv andraSeguimientoCjsv) {
        VBox andraCajaVertical = new VBox(0);
        for (int andraIndice = 0; andraIndice < andraSeguimientoCjsv.andraEtapas().size(); andraIndice++) {
            AndraEtapa andraEtapa = andraSeguimientoCjsv.andraEtapas().get(andraIndice);
            boolean andraHecho = andraIndice < andraSeguimientoCjsv.andraActual();
            boolean andraActual = andraIndice == andraSeguimientoCjsv.andraActual();
            StackPane andraPunto = new StackPane(new FontIcon(andraHecho ? "fth-check" : andraEtapa.andraIcono()));
            andraPunto.getStyleClass().add("tl-dot");
            String andraDetalle = andraHecho ? AndraFechas.andraDmy(andraSeguimientoCjsv.andraTiempos().get(andraIndice).toLocalDate()) + " · " + AndraFechas.andraHm(andraSeguimientoCjsv.andraTiempos().get(andraIndice))
                    : andraActual ? (andraSeguimientoCjsv.andraObservado() ? "Observado" : "En curso") : "Pendiente";
            VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraEtapa.andraTitulo(), "strong"), AndraComponentes.andraEtiqueta(andraDetalle, "hint"));
            Region andraLinea = new Region();
            andraLinea.getStyleClass().add("tl-linea");
            andraLinea.setMinHeight(18);
            VBox andraCol = new VBox(andraPunto);
            andraCol.setAlignment(Pos.TOP_CENTER);
            if (andraIndice < andraSeguimientoCjsv.andraEtapas().size() - 1) {
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
