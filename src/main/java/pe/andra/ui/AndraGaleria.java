package pe.andra.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class AndraGaleria extends Pane {

    private static final double ANDRA_MINIMO = 250;
    private static final double ANDRA_SEPARACION = 22;

    private record AndraLugar(Node andraNodo, int andraFila, int andraColumna, int andraAncho, int andraAlto) {
    }

    public AndraGaleria() {
        getStyleClass().add("gal");
    }

    public static void andraDestacar(Node andraNodo) {
        andraNodo.getProperties().put("destacado", Boolean.TRUE);
    }

    private static boolean andraDestacado(Node andraNodo) {
        return Boolean.TRUE.equals(andraNodo.getProperties().get("destacado"));
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    private int andraColumnas(double andraAncho) {
        return Math.max(1, (int) Math.floor((andraAncho + ANDRA_SEPARACION) / (ANDRA_MINIMO + ANDRA_SEPARACION)));
    }

    private List<AndraLugar> andraUbicar(int andraCols) {
        List<AndraLugar> andraLugares = new ArrayList<>();
        List<boolean[]> andraOcupado = new ArrayList<>();
        int andraFila = 0;
        int andraCol = 0;
        for (Node andraNodo : getManagedChildren()) {
            int andraAnchoCeldas = andraDestacado(andraNodo) && andraCols >= 2 ? 2 : 1;
            int andraAltoCeldas = andraAnchoCeldas;
            while (true) {
                while (andraOcupado.size() < andraFila + andraAltoCeldas) {
                    andraOcupado.add(new boolean[andraCols]);
                }
                if (andraCol + andraAnchoCeldas <= andraCols && andraLibre(andraOcupado, andraFila, andraCol, andraAnchoCeldas, andraAltoCeldas)) {
                    break;
                }
                andraCol++;
                if (andraCol >= andraCols) {
                    andraCol = 0;
                    andraFila++;
                }
            }
            for (int andraNumero = andraFila; andraNumero < andraFila + andraAltoCeldas; andraNumero++) {
                for (int andraNumero2 = andraCol; andraNumero2 < andraCol + andraAnchoCeldas; andraNumero2++) {
                    andraOcupado.get(andraNumero)[andraNumero2] = true;
                }
            }
            andraLugares.add(new AndraLugar(andraNodo, andraFila, andraCol, andraAnchoCeldas, andraAltoCeldas));
            andraCol += andraAnchoCeldas;
            if (andraCol >= andraCols) {
                andraCol = 0;
                andraFila++;
            }
        }
        return andraLugares;
    }

    private static boolean andraLibre(List<boolean[]> andraOcupado, int andraFila, int andraCol, int andraAnchoCeldas, int andraAltoCeldas) {
        for (int andraNumero = andraFila; andraNumero < andraFila + andraAltoCeldas; andraNumero++) {
            for (int andraNumero2 = andraCol; andraNumero2 < andraCol + andraAnchoCeldas; andraNumero2++) {
                if (andraOcupado.get(andraNumero)[andraNumero2]) {
                    return false;
                }
            }
        }
        return true;
    }

    private double[] andraAltos(List<AndraLugar> andraLugares, double andraAnchoColumna) {
        int andraFilas = andraLugares.stream().mapToInt(andraLugar -> andraLugar.andraFila() + andraLugar.andraAlto()).max().orElse(0);
        double[] andraAlto = new double[andraFilas];
        for (AndraLugar andraLugar : andraLugares) {
            if (andraLugar.andraAlto() == 1) {
                andraAlto[andraLugar.andraFila()] = Math.max(andraAlto[andraLugar.andraFila()], andraLugar.andraNodo().prefHeight(andraAnchoColumna));
            }
        }
        for (AndraLugar andraLugar : andraLugares) {
            if (andraLugar.andraAlto() > 1) {
                double andraAncho = andraAnchoColumna * andraLugar.andraAncho() + ANDRA_SEPARACION * (andraLugar.andraAncho() - 1);
                double andraNecesario = andraLugar.andraNodo().prefHeight(andraAncho);
                double andraActual = ANDRA_SEPARACION * (andraLugar.andraAlto() - 1);
                for (int andraNumero = andraLugar.andraFila(); andraNumero < andraLugar.andraFila() + andraLugar.andraAlto(); andraNumero++) {
                    andraActual += andraAlto[andraNumero];
                }
                if (andraNecesario > andraActual) {
                    andraAlto[andraLugar.andraFila() + andraLugar.andraAlto() - 1] += andraNecesario - andraActual;
                }
            }
        }
        return andraAlto;
    }

    @Override
    protected double computePrefHeight(double andraAncho) {
        double andraAncho2 = andraAncho < 0 ? Math.max(getWidth(), ANDRA_MINIMO) : andraAncho;
        int andraCols = andraColumnas(andraAncho2);
        double andraColW = (andraAncho2 - (andraCols - 1) * ANDRA_SEPARACION) / andraCols;
        double[] andraAlto = andraAltos(andraUbicar(andraCols), andraColW);
        double andraTotal = 0;
        for (double andraPosicionX : andraAlto) {
            andraTotal += andraPosicionX;
        }
        return andraTotal + Math.max(0, andraAlto.length - 1) * ANDRA_SEPARACION;
    }

    @Override
    protected double computePrefWidth(double andraAlto) {
        return ANDRA_MINIMO;
    }

    @Override
    protected double computeMinWidth(double andraAlto) {
        return ANDRA_MINIMO;
    }

    @Override
    protected double computeMinHeight(double andraAncho) {
        return computePrefHeight(andraAncho);
    }

    @Override
    protected void layoutChildren() {
        double andraAncho = getWidth();
        int andraCols = andraColumnas(andraAncho);
        double andraColW = (andraAncho - (andraCols - 1) * ANDRA_SEPARACION) / andraCols;
        List<AndraLugar> andraLugares = andraUbicar(andraCols);
        double[] andraAlto = andraAltos(andraLugares, andraColW);
        double[] andraPosicionY = new double[andraAlto.length + 1];
        for (int andraIndice = 0; andraIndice < andraAlto.length; andraIndice++) {
            andraPosicionY[andraIndice + 1] = andraPosicionY[andraIndice] + andraAlto[andraIndice] + ANDRA_SEPARACION;
        }
        for (AndraLugar andraLugar : andraLugares) {
            double andraPosicionX = andraLugar.andraColumna() * (andraColW + ANDRA_SEPARACION);
            double andraAncho2 = andraColW * andraLugar.andraAncho() + ANDRA_SEPARACION * (andraLugar.andraAncho() - 1);
            double andraAlto2 = andraPosicionY[andraLugar.andraFila() + andraLugar.andraAlto()] - andraPosicionY[andraLugar.andraFila()] - ANDRA_SEPARACION;
            andraLugar.andraNodo().resizeRelocate(snapPositionX(andraPosicionX), snapPositionY(andraPosicionY[andraLugar.andraFila()]), snapSizeX(andraAncho2), snapSizeY(andraAlto2));
        }
    }
}
