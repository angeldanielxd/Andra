package pe.andra.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.model.AndraPersona;

import java.io.ByteArrayInputStream;

public final class AndraTarjetaCarne {

    private AndraTarjetaCarne() {
    }

    public static StackPane andraCrear(AndraPersona andraPersona, boolean andraVigente, byte[] andraFoto) {
        HBox andraSuperior = new HBox(10, AndraComponentes.andraLogo(30), new VBox(AndraComponentes.andraLiteral("Institución", "strong"),
                AndraComponentes.andraEtiqueta("Carné universitario digital", "small")), AndraComponentes.andraEspacio(),
                AndraComponentes.andraLiteral("2026-2", "dcard-sem"));
        andraSuperior.setAlignment(Pos.CENTER_LEFT);
        StackPane andraMarcoFoto = new StackPane();
        andraMarcoFoto.getStyleClass().add("dcard-foto");
        if (andraFoto != null && andraFoto.length > 0) {
            ImageView andraVistaImagen = new ImageView(new Image(new ByteArrayInputStream(andraFoto)));
            andraVistaImagen.setFitWidth(84);
            andraVistaImagen.setFitHeight(104);
            Rectangle andraRectangulo = new Rectangle(84, 104);
            andraRectangulo.setArcWidth(28);
            andraRectangulo.setArcHeight(28);
            andraVistaImagen.setClip(andraRectangulo);
            andraMarcoFoto.getChildren().add(andraVistaImagen);
        } else {
            andraMarcoFoto.getChildren().add(AndraComponentes.andraLiteral(andraPersona.andraObtenerIniciales(), "dcard-ini"));
        }
        VBox andraDatos = new VBox(1, AndraComponentes.andraLiteral(andraPersona.getAndraNombre(), "dcard-nombre"),
                AndraComponentes.andraEtiqueta("Código " + andraPersona.getAndraCodigo(), "dcard-dato"), AndraComponentes.andraEtiqueta("DNI " + andraPersona.getAndraDni(), "dcard-dato"),
                AndraComponentes.andraLiteral("EP " + andraPersona.getAndraEscuela().getAndraNombre(), "dcard-dato"),
                AndraComponentes.andraEtiqueta("Sede " + andraPersona.getAndraSede().getAndraNombre(), "dcard-dato"));
        HBox andraCuerpo = new HBox(16, andraMarcoFoto, andraDatos);
        andraCuerpo.setAlignment(Pos.CENTER_LEFT);
        Label andraEstado = AndraComponentes.andraEtiqueta(andraVigente ? "Vigente" : "Matrícula pendiente", "dc-state", andraVigente ? "ok" : "warn");
        andraEstado.setGraphic(new FontIcon(andraVigente ? "fth-check" : "fth-clock"));
        HBox andraPie = new HBox(andraEstado, AndraComponentes.andraEspacio(), AndraComponentes.andraEtiqueta("Válido hasta 31/03/2027", "small"));
        andraPie.setAlignment(Pos.CENTER_LEFT);
        VBox andraTarjeta = new VBox(andraSuperior, AndraComponentes.andraEspacioV(), andraCuerpo, AndraComponentes.andraEspacioV(), andraPie);
        andraTarjeta.getStyleClass().add("dcard");
        andraTarjeta.setMaxWidth(520);
        andraTarjeta.setPrefHeight(328);
        andraTarjeta.setMinHeight(300);
        StackPane andraCont = new StackPane(andraTarjeta);
        return andraCont;
    }
}
