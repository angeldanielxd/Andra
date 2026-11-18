package pe.andra.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AndraPagosControlador extends AndraVistaBase {

    private final DixonPagoServicio andraPagos;
    private final AndraAccionesPago andraAccionesPago;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();
    private String andraPestana = "pend";

    @Override
    protected List<Node> andraConstruir() {
        List<DixonPago> andraTodos = andraPagos.dixonMios();
        List<DixonPago> andraPendientes = andraTodos.stream().filter(DixonPago::dixonEsPendiente).toList();
        List<DixonPago> andraPagados = andraTodos.stream().filter(DixonPago::dixonEsPagado).toList();
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(andraEncabezado(andraPendientes));
        andraNodos.add(andraMatricula());
        andraNodos.add(andraDerechos(andraTodos, andraPendientes, andraPagados));
        andraNodos.add(andraInformacion());
        return andraNodos;
    }

    private Circle andraMancha(double andraRadio, Color andraColor) {
        Circle andraCirculo = new Circle(andraRadio);
        andraCirculo.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, new Stop(0, andraColor), new Stop(0.65, Color.TRANSPARENT)));
        andraCirculo.setManaged(false);
        andraCirculo.setMouseTransparent(true);
        return andraCirculo;
    }

    private StackPane andraEncabezado(List<DixonPago> andraPendientes) {
        Label andraAntetitulo = AndraComponentes.andraLiteral("Andra Pay", "pay-eyebrow");
        Label andraTitulo = AndraComponentes.andraParrafo("Paga tu universidad sin ir al banco.", "pay-titulo");
        andraTitulo.setMaxWidth(560);
        Label andraSub = AndraComponentes.andraParrafo("Matrícula, documentos, carné y otros derechos de pago con Yape, Plin o tarjeta. Tu comprobante llega al instante.", "lead");
        andraSub.setMaxWidth(520);
        FlowPane andraMetodos = new FlowPane(6, 6, AndraComponentes.andraLiteral("Yape", "pm-chip", "y"), AndraComponentes.andraLiteral("Plin", "pm-chip", "p"),
                AndraComponentes.andraLiteral("Visa", "pm-chip"), AndraComponentes.andraLiteral("Mastercard", "pm-chip"), AndraComponentes.andraLiteral("American Express", "pm-chip"),
                AndraComponentes.andraLiteral("Diners Club", "pm-chip"));
        AndraComponentes.andraAccesible(andraMetodos, "Métodos aceptados");
        VBox andraIzquierda = new VBox(12, andraAntetitulo, andraTitulo, andraSub, andraMetodos);
        VBox.setMargin(andraMetodos, new Insets(6, 0, 0, 0));
        HBox.setHgrow(andraIzquierda, Priority.ALWAYS);
        int andraCantidad = andraPendientes.size();
        VBox andraSaldo = new VBox(AndraComponentes.andraMayus("Por pagar", "label-caps"), AndraComponentes.andraLiteral(DixonMontos.dixonSoles(DixonPagoServicio.dixonTotal(andraPendientes)), "pay-balance-valor"),
                AndraComponentes.andraEtiqueta(andraCantidad + " derecho" + (andraCantidad == 1 ? "" : "s") + " de pago pendiente" + (andraCantidad == 1 ? "" : "s"), "hint"));
        andraSaldo.getStyleClass().add("pay-balance");
        Optional<DixonPago> andraProximo = andraPagos.dixonMasProximo();
        if (andraProximo.isPresent()) {
            DixonPago andraPago = andraProximo.get();
            andraSaldo.getChildren().addAll(AndraComponentes.andraBloque(AndraComponentes.andraBoton("Pagar lo más próximo · " + DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "btn-primary", null,
                    andraEvento -> andraAccionesPago.andraPantalla(andraPago.getDixonCodigo(), this::andraRedibujar))), AndraComponentes.andraParrafo("Vence el " + DixonFechas.dixonDmy(andraPago.getDixonVence()) + " · " + andraPago.getDixonConcepto(), "hint"));
        } else {
            Label andraAldia = AndraComponentes.andraEtiqueta("Estás al día", "hint");
            andraAldia.setGraphic(AndraComponentes.andraIcono("fth-check", "verde"));
            andraSaldo.getChildren().add(andraAldia);
        }
        HBox andraPagado = new HBox(AndraComponentes.andraEtiqueta("Pagado en 2026"), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraPagos.dixonPagadoEnAnio(2026)), "strong"));
        andraPagado.getStyleClass().add("pb-row");
        andraSaldo.getChildren().add(andraPagado);
        andraSaldo.setPrefWidth(400);
        andraSaldo.setMinWidth(320);
        HBox andraContenido = new HBox(40, andraIzquierda, andraSaldo);
        andraContenido.getStyleClass().add("pay-hero-in");
        andraContenido.setAlignment(Pos.CENTER_LEFT);
        Circle andraAzul = andraMancha(280, Color.rgb(0, 113, 227, 0.14));
        Circle andraVioleta = andraMancha(210, Color.rgb(116, 34, 132, 0.08));
        Pane andraManchas = new Pane(andraAzul, andraVioleta);
        andraManchas.setMouseTransparent(true);
        andraManchas.widthProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> {
            andraAzul.setCenterX(andraNumeroB.doubleValue() - 160);
            andraAzul.setCenterY(100);
            andraVioleta.setCenterX(andraNumeroB.doubleValue() - 640);
            andraVioleta.setCenterY(andraManchas.getHeight() + 50);
        });
        andraManchas.heightProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> andraVioleta.setCenterY(andraNumeroB.doubleValue() + 50));
        StackPane andraCentrado = new StackPane(andraContenido);
        andraCentrado.setAlignment(Pos.CENTER);
        StackPane andraBanda = new StackPane(andraManchas, andraCentrado);
        andraBanda.getStyleClass().add("pay-hero");
        Rectangle andraRecorte = new Rectangle();
        andraRecorte.widthProperty().bind(andraBanda.widthProperty());
        andraRecorte.heightProperty().bind(andraBanda.heightProperty());
        andraBanda.setClip(andraRecorte);
        andraBanda.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(andraBanda, new Insets(0, 0, 16, 0));
        return andraBanda;
    }

    private VBox andraMatricula() {
        DixonMatricula andraMatricula = andraPagos.dixonMatricula();
        DixonPago andraPago = andraMatricula.getDixonPago();
        boolean andraCorrecto = andraMatricula.dixonEsConfirmada();
        VBox andraTextos = new VBox(4, AndraComponentes.andraMayus("Matrícula " + andraMatricula.getDixonPeriodo(), "label-caps"),
                AndraComponentes.andraParrafo(andraCorrecto ? "Tu matrícula está confirmada" : "Completa tu matrícula con el pago", "h2"),
                AndraComponentes.andraParrafo(andraCorrecto ? "Pagaste " + DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()) + " con " + andraPago.getDixonMetodo().dixonObtenerEtiqueta() + " · Comprobante " + andraPago.getDixonBoleta()
                        : "Ya llenaste tu ficha en el sistema académico de la institución. Tu derecho de pago llegó a Andra Pay: págalo aquí y tu matrícula queda confirmada al instante.", "muted", "small"));
        andraTextos.setMaxWidth(620);
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        Button andraAccion = andraCorrecto ? AndraComponentes.andraBoton("Ver comprobante", "btn-secondary", "fth-file", andraEvento -> andraAccionesPago.andraComprobante(andraPago.getDixonCodigo()))
                : AndraComponentes.andraBoton("Pagar matrícula · " + DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "btn-primary", "fth-credit-card", andraEvento -> andraAccionesPago.andraPantalla(andraPago.getDixonCodigo(), this::andraRedibujar));
        HBox andraCab = new HBox(20, andraTextos, andraAccion);
        andraCab.setAlignment(Pos.TOP_LEFT);
        AndraRejilla andraPasos = new AndraRejilla(3, 220, 10);
        andraPasos.getChildren().addAll(andraPaso("done", null, "Ficha de matrícula", "Completada en el sistema académico"),
                andraPaso(andraCorrecto ? "done" : "cur", "2", "Derecho de pago", andraCorrecto ? "Pagado en Andra Pay" : "Pendiente · vence el " + DixonFechas.dixonDmy(andraPago.getDixonVence())),
                andraPaso(andraCorrecto ? "done" : "", "3", "Matrícula confirmada", "Constancia disponible en Documentos"));
        VBox andraTarjeta = new VBox(andraCab, andraPasos);
        andraTarjeta.getStyleClass().add("mat-card");
        if (andraCorrecto) {
            andraTarjeta.getStyleClass().add("done");
        }
        return AndraComponentes.andraSeccion(andraTarjeta);
    }

    private HBox andraPaso(String andraEstado, String andraNumero, String andraTitulo, String andraDetalle) {
        StackPane andraNum = new StackPane("done".equals(andraEstado) ? new FontIcon("fth-check") : AndraComponentes.andraLiteral(andraNumero));
        andraNum.getStyleClass().add("mat-num");
        VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraTitulo, "strong", "small"), AndraComponentes.andraParrafo(andraDetalle, "hint"));
        HBox andraAlto = new HBox(andraNum, andraTextos);
        andraAlto.getStyleClass().add("mat-step");
        if (!andraEstado.isEmpty()) {
            andraAlto.getStyleClass().add(andraEstado);
        }
        return andraAlto;
    }

    private VBox andraDerechos(List<DixonPago> andraTodos, List<DixonPago> andraPendientes, List<DixonPago> andraPagados) {
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Derechos de pago", "Todo lo que debes pagar a la universidad, en un solo lugar.",
                AndraFiltros.andraBoton(andraFiltros, DixonPagoServicio.DIXON_PREDEFINIDOS, null, andraNotificador, this::andraRedibujar),
                AndraComponentes.andraBotonSm("Nuevo pago", "btn-primary", "fth-plus", andraEvento -> andraAccionesPago.andraNuevoPago(this::andraRedibujar)));
        AndraSegmentado andraPestanas = new AndraSegmentado(true).andraOpcion("pend", "Por pagar", null, (long) andraPendientes.size())
                .andraOpcion("paid", "Pagados", null, (long) andraPagados.size()).andraOpcion("all", "Todos", null, (long) andraTodos.size()).andraSeleccionar(andraPestana);
        andraPestanas.andraAlCambiar(andraValor -> {
            andraPestana = andraValor;
            andraRedibujar();
        });
        andraPestanas.setMaxWidth(460);
        AndraComponentes.andraAccesible(andraPestanas, "Estado");
        VBox.setMargin(andraPestanas, new Insets(0, 0, 16, 0));
        List<DixonPago> andraLista = "pend".equals(andraPestana) ? andraPendientes : "paid".equals(andraPestana) ? andraPagados : andraTodos;
        andraLista = new ArrayList<>(DixonFiltroServicio.dixonAplicar(andraLista, andraFiltros, andraPagos.dixonPredicados(), andraPago -> andraPago.getDixonConcepto() + " " + andraPago.getDixonCodigo() + " " + (andraPago.getDixonBoleta() == null ? "" : andraPago.getDixonBoleta())));
        andraLista.sort(Comparator.comparing((DixonPago andraPago) -> andraPago.getDixonPagado() != null ? andraPago.getDixonPagado() : andraPago.getDixonCreado()).reversed());
        Node andraCuerpo;
        if (andraLista.isEmpty()) {
            andraCuerpo = AndraComponentes.andraVacio("fth-credit-card", "pend".equals(andraPestana) ? "No tienes pagos pendientes" : "No hay pagos con esos filtros",
                    "Cuando solicites un documento, carné o matrícula, su derecho de pago aparecerá aquí.");
        } else {
            VBox andraFilas = new VBox(10);
            andraLista.forEach(andraPago -> andraFilas.getChildren().add(andraFila(andraPago)));
            andraCuerpo = andraFilas;
        }
        return AndraComponentes.andraSeccion(andraCab, andraPestanas, AndraFiltros.andraResumen(andraFiltros, DixonPagoServicio.DIXON_PREDEFINIDOS, null, this::andraRedibujar), andraCuerpo);
    }

    private HBox andraFila(DixonPago andraPago) {
        StackPane andraIcono = new StackPane(new FontIcon(andraPago.getDixonTipo().dixonObtenerIcono()));
        andraIcono.getStyleClass().add("pr-ico");
        String andraDetalle = andraPago.getDixonCodigo() + " · " + andraPago.getDixonTipo().dixonObtenerEtiqueta() + (andraPago.dixonEsPagado() && andraPago.getDixonPagado() != null
                ? " · Pagado el " + DixonFechas.dixonDmy(andraPago.getDixonPagado().toLocalDate()) + " con " + andraPago.getDixonMetodo().dixonObtenerEtiqueta()
                : andraPago.getDixonVence() != null ? " · Vence el " + DixonFechas.dixonDmy(andraPago.getDixonVence()) : "");
        VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraPago.getDixonConcepto(), "strong"), AndraComponentes.andraEtiqueta(andraDetalle, "hint"));
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        String andraEstado = DixonPagoServicio.dixonEstadoVisible(andraPago);
        HBox andraAlto = new HBox(andraIcono, andraTextos, AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "pr-amt"), AndraComponentes.andraPildora(andraEstado, DixonPagoServicio.dixonTonoEstado(andraEstado)),
                AndraComponentes.andraIcono("fth-arrow-right", "muted"));
        andraAlto.getStyleClass().add("pay-row");
        andraAlto.setFocusTraversable(true);
        AndraComponentes.andraAccesible(andraAlto, andraPago.getDixonConcepto() + " · " + DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()) + " · " + andraEstado);
        Runnable andraAbrir = () -> {
            if (andraPago.dixonEsPendiente()) {
                andraAccionesPago.andraDetalle(andraPago.getDixonCodigo(), this::andraRedibujar);
            } else {
                andraAccionesPago.andraComprobante(andraPago.getDixonCodigo());
            }
        };
        andraAlto.setOnMouseClicked(andraEvento -> andraAbrir.run());
        andraAlto.setOnKeyPressed(andraEvento -> {
            if (andraEvento.getCode() == javafx.scene.input.KeyCode.ENTER) {
                andraAbrir.run();
            }
        });
        return andraAlto;
    }

    private VBox andraInformacion() {
        AndraRejilla andraRejilla = new AndraRejilla(3, 240);
        String[][] andraDatos = {{"fth-award", "Matrícula", "Llena tu ficha en el sistema académico. El derecho de pago aparece aquí automáticamente."},
                {"fth-file-text", "Documentos", "Constancias, certificados y récord se generan con tus datos y se recogen, por lo general, el mismo día."},
                {"fth-credit-card", "Carné universitario", "Solicítalo con tu foto, págalo y recógelo en la Mesa de Partes de tu sede."}};
        for (String[] andraTextos : andraDatos) {
            StackPane andraCapa = new StackPane(new FontIcon(andraTextos[0]));
            andraCapa.getStyleClass().add("sicon-sm");
            VBox andraCajaVertical = new VBox(12, andraCapa, AndraComponentes.andraParrafo(andraTextos[1], "h3"), AndraComponentes.andraParrafo(andraTextos[2], "muted", "small"));
            andraCajaVertical.getStyleClass().add("office");
            andraRejilla.getChildren().add(andraCajaVertical);
        }
        Label andraNota = AndraComponentes.andraParrafo("Prototipo: los pagos son simulados y no generan cargos reales.", "hint");
        andraNota.setMaxWidth(Double.MAX_VALUE);
        andraNota.setAlignment(Pos.CENTER);
        VBox.setMargin(andraNota, new Insets(16, 0, 0, 0));
        return AndraComponentes.andraSeccion(andraRejilla, andraNota);
    }
}
