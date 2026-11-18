package pe.andra.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;

public class AndraRejilla extends Pane {

    private int andraColumnas;
    private double andraAnchoMinimo;
    private double andraEspacioHorizontal = 24;
    private double andraEspacioVertical = 24;

    public AndraRejilla(int andraColumnas, double andraAnchoMinimo) {
        this.andraColumnas = andraColumnas;
        this.andraAnchoMinimo = andraAnchoMinimo;
        getStyleClass().add("rejilla");
    }

    public AndraRejilla(int andraColumnas, double andraAnchoMinimo, double andraSeparacion) {
        this(andraColumnas, andraAnchoMinimo);
        this.andraEspacioHorizontal = andraSeparacion;
        this.andraEspacioVertical = andraSeparacion;
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    private int andraColumnasPara(double andraAncho) {
        int andraNumero = andraColumnas;
        while (andraNumero > 1 && (andraAncho - (andraNumero - 1) * andraEspacioHorizontal) / andraNumero < andraAnchoMinimo) {
            andraNumero--;
        }
        return andraNumero;
    }

    @Override
    protected double computePrefWidth(double andraAlto) {
        Insets andraMargenes = getInsets();
        return andraMargenes.getLeft() + andraMargenes.getRight() + andraAnchoMinimo;
    }

    @Override
    protected double computeMinWidth(double andraAlto) {
        return computePrefWidth(andraAlto);
    }

    @Override
    protected double computePrefHeight(double andraAncho) {
        Insets andraMargenes = getInsets();
        double andraUtilidad = (andraAncho < 0 ? Math.max(getWidth(), andraAnchoMinimo) : andraAncho) - andraMargenes.getLeft() - andraMargenes.getRight();
        List<Node> andraHijos = getManagedChildren();
        if (andraHijos.isEmpty()) {
            return andraMargenes.getTop() + andraMargenes.getBottom();
        }
        int andraNumero = andraColumnasPara(andraUtilidad);
        double andraAncho2 = Math.max(0, (andraUtilidad - (andraNumero - 1) * andraEspacioHorizontal) / andraNumero);
        double andraTotal = 0;
        for (int andraIndice = 0; andraIndice < andraHijos.size(); andraIndice += andraNumero) {
            andraTotal += andraAltoFila(andraHijos, andraIndice, andraNumero, andraAncho2);
        }
        andraTotal += andraEspacioVertical * (Math.ceil(andraHijos.size() / (double) andraNumero) - 1);
        return andraMargenes.getTop() + andraMargenes.getBottom() + andraTotal;
    }

    @Override
    protected double computeMinHeight(double andraAncho) {
        return computePrefHeight(andraAncho);
    }

    @Override
    protected void layoutChildren() {
        Insets andraMargenes = getInsets();
        double andraUtilidad = getWidth() - andraMargenes.getLeft() - andraMargenes.getRight();
        List<Node> andraHijos = getManagedChildren();
        int andraNumero = andraColumnasPara(andraUtilidad);
        double andraAncho = Math.max(0, (andraUtilidad - (andraNumero - 1) * andraEspacioHorizontal) / andraNumero);
        double andraPosicionY = andraMargenes.getTop();
        for (int andraIndice = 0; andraIndice < andraHijos.size(); andraIndice += andraNumero) {
            double andraAlto = andraAltoFila(andraHijos, andraIndice, andraNumero, andraAncho);
            for (int andraIndice2 = andraIndice; andraIndice2 < Math.min(andraIndice + andraNumero, andraHijos.size()); andraIndice2++) {
                double andraPosicionX = andraMargenes.getLeft() + (andraIndice2 - andraIndice) * (andraAncho + andraEspacioHorizontal);
                andraHijos.get(andraIndice2).resizeRelocate(snapPositionX(andraPosicionX), snapPositionY(andraPosicionY), snapSizeX(andraAncho), snapSizeY(andraAlto));
            }
            andraPosicionY += andraAlto + andraEspacioVertical;
        }
    }

    private double andraAltoFila(List<Node> andraHijos, int andraDesde, int andraNumero, double andraAncho) {
        double andraAlto = 0;
        for (int andraIndice = andraDesde; andraIndice < Math.min(andraDesde + andraNumero, andraHijos.size()); andraIndice++) {
            andraAlto = Math.max(andraAlto, andraHijos.get(andraIndice).prefHeight(andraAncho));
        }
        return andraAlto;
    }
}
