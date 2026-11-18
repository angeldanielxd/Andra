package pe.andra.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.ui.AndraNotificador;
import pe.andra.ui.AndraPieDePagina;
import pe.andra.ui.AndraVista;

import java.util.List;

public abstract class AndraVistaBase implements AndraVista {

    @FXML
    protected ScrollPane andraDesplazable;
    @FXML
    protected VBox andraPagina;

    @Autowired
    protected AndraPieDePagina andraPie;
    @Autowired
    protected AndraNotificador andraNotificador;

    protected abstract List<Node> andraConstruir();

    protected boolean andraConPie() {
        return true;
    }

    @Override
    public void andraRefrescar() {
        andraPagina.getChildren().setAll(andraConstruir());
        if (andraConPie()) {
            andraPagina.getChildren().add(andraPie.andraCrear());
        }
    }

    public void andraRedibujar() {
        double andraValor = andraDesplazable.getVvalue();
        andraRefrescar();
        Platform.runLater(() -> {
            andraDesplazable.layout();
            andraDesplazable.setVvalue(andraValor);
        });
    }

    protected boolean andraIntentar(Runnable andraAccion) {
        try {
            andraAccion.run();
            return true;
        } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
            andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            return false;
        }
    }

    protected void andraIr(String andraRuta) {
        pe.andra.ui.AndraNavegacion.andraIr(andraPagina, andraRuta);
    }
}
