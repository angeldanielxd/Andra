package pe.andra.ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.service.AndraDocumentoGeneradoServicio.AndraDocumentoOficial;
import pe.andra.service.AndraQrServicio;

import java.io.ByteArrayInputStream;

public final class AndraVistaDocumento {

    private AndraVistaDocumento() {
    }

    private static Text andraT(String andraTexto, boolean andraNegrita) {
        Text andraPosicionX = new Text(andraTexto);
        andraPosicionX.getStyleClass().add("dg-texto");
        if (andraNegrita) {
            andraPosicionX.setStyle("-fx-font-weight: 700;");
        }
        return andraPosicionX;
    }

    public static StackPane andraCrear(AndraDocumentoOficial andraDocumentoOficial) {
        if ("carne".equals(andraDocumentoOficial.andraPlantilla())) {
            return andraCarne(andraDocumentoOficial);
        }
        VBox andraTextos = new VBox(AndraComponentes.andraLiteral("Institución", "strong"),
                AndraComponentes.andraLiteral("Dirección de Servicios Académicos · Andra", "hint"));
        HBox andraCabecera = new HBox(12, AndraComponentes.andraLogo(40), andraTextos);
        andraCabecera.getStyleClass().add("dg-head");
        Label andraTitulo = AndraComponentes.andraLiteralParrafo(andraDocumentoOficial.andraTitulo(), "dg-titulo");
        andraTitulo.setMaxWidth(Double.MAX_VALUE);
        andraTitulo.setAlignment(Pos.CENTER);
        TextFlow andraCuerpo = new TextFlow(andraT("La Dirección de Servicios Académicos hace constar que ", false),
                andraT(andraDocumentoOficial.andraSolicitante().getAndraNombre(), true), andraT(", identificado(a) con DNI N.° ", false), andraT(andraDocumentoOficial.andraSolicitante().getAndraDni(), true));
        if (andraDocumentoOficial.andraConCodigo()) {
            andraCuerpo.getChildren().addAll(andraT(" y código universitario N.° ", false), andraT(andraDocumentoOficial.andraSolicitante().getAndraCodigo(), true));
        }
        andraCuerpo.getChildren().add(andraT(", " + andraDocumentoOficial.andraRol() + ", sede " + andraDocumentoOficial.andraSede() + ".", false));
        andraCuerpo.setLineSpacing(4);
        VBox andraHoja = new VBox(14, andraCabecera, andraTitulo, andraCuerpo);
        if (!andraDocumentoOficial.andraSemestres().isEmpty()) {
            GridPane andraTabla = new GridPane();
            for (double andraValorA : new double[]{50, 25, 25}) {
                ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
                andraRestriccionColumna.setPercentWidth(andraValorA);
                andraTabla.getColumnConstraints().add(andraRestriccionColumna);
            }
            andraTabla.getColumnConstraints().get(1).setHalignment(HPos.RIGHT);
            andraTabla.getColumnConstraints().get(2).setHalignment(HPos.RIGHT);
            String[] andraCab = {"SEMESTRE", "CRÉDITOS", "PROMEDIO"};
            for (int andraIndice = 0; andraIndice < 3; andraIndice++) {
                andraTabla.add(AndraComponentes.andraLiteral(andraCab[andraIndice], "inv-th"), andraIndice, 0);
            }
            for (int andraIndice = 0; andraIndice < andraDocumentoOficial.andraSemestres().size(); andraIndice++) {
                var andraSemestre = andraDocumentoOficial.andraSemestres().get(andraIndice);
                andraTabla.add(AndraComponentes.andraLiteral(andraSemestre.andraPeriodo()), 0, andraIndice + 1);
                andraTabla.add(AndraComponentes.andraLiteral(String.valueOf(andraSemestre.andraCreditos())), 1, andraIndice + 1);
                andraTabla.add(AndraComponentes.andraLiteral(andraSemestre.andraPromedio()), 2, andraIndice + 1);
            }
            andraTabla.setVgap(8);
            andraHoja.getChildren().add(andraTabla);
        }
        if ("certificado".equals(andraDocumentoOficial.andraPlantilla())) {
            andraHoja.getChildren().add(new TextFlow(andraT("Se certifica que ha aprobado las asignaturas correspondientes a los semestres 2024-2 al 2026-1, conforme al detalle registrado en el sistema académico.", false)));
        }
        andraHoja.getChildren().add(new TextFlow(andraT("Se expide el presente documento a solicitud del interesado(a) para fines de ", false),
                andraT(andraDocumentoOficial.andraUso(), true), andraT(".", false)));
        Label andraFecha = AndraComponentes.andraLiteral(andraDocumentoOficial.andraFecha());
        HBox andraFilaFecha = new HBox(AndraComponentes.andraEspacio(), andraFecha);
        andraFilaFecha.setPadding(new Insets(6, 0, 0, 0));
        Region andraLinea = new Region();
        andraLinea.setMinSize(180, 1);
        andraLinea.setMaxSize(180, 1);
        andraLinea.setStyle("-fx-background-color: -color-text;");
        VBox andraFirma = new VBox(4, andraLinea, AndraComponentes.andraLiteral("Firma digital", "small"), AndraComponentes.andraLiteral("Director(a) de Servicios Académicos", "hint"));
        andraFirma.setAlignment(Pos.CENTER);
        ImageView andraImagenQr = new ImageView(AndraQrServicio.andraImagen(andraDocumentoOficial.andraContenidoQr(), 192));
        andraImagenQr.setFitWidth(96);
        andraImagenQr.setFitHeight(96);
        VBox andraQrCaja = new VBox(4, andraImagenQr, AndraComponentes.andraLiteral("Verificación: " + andraDocumentoOficial.andraVerificacion(), "hint"));
        andraQrCaja.setAlignment(Pos.CENTER);
        HBox andraPie = new HBox(andraFirma, AndraComponentes.andraEspacio(), andraQrCaja);
        andraPie.setAlignment(Pos.BOTTOM_LEFT);
        andraPie.setPadding(new Insets(20, 0, 0, 0));
        andraHoja.getChildren().addAll(andraFilaFecha, andraPie);
        andraHoja.getStyleClass().add("docgen");
        StackPane andraCont = new StackPane(andraHoja);
        andraCont.getStyleClass().add("sheet");
        Label andraMarca = AndraComponentes.andraLiteral("DEMOSTRACIÓN", "watermark");
        andraMarca.setMouseTransparent(true);
        StackPane.setAlignment(andraMarca, Pos.CENTER);
        andraCont.getChildren().add(andraMarca);
        return andraCont;
    }

