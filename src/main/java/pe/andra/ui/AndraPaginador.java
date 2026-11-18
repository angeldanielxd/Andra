package pe.andra.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.function.IntConsumer;

public final class AndraPaginador {

    private AndraPaginador() {
    }

    public static HBox andraCrear(int andraPagina, int andraPaginas, int andraTotal, int andraPorPagina, String andraUnidad, IntConsumer andraIrAPagina) {
        int andraDesde = andraTotal == 0 ? 0 : (andraPagina - 1) * andraPorPagina + 1;
        int andraHasta = Math.min(andraPagina * andraPorPagina, andraTotal);
        Label andraResumen = AndraComponentes.andraEtiqueta("Mostrando " + andraDesde + " - " + andraHasta + " de " + andraTotal + " " + andraUnidad, "hint");
        HBox andraBotones = new HBox();
        andraBotones.getStyleClass().add("pager-btns");
        Button andraAnterior = AndraComponentes.andraBotonSm("‹ Anterior", "btn-secondary", null, andraEvento -> andraIrAPagina.accept(andraPagina - 1));
        andraAnterior.setDisable(andraPagina <= 1);
        andraBotones.getChildren().add(andraAnterior);
        for (int andraIndice = 1; andraIndice <= andraPaginas; andraIndice++) {
            final int andraNumero = andraIndice;
            Button andraBoton = AndraComponentes.andraBotonSm(null, "btn-secondary", null, andraEvento -> andraIrAPagina.accept(andraNumero));
            andraBoton.setText(String.valueOf(andraIndice));
            andraBoton.getStyleClass().add("btn-icon");
            if (andraIndice == andraPagina) {
                andraBoton.getStyleClass().add("actual");
            }
            andraBotones.getChildren().add(andraBoton);
        }
        Button andraSiguiente = AndraComponentes.andraBotonSm("Siguiente ›", "btn-secondary", null, andraEvento -> andraIrAPagina.accept(andraPagina + 1));
        andraSiguiente.setDisable(andraPagina >= andraPaginas);
        andraBotones.getChildren().add(andraSiguiente);
        HBox andraPaginador = new HBox(andraResumen, AndraComponentes.andraEspacio(), andraBotones);
        andraPaginador.getStyleClass().add("pager");
        return andraPaginador;
    }
}
