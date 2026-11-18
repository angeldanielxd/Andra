package pe.andra.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

public class AndraSegmentado extends HBox {

    private final ToggleGroup andraGrupo = new ToggleGroup();
    private Consumer<String> andraAlCambiar = andraValor -> {
    };
    private boolean andraSilencio;

    public AndraSegmentado(boolean andraGrande) {
        getStyleClass().add("seg");
        if (andraGrande) {
            getStyleClass().add("seg-lg");
        }
        andraGrupo.selectedToggleProperty().addListener((andraObservable, andraOpcionA, andraOpcionB) -> {
            if (andraOpcionB == null) {
                andraGrupo.selectToggle(andraOpcionA);
                return;
            }
            if (!andraSilencio) {
                andraAlCambiar.accept((String) andraOpcionB.getUserData());
            }
        });
    }

    public AndraSegmentado andraOpcion(String andraValor, String andraTexto, String andraIcono, Long andraConteo) {
        ToggleButton andraBotonOpcion = new ToggleButton();
        AndraComponentes.andraFijar(andraBotonOpcion, andraTexto);
        andraBotonOpcion.setUserData(andraValor);
        andraBotonOpcion.setToggleGroup(andraGrupo);
        andraBotonOpcion.getStyleClass().add("seg-btn");
        andraBotonOpcion.setMnemonicParsing(false);
        andraBotonOpcion.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(andraBotonOpcion, Priority.ALWAYS);
        Node andraGrafico = null;
        if (andraIcono != null) {
            andraGrafico = new FontIcon(andraIcono);
        }
        if (andraConteo != null) {
            Label andraEtiqueta = AndraComponentes.andraConteo(andraConteo);
            andraBotonOpcion.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
            andraBotonOpcion.setGraphic(andraEtiqueta);
        } else if (andraGrafico != null) {
            andraBotonOpcion.setGraphic(andraGrafico);
        }
        getChildren().add(andraBotonOpcion);
        return this;
    }

    public AndraSegmentado andraOpcionNodo(String andraValor, String andraTexto, Node andraGrafico) {
        andraOpcion(andraValor, andraTexto, null, null);
        ((ToggleButton) getChildren().get(getChildren().size() - 1)).setGraphic(andraGrafico);
        return this;
    }

    public AndraSegmentado andraAlCambiar(Consumer<String> andraAccion) {
        this.andraAlCambiar = andraAccion;
        return this;
    }

    public AndraSegmentado andraSeleccionar(String andraValor) {
        andraSilencio = true;
        andraGrupo.getToggles().stream().filter(andraOpcion -> andraValor.equals(andraOpcion.getUserData())).findFirst().ifPresent(andraGrupo::selectToggle);
        andraSilencio = false;
        return this;
    }

    public String andraValor() {
        return andraGrupo.getSelectedToggle() == null ? null : (String) andraGrupo.getSelectedToggle().getUserData();
    }
}
