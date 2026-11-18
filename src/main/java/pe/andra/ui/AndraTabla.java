package pe.andra.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.List;

public final class AndraTabla {

    private AndraTabla() {
    }

    private static GridPane andraFila(double[] andraAnchos, String andraClase) {
        GridPane andraRejilla = new GridPane();
        andraRejilla.getStyleClass().add(andraClase);
        for (double andraValorA : andraAnchos) {
            ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
            andraRestriccionColumna.setPercentWidth(andraValorA);
            andraRestriccionColumna.setFillWidth(true);
            andraRejilla.getColumnConstraints().add(andraRestriccionColumna);
        }
        return andraRejilla;
    }

    public static VBox andraCrear(String[] andraCabeceras, double[] andraAnchos, boolean[] andraDerecha, List<Node[]> andraFilas) {
        VBox andraCajaVertical = new VBox();
        andraCajaVertical.getStyleClass().add("tbl");
        GridPane andraCab = andraFila(andraAnchos, "tbl-h");
        for (int andraIndice = 0; andraIndice < andraCabeceras.length; andraIndice++) {
            HBox andraCelda = new HBox(AndraComponentes.andraMayus(andraCabeceras[andraIndice]));
            andraCelda.setAlignment(andraDerecha != null && andraDerecha[andraIndice] ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            andraCab.add(andraCelda, andraIndice, 0);
        }
        andraCajaVertical.getChildren().add(andraCab);
        for (Node[] andraNodos : andraFilas) {
            GridPane andraRejilla = andraFila(andraAnchos, "tbl-r");
            for (int andraIndice = 0; andraIndice < andraNodos.length; andraIndice++) {
                HBox andraCelda = new HBox(andraNodos[andraIndice]);
                andraCelda.setAlignment(andraDerecha != null && andraDerecha[andraIndice] ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                andraCelda.setMinWidth(0);
                andraRejilla.add(andraCelda, andraIndice, 0);
            }
            andraCajaVertical.getChildren().add(andraRejilla);
        }
        AndraComponentes.andraMarcarUltima(andraCajaVertical);
        return andraCajaVertical;
    }

    public static VBox andraCelda(String andraPrincipal, String andraSecundario) {
        VBox andraCajaVertical = new VBox(2, AndraComponentes.andraLiteralParrafo(andraPrincipal, "strong", "small"));
        if (andraSecundario != null) {
            andraCajaVertical.getChildren().add(AndraComponentes.andraParrafo(andraSecundario, "hint"));
        }
        return andraCajaVertical;
    }
}
