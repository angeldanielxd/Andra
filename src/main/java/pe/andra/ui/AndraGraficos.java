package pe.andra.ui;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pe.andra.service.AndraAdministradorServicio;
import pe.andra.service.AndraAnaliticaServicio;
import pe.andra.service.AndraCatalogoServicio;
import pe.andra.service.AndraIdiomaServicio;
import pe.andra.util.AndraMontos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class AndraGraficos {

    private AndraGraficos() {
    }

    public static Pane andraBarrasDias(List<AndraAdministradorServicio.AndraDia> andraDias) {
        Pane andraLienzo = new Pane();
        andraLienzo.widthProperty().addListener((andraObs, andraAnterior, andraNuevo) -> {
            {
                andraLienzo.getChildren().clear();
                double andraAncho = andraNuevo.doubleValue();
                double andraAlto = 180;
                double andraMaximo = Math.max(1, andraDias.stream().mapToDouble(andraDia -> andraDia.andraMonto().doubleValue()).max().orElse(1));
                for (double andraFactor : new double[]{0, 0.5, 1}) {
                    double andraPosicionY = andraAlto - andraAlto * andraFactor * 0.9;
                    Line andraLinea = new Line(0, andraPosicionY, andraAncho, andraPosicionY);
                    andraLinea.getStyleClass().add("grid-l");
                    andraLienzo.getChildren().add(andraLinea);
                }
                double andraAnchoBarra = andraAncho / andraDias.size();
                for (int andraIndice = 0; andraIndice < andraDias.size(); andraIndice++) {
                    AndraAdministradorServicio.AndraDia andraDia = andraDias.get(andraIndice);
                    double andraAlto2 = Math.max(2, andraDia.andraMonto().doubleValue() / andraMaximo * andraAlto * 0.9);
                    Rectangle andraRectangulo = new Rectangle(andraIndice * andraAnchoBarra + andraAnchoBarra * 0.22, andraAlto - andraAlto2, andraAnchoBarra * 0.56, andraAlto2);
                    andraRectangulo.setArcWidth(12);
                    andraRectangulo.setArcHeight(12);
                    andraRectangulo.getStyleClass().add("bar");
                    if (andraDia.andraHoy()) {
                        andraRectangulo.getStyleClass().add("now");
                    }
                    Tooltip.install(andraRectangulo, new Tooltip(AndraIdiomaServicio.andraT(andraDia.andraEtiqueta()) + ": " + AndraMontos.andraSoles(andraDia.andraMonto())));
                    Text andraTexto = new Text();
                    andraTexto.textProperty().bind(AndraIdiomaServicio.andraEnlace(andraDia.andraEtiqueta()));
                    andraTexto.getStyleClass().add("bl");
                    andraTexto.setTextAlignment(TextAlignment.CENTER);
                    andraTexto.setWrappingWidth(andraAnchoBarra);
                    andraTexto.setX(andraIndice * andraAnchoBarra);
                    andraTexto.setY(andraAlto + 18);
                    andraLienzo.getChildren().addAll(andraRectangulo, andraTexto);
                    if (andraDia.andraMonto().signum() > 0) {
                        Text andraTexto2 = new Text(AndraMontos.andraEntero(andraDia.andraMonto()));
                        andraTexto2.getStyleClass().add("bv");
                        andraTexto2.setTextAlignment(TextAlignment.CENTER);
                        andraTexto2.setWrappingWidth(andraAnchoBarra);
                        andraTexto2.setX(andraIndice * andraAnchoBarra);
                        andraTexto2.setY(andraAlto - andraAlto2 - 6);
                        andraLienzo.getChildren().add(andraTexto2);
                    }
                }
            }
        });
        andraLienzo.setPrefWidth(400);
        andraLienzo.setMinWidth(0);
        andraLienzo.setMinHeight(214);
        andraLienzo.setPrefHeight(214);
        AndraComponentes.andraAccesible(andraLienzo, "Recaudación de los últimos 7 días");
        return andraLienzo;
    }

    public static VBox andraPorMetodo(Map<String, BigDecimal> andraMontos) {
        double andraTotal = Math.max(1, andraMontos.values().stream().mapToDouble(BigDecimal::doubleValue).sum());
        HBox andraBarra = new HBox();
        andraBarra.getStyleClass().add("stack");
        Rectangle andraRecorte = new Rectangle();
        andraRecorte.setArcWidth(14);
        andraRecorte.setArcHeight(14);
        andraRecorte.widthProperty().bind(andraBarra.widthProperty());
        andraRecorte.heightProperty().bind(andraBarra.heightProperty());
        andraBarra.setClip(andraRecorte);
        VBox andraLeyenda = new VBox(8);
        andraMontos.forEach((andraTexto, andraMonto) -> {
            if (andraMonto.signum() <= 0) {
                return;
            }
            String andraClase = "sg-" + andraTexto.toLowerCase();
            Region andraSeg = new Region();
            andraSeg.getStyleClass().add(andraClase);
            andraSeg.prefWidthProperty().bind(andraBarra.widthProperty().multiply(andraMonto.doubleValue() / andraTotal));
            andraSeg.setMinWidth(0);
            Tooltip.install(andraSeg, new Tooltip(andraTexto + ": " + AndraMontos.andraSoles(andraMonto)));
            andraBarra.getChildren().add(andraSeg);
            Region andraPunto = new Region();
            andraPunto.getStyleClass().addAll("leg-dot", andraClase);
            HBox andraFila = new HBox(8, andraPunto, AndraComponentes.andraEtiqueta(andraTexto), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraMonto), "strong"));
            andraFila.setAlignment(Pos.CENTER_LEFT);
            andraLeyenda.getChildren().add(andraFila);
        });
        VBox.setMargin(andraLeyenda, new javafx.geometry.Insets(12, 0, 0, 0));
        return new VBox(andraBarra, andraLeyenda);
    }

    public static VBox andraLeyendaSimple(Map<String, BigDecimal> andraMontos) {
        VBox andraCajaVertical = new VBox(8);
        andraMontos.forEach((andraTexto, andraMonto) -> {
            HBox andraFila = new HBox(8, AndraComponentes.andraEtiqueta(andraTexto), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraMonto), "strong"));
            andraFila.setAlignment(Pos.CENTER_LEFT);
            andraCajaVertical.getChildren().add(andraFila);
        });
        return andraCajaVertical;
    }

    public static VBox andraBarrasHorizontales(List<AndraAnaliticaServicio.AndraBarra> andraBarras, Function<AndraAnaliticaServicio.AndraBarra, String> andraValor,
                                          boolean andraPorcentaje) {
        VBox andraCajaVertical = new VBox(10);
        double andraMaximo = Math.max(1, andraBarras.stream().mapToDouble(AndraAnaliticaServicio.AndraBarra::andraValor).max().orElse(1));
        for (AndraAnaliticaServicio.AndraBarra andraBarra : andraBarras) {
            Label andraEtiqueta = AndraComponentes.andraLiteral(andraBarra.andraEtiqueta(), "hb-l");
            andraEtiqueta.setMinWidth(0);
            andraEtiqueta.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            StackPane andraPista = new StackPane();
            andraPista.getStyleClass().add("hb-t");
            Region andraRelleno = new Region();
            andraRelleno.getStyleClass().add("hb-i");
            double andraFraccion = andraPorcentaje ? andraBarra.andraValor() / 100.0 : andraBarra.andraValor() / andraMaximo;
            andraRelleno.maxWidthProperty().bind(andraPista.widthProperty().multiply(Math.max(0, Math.min(1, andraFraccion))));
            StackPane.setAlignment(andraRelleno, Pos.CENTER_LEFT);
            andraPista.getChildren().add(andraRelleno);
            Label andraNum = AndraComponentes.andraLiteral(andraValor.apply(andraBarra), "strong");
            andraNum.setMinWidth(Region.USE_PREF_SIZE);
            GridPane andraFila = new GridPane();
            ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
            andraRestriccionColumna1.setPercentWidth(52);
            ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
            andraRestriccionColumna2.setPercentWidth(36);
            ColumnConstraints andraRestriccionColumna3 = new ColumnConstraints();
            andraRestriccionColumna3.setPercentWidth(12);
            andraRestriccionColumna3.setHalignment(javafx.geometry.HPos.RIGHT);
            andraFila.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2, andraRestriccionColumna3);
            andraFila.setHgap(10);
            andraFila.add(andraEtiqueta, 0, 0);
            andraFila.add(andraPista, 1, 0);
            andraFila.add(andraNum, 2, 0);
            GridPane.setValignment(andraPista, VPos.CENTER);
            Tooltip.install(andraFila, new Tooltip(andraBarra.andraEtiqueta() + ": " + andraValor.apply(andraBarra)));
            andraCajaVertical.getChildren().add(andraFila);
        }
        return andraCajaVertical;
    }

    public static VBox andraMapaCalorBnps(AndraAnaliticaServicio.AndraAnalisis andraAnalisis) {
        GridPane andraRejilla = new GridPane();
        andraRejilla.setHgap(4);
        andraRejilla.setVgap(4);
        ColumnConstraints andraPrimera = new ColumnConstraints(44);
        andraRejilla.getColumnConstraints().add(andraPrimera);
        for (int andraIndice = 0; andraIndice < 5; andraIndice++) {
            ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
            andraRestriccionColumna.setHgrow(Priority.ALWAYS);
            andraRestriccionColumna.setPercentWidth(-1);
            andraRestriccionColumna.setFillWidth(true);
            andraRejilla.getColumnConstraints().add(andraRestriccionColumna);
            Label andraAlto = AndraComponentes.andraLiteral(AndraCatalogoServicio.ANDRA_FRANJAS.get(andraIndice), "hint");
            andraAlto.setMaxWidth(Double.MAX_VALUE);
            andraAlto.setAlignment(Pos.CENTER);
            andraRejilla.add(andraAlto, andraIndice + 1, 0);
        }
        for (int andraIndice2 = 0; andraIndice2 < 5; andraIndice2++) {
            andraRejilla.add(AndraComponentes.andraEtiqueta(AndraAnaliticaServicio.ANDRA_DIAS[andraIndice2], "hint"), 0, andraIndice2 + 1);
            for (int andraIndice = 0; andraIndice < 5; andraIndice++) {
                int andraValor = andraAnalisis.andraCalor()[andraIndice2][andraIndice];
                int andraNivel = AndraAnaliticaServicio.andraNivel(andraValor, andraAnalisis.andraCalorMaximo());
                Label andraCelda = AndraComponentes.andraLiteral(String.valueOf(andraValor), "hc");
                if (andraNivel >= 3) {
                    andraCelda.getStyleClass().add("dk");
                }
                andraCelda.setStyle("-fx-background-color: " + AndraAnaliticaServicio.ANDRA_RAMPA[andraNivel] + ";");
                andraCelda.setMaxWidth(Double.MAX_VALUE);
                Tooltip.install(andraCelda, new Tooltip(AndraIdiomaServicio.andraT(AndraAnaliticaServicio.ANDRA_DIAS[andraIndice2]) + " " + AndraCatalogoServicio.ANDRA_FRANJAS.get(andraIndice)
                        + ": " + andraValor + " " + AndraIdiomaServicio.andraT("atenciones")));
                andraRejilla.add(andraCelda, andraIndice + 1, andraIndice2 + 1);
            }
        }
        HBox andraLeyenda = new HBox(4, AndraComponentes.andraEtiqueta("Menos", "hint"));
        andraLeyenda.setAlignment(Pos.CENTER_LEFT);
        for (String andraColor : AndraAnaliticaServicio.ANDRA_RAMPA) {
            Region andraRegion = new Region();
            andraRegion.getStyleClass().add("heat-leg-i");
            andraRegion.setStyle("-fx-background-color: " + andraColor + ";");
            andraLeyenda.getChildren().add(andraRegion);
        }
        andraLeyenda.getChildren().add(AndraComponentes.andraEtiqueta("Más", "hint"));
        andraLeyenda.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
        AndraComponentes.andraAccesible(andraRejilla, "Mapa de calor de atenciones");
        return new VBox(andraRejilla, andraLeyenda);
    }
}
