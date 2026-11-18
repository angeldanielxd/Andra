package pe.andra.ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import pe.andra.model.*;
import pe.andra.service.DixonCatalogoServicio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class AndraVistaFactura {

    private AndraVistaFactura() {
    }

    public static StackPane andraCrear(String andraCodigo, List<DixonElementoPago> andraElementos, DixonDatosSolicitante andraSol, LocalDate andraVence, DixonSede andraSede,
                                  DixonPago andraPagado, DixonCatalogoServicio andraCatalogo) {
        BigDecimal andraTotal = andraElementos.stream().map(DixonElementoPago::dixonObtenerImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        VBox andraMarca = new VBox(AndraComponentes.andraLiteral("Andra Pay", "strong", "h3"), AndraComponentes.andraLiteral("tus trámites más simples", "hint"));
        HBox andraMarcaFila = new HBox(10, AndraComponentes.andraLogo(36), andraMarca);
        andraMarcaFila.setAlignment(Pos.CENTER_LEFT);
        VBox andraId = new VBox(2, AndraComponentes.andraLiteral(andraPagado != null ? "COMPROBANTE DE PAGO" : "ORDEN DE PAGO", "label-caps"),
                AndraComponentes.andraLiteral(andraPagado != null ? andraPagado.getDixonBoleta() : andraCodigo, "h3"));
        andraId.setAlignment(Pos.TOP_RIGHT);
        if (andraPagado != null) {
            andraId.getChildren().add(AndraComponentes.andraLiteral("Op. " + andraPagado.getDixonOperacion(), "hint"));
        }
        HBox andraCabecera = new HBox(andraMarcaFila, AndraComponentes.andraEspacio(), andraId);
        andraCabecera.getStyleClass().add("inv-head");

        String andraTipo = andraSol.getDixonTipo() + (andraCatalogo.dixonEscuela(andraSol.getDixonEscuela()).isPresent() ? " · EP " + andraCatalogo.dixonNombreEscuela(andraSol.getDixonEscuela()) : "");
        VBox andraIzquierda = new VBox(2, AndraComponentes.andraLiteral("SOLICITANTE", "label-caps"), AndraComponentes.andraLiteral(andraSol.getDixonNombre(), "strong"),
                AndraComponentes.andraLiteral("DNI " + andraSol.getDixonDni() + (andraSol.getDixonCodigo() != null && !andraSol.getDixonCodigo().isBlank() ? " · Código " + andraSol.getDixonCodigo() : ""), "small"),
                AndraComponentes.andraLiteral(andraTipo, "small"));
        VBox andraDerecha = new VBox(2, AndraComponentes.andraLiteral(andraPagado != null ? "FECHA DE PAGO" : "EMITIDA", "label-caps"),
                AndraComponentes.andraLiteral(andraPagado != null ? DixonFechas.dixonDmyHm(andraPagado.getDixonPagado()) : DixonFechas.dixonDmy(DixonFechas.dixonHoy()), "strong"),
                AndraComponentes.andraLiteral(andraPagado != null ? "MÉTODO" : "VENCE", "label-caps"),
                AndraComponentes.andraLiteral(andraPagado != null ? andraPagado.getDixonMetodo().dixonObtenerEtiqueta() : DixonFechas.dixonDmy(andraVence), "strong"));
        VBox.setMargin(andraDerecha.getChildren().get(2), new Insets(8, 0, 0, 0));
        GridPane andraMeta = new GridPane();
        andraMeta.getStyleClass().add("inv-meta");
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(58);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(42);
        andraMeta.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        andraMeta.add(andraIzquierda, 0, 0);
        andraMeta.add(andraDerecha, 1, 0);

        GridPane andraTabla = new GridPane();
        double[] andraAnchos = {52, 12, 18, 18};
        for (double andraValorA : andraAnchos) {
            ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
            andraRestriccionColumna.setPercentWidth(andraValorA);
            andraTabla.getColumnConstraints().add(andraRestriccionColumna);
        }
        andraTabla.getColumnConstraints().get(1).setHalignment(HPos.RIGHT);
        andraTabla.getColumnConstraints().get(2).setHalignment(HPos.RIGHT);
        andraTabla.getColumnConstraints().get(3).setHalignment(HPos.RIGHT);
        String[] andraCab = {"CONCEPTO", "CANT.", "P. UNIT.", "IMPORTE"};
        for (int andraIndice = 0; andraIndice < andraCab.length; andraIndice++) {
            Label andraEtiqueta = AndraComponentes.andraLiteral(andraCab[andraIndice], "inv-th");
            andraTabla.add(andraCelda(andraEtiqueta, andraIndice > 0), andraIndice, 0);
        }
        int andraFila = 1;
        for (DixonElementoPago andraElementoPago : andraElementos) {
            andraTabla.add(andraCelda(AndraComponentes.andraLiteralParrafo(andraElementoPago.getDixonConcepto()), false), 0, andraFila);
            andraTabla.add(andraCelda(AndraComponentes.andraLiteral(String.valueOf(andraElementoPago.getDixonCantidad())), true), 1, andraFila);
            andraTabla.add(andraCelda(AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraElementoPago.getDixonPrecioUnitario())), true), 2, andraFila);
            andraTabla.add(andraCelda(AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraElementoPago.dixonObtenerImporte())), true), 3, andraFila);
            andraFila++;
        }
        HBox andraTotalFila = new HBox(AndraComponentes.andraLiteral("Total a pagar", "strong"), AndraComponentes.andraEspacio(),
                AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraTotal), "strong"));
        andraTotalFila.getStyleClass().add("inv-total");
        andraTotalFila.setPadding(new Insets(12, 6, 0, 6));
        Label andraNota = AndraComponentes.andraLiteralParrafo((andraPagado != null ? "Pago confirmado. " : "") + "Sede de atención: "
                + andraSede.getDixonNombreCompleto() + ". Montos referenciales de demostración; no se realizan cargos reales.", "inv-note");
        VBox andraHoja = new VBox(andraCabecera, andraMeta, andraTabla, andraTotalFila, andraNota);
        StackPane andraContenedor = new StackPane(andraHoja);
        andraContenedor.getStyleClass().add("sheet");
        if (andraPagado != null) {
            Label andraMarcaAgua = AndraComponentes.andraLiteral("DEMOSTRACIÓN", "watermark");
            andraMarcaAgua.setMouseTransparent(true);
            StackPane.setAlignment(andraMarcaAgua, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(andraMarcaAgua, new Insets(0, -10, 30, 0));
            andraContenedor.getChildren().add(andraMarcaAgua);
        }
        return andraContenedor;
    }

    private static Region andraCelda(Label andraEtiqueta, boolean andraDerecha) {
        HBox andraCajaHorizontal = new HBox(andraEtiqueta);
        andraCajaHorizontal.getStyleClass().add("inv-fila");
        andraCajaHorizontal.setAlignment(andraDerecha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        andraCajaHorizontal.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(andraCajaHorizontal, Priority.ALWAYS);
        return andraCajaHorizontal;
    }
}