    private static StackPane andraCarne(AndraDocumentoOficial andraDocumentoOficial) {
        HBox andraSuperior = new HBox(10, AndraComponentes.andraLogo(30), new VBox(AndraComponentes.andraLiteral("Institución", "strong"),
                AndraComponentes.andraLiteral("Carné universitario 2026-2", "small")));
        andraSuperior.setPadding(new Insets(14, 16, 14, 16));
        andraSuperior.setStyle("-fx-background-color: -color-primary; -fx-background-radius: 18 18 0 0;");
        andraSuperior.getChildren().get(1).setStyle("-fx-text-fill: white;");
        ((VBox) andraSuperior.getChildren().get(1)).getChildren().forEach(andraNodo -> andraNodo.setStyle("-fx-text-fill: white;"));
        StackPane andraFoto = new StackPane();
        andraFoto.setMinSize(96, 118);
        andraFoto.setMaxSize(96, 118);
        andraFoto.setStyle("-fx-background-color: -color-fill; -fx-background-radius: 10;");
        if (andraDocumentoOficial.andraFoto() != null && andraDocumentoOficial.andraFoto().length > 0) {
            ImageView andraVistaImagen = new ImageView(new Image(new ByteArrayInputStream(andraDocumentoOficial.andraFoto())));
            andraVistaImagen.setFitWidth(96);
            andraVistaImagen.setFitHeight(118);
            andraVistaImagen.setPreserveRatio(false);
            Rectangle andraRectangulo = new Rectangle(96, 118);
            andraRectangulo.setArcWidth(20);
            andraRectangulo.setArcHeight(20);
            andraVistaImagen.setClip(andraRectangulo);
            andraFoto.getChildren().add(andraVistaImagen);
        } else {
            andraFoto.getChildren().add(new FontIcon("fth-user"));
        }
        VBox andraDatos = new VBox(2, AndraComponentes.andraLiteral(andraDocumentoOficial.andraSolicitante().getAndraNombre(), "strong"),
                AndraComponentes.andraLiteral("DNI " + andraDocumentoOficial.andraSolicitante().getAndraDni(), "small"),
                AndraComponentes.andraLiteral("Código " + (andraDocumentoOficial.andraSolicitante().getAndraCodigo() == null ? "—" : andraDocumentoOficial.andraSolicitante().getAndraCodigo()), "small"),
                AndraComponentes.andraLiteral("EP " + andraDocumentoOficial.andraEscuela(), "small"), AndraComponentes.andraLiteral("Sede " + andraDocumentoOficial.andraSede(), "small"));
        HBox andraCuerpo = new HBox(16, andraFoto, andraDatos);
        andraCuerpo.setPadding(new Insets(16));
        HBox andraPie = new HBox(AndraComponentes.andraLiteral("Válido hasta 31/03/2027", "hint"), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(andraDocumentoOficial.andraVerificacion(), "hint"));
        andraPie.setPadding(new Insets(10, 16, 10, 16));
        andraPie.setStyle("-fx-border-color: -color-border transparent transparent transparent;");
        VBox andraTarjeta = new VBox(andraSuperior, andraCuerpo, andraPie);
        andraTarjeta.setMaxWidth(420);
        andraTarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-border-color: -color-border; -fx-border-radius: 18;");
        StackPane andraCont = new StackPane(andraTarjeta);
        Label andraMarca = AndraComponentes.andraLiteral("DEMOSTRACIÓN", "watermark", "sm");
        andraMarca.setMouseTransparent(true);
        andraCont.getChildren().add(andraMarca);
        return andraCont;
    }

    public static LinearGradient andraDegradadoCarne() {
        return new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#0A63C9")),
                new Stop(0.55, Color.web("#0071E3")), new Stop(1, Color.web("#2B8CF0")));
    }
}
