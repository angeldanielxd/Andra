package pe.andra.ui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AndraCampo<C extends Region> {

    private final VBox andraCaja;
    private final C andraControl;
    private final Label andraError;
    private final List<Function<C, String>> andraReglas = new ArrayList<>();

    public AndraCampo(String andraEtiqueta, C andraControl) {
        this.andraControl = andraControl;
        this.andraError = AndraComponentes.andraParrafo("", "err");
        andraError.setVisible(false);
        andraError.setManaged(false);
        andraCaja = new VBox();
        andraCaja.getStyleClass().add("field");
        if (andraEtiqueta != null) {
            Label andraEtiqueta2 = AndraComponentes.andraEtiqueta(andraEtiqueta, "flabel");
            andraEtiqueta2.setLabelFor(andraControl);
            andraCaja.getChildren().add(andraEtiqueta2);
            AndraComponentes.andraAccesible(andraControl, andraEtiqueta);
        }
        andraCaja.getChildren().addAll(andraControl, andraError);
        andraControl.setMaxWidth(Double.MAX_VALUE);
        if (andraControl instanceof TextInputControl andraCampo) {
            andraCampo.textProperty().addListener((andraObservable, andraTextoA, andraTextoB) -> andraLimpiar());
        } else if (andraControl instanceof ComboBox<?> andraCombo) {
            andraCombo.valueProperty().addListener((andraObservable, andraObjetoA, andraObjetoB) -> andraLimpiar());
        } else if (andraControl instanceof DatePicker andraSelectorFecha) {
            andraSelectorFecha.valueProperty().addListener((andraObservable, andraFechaA, andraFechaB) -> andraLimpiar());
        }
    }

    public VBox andraNodo() {
        return andraCaja;
    }

    public C andraControl() {
        return andraControl;
    }

    public AndraCampo<C> andraRegla(Function<C, String> andraRegla) {
        andraReglas.add(andraRegla);
        return this;
    }

    public AndraCampo<C> andraRequerido(String andraMensaje) {
        return andraRegla(andraRegion -> andraVacio() ? andraMensaje : null);
    }

    public AndraCampo<C> andraMinimo(int andraCaracteres) {
        return andraRegla(andraRegion -> !andraVacio() && andraTexto().length() < andraCaracteres ? "Escribe al menos " + andraCaracteres + " caracteres." : null);
    }

    public AndraCampo<C> andraPatron(String andraPatron, String andraMensaje) {
        return andraRegla(andraRegion -> !andraVacio() && !andraTexto().matches(andraPatron) ? andraMensaje : null);
    }

    public AndraCampo<C> andraAyuda(String andraTexto) {
        andraCaja.getChildren().add(AndraComponentes.andraParrafo(andraTexto, "hint"));
        return this;
    }

    public String andraTexto() {
        if (andraControl instanceof TextInputControl andraCampo) {
            return andraCampo.getText() == null ? "" : andraCampo.getText().trim();
        }
        if (andraControl instanceof ComboBox<?> andraCombo && andraCombo.getValue() instanceof AndraOpcion andraOpcion) {
            return andraOpcion.andraValor();
        }
        if (andraControl instanceof DatePicker andraSelectorFecha) {
            return andraSelectorFecha.getValue() == null ? "" : andraSelectorFecha.getValue().toString();
        }
        if (andraControl instanceof AndraFormulario.AndraArchivo andraArchivo) {
            return andraArchivo.andraObtenerElegido() == null ? "" : andraArchivo.andraObtenerElegido().getName();
        }
        if (andraControl instanceof javafx.scene.control.CheckBox andraCasilla) {
            return andraCasilla.isSelected() ? "1" : "";
        }
        return "";
    }

    public boolean andraVacio() {
        return andraTexto().isBlank();
    }

    public String andraValidar() {
        for (Function<C, String> andraFuncion : andraReglas) {
            String andraTexto = andraFuncion.apply(andraControl);
            if (andraTexto != null) {
                andraMostrar(andraTexto);
                return andraTexto;
            }
        }
        andraLimpiar();
        return null;
    }

    public void andraMostrar(String andraMensaje) {
        AndraComponentes.andraFijar(andraError, andraMensaje);
        andraError.setVisible(true);
        andraError.setManaged(true);
        if (!andraControl.getStyleClass().contains("invalido")) {
            andraControl.getStyleClass().add("invalido");
        }
    }

    public void andraLimpiar() {
        andraError.setVisible(false);
        andraError.setManaged(false);
        andraControl.getStyleClass().remove("invalido");
    }
}
