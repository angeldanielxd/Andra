package pe.andra.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.util.Duration;

public final class AndraMovimiento {

    public static final Interpolator ANDRA_SUAVE = Interpolator.SPLINE(0.25, 0.1, 0.25, 1);
    public static final Interpolator ANDRA_GIRO = Interpolator.SPLINE(0.45, 0.05, 0.25, 1);
    public static final Duration ANDRA_RAPIDO = Duration.millis(180);

    private static final BooleanProperty ANDRA_REDUCIDO = new SimpleBooleanProperty(false);

    private AndraMovimiento() {
    }

    public static BooleanProperty andraReducidoPropiedad() {
        return ANDRA_REDUCIDO;
    }

    public static boolean andraReducido() {
        return ANDRA_REDUCIDO.get();
    }

    public static void andraAparecer(Node andraNodo, Duration andraDuracion, Duration andraRetraso) {
        if (andraReducido()) {
            andraNodo.setOpacity(1);
            return;
        }
        andraNodo.setOpacity(0);
        FadeTransition andraDesvanecido = new FadeTransition(andraDuracion, andraNodo);
        andraDesvanecido.setFromValue(0);
        andraDesvanecido.setToValue(1);
        andraDesvanecido.setDelay(andraRetraso);
        andraDesvanecido.setInterpolator(ANDRA_SUAVE);
        andraDesvanecido.play();
    }

    public static void andraSubir(Node andraNodo) {
        if (andraReducido()) {
            andraNodo.setOpacity(1);
            return;
        }
        andraNodo.setOpacity(0);
        andraNodo.setTranslateY(12);
        FadeTransition andraDesvanecido = new FadeTransition(Duration.millis(220), andraNodo);
        andraDesvanecido.setToValue(1);
        TranslateTransition andraTraslacion = new TranslateTransition(Duration.millis(220), andraNodo);
        andraTraslacion.setToY(0);
        ParallelTransition andraAnimacion = new ParallelTransition(andraDesvanecido, andraTraslacion);
        andraAnimacion.setInterpolator(ANDRA_SUAVE);
        andraAnimacion.play();
    }

    public static void andraDesvanecer(Node andraNodo, Runnable andraAlTerminar) {
        if (andraReducido()) {
            andraAlTerminar.run();
            return;
        }
        FadeTransition andraDesvanecido = new FadeTransition(Duration.millis(200), andraNodo);
        andraDesvanecido.setToValue(0);
        andraDesvanecido.setInterpolator(ANDRA_SUAVE);
        andraDesvanecido.setOnFinished(andraEvento -> andraAlTerminar.run());
        andraDesvanecido.play();
    }

    public static void andraOpacidad(Node andraNodo, double andraValor) {
        if (andraReducido()) {
            andraNodo.setOpacity(andraValor);
            return;
        }
        FadeTransition andraDesvanecido = new FadeTransition(ANDRA_RAPIDO, andraNodo);
        andraDesvanecido.setToValue(andraValor);
        andraDesvanecido.setInterpolator(ANDRA_SUAVE);
        andraDesvanecido.play();
    }
}
