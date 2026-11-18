package pe.andra.controller;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.DixonPersona;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonMontos;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraAnalisisControlador extends AndraVistaBase {

    private final DixonAnaliticaServicio andraAnalitica;
    private final DixonContextoServicio andraContexto;
    private final DixonCarneServicio andraCarne;

    private String andraPeriodo = "30";
    private TextField andraValidar;
    private VBox andraResultado;

    @Override
    protected List<Node> andraConstruir() {
        DixonAnaliticaServicio.DixonAnalisis andraAnalisisA = andraAnalitica.dixonAnalizar(andraPeriodo);
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortadaAdministrador("Análisis", "Datos para decidir: qué se pide, cuándo se saturan las ventanillas y qué espacios se aprovechan poco.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")));
        AndraSegmentado andraPer = new AndraSegmentado(true).andraOpcion("7", "7 días", null, null).andraOpcion("30", "30 días", null, null)
                .andraOpcion("sem", "Semestre", null, null).andraSeleccionar(andraPeriodo);
        andraPer.andraAlCambiar(andraValor -> {
            andraPeriodo = andraValor;
            andraRedibujar();
        });
        andraPer.setMaxWidth(420);
        AndraComponentes.andraAccesible(andraPer, "Periodo");
        VBox.setMargin(andraPer, new javafx.geometry.Insets(0, 0, 20, 0));
        AndraRejilla andraIndicadores = new AndraRejilla(4, 200, 16);
        andraIndicadores.getChildren().addAll(AndraComponentes.andraIndicador("Trámites y órdenes", String.valueOf(andraAnalisisA.dixonTramitesYOrdenes()), null, "En el periodo"),
                AndraComponentes.andraIndicador("Recaudación", DixonMontos.dixonSoles(andraAnalisisA.dixonRecaudacion()), null, "Pagos confirmados"),
                AndraComponentes.andraIndicador("Atención promedio", "4 h 20 min", null, "Del pago a \"listo para recoger\""),
                AndraComponentes.andraIndicador("Uso de espacios", andraAnalisisA.dixonUsoPromedio() + " %", null, "Promedio semanal"));
        VBox andraSeccion0 = AndraComponentes.andraSeccion(andraPer, andraIndicadores);
        andraSeccion0.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion0);
        VBox andraLista = new VBox(6);
        for (DixonAnaliticaServicio.DixonHallazgo andraAlto : andraAnalisisA.dixonHallazgos()) {
            TextFlow andraFlujoTexto = new TextFlow();
            if (!andraAlto.dixonAntes().isBlank()) {
                andraFlujoTexto.getChildren().add(AndraComponentes.andraTexto(andraAlto.dixonAntes()));
            }
            if (!andraAlto.dixonNegrita().isBlank()) {
                Text andraTexto = AndraComponentes.andraTexto(andraAlto.dixonNegrita());
                andraTexto.setStyle("-fx-font-weight: 700;");
                andraFlujoTexto.getChildren().add(andraTexto);
            }
            if (!andraAlto.dixonDespues().isBlank()) {
                andraFlujoTexto.getChildren().add(AndraComponentes.andraTexto(andraAlto.dixonDespues()));
            }
            andraFlujoTexto.getChildren().forEach(andraNodo -> andraNodo.setStyle((andraNodo.getStyle() == null ? "" : andraNodo.getStyle()) + "-fx-font-size: 14.5px;"));
            andraLista.getChildren().add(new HBox(8, AndraComponentes.andraLiteral("•"), andraFlujoTexto));
        }
        VBox andraTextos = new VBox(8, AndraComponentes.andraParrafo("Lo que dicen los datos", "h4"), andraLista);
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        HBox andraHallazgos = new HBox(new FontIcon("fth-zap"), andraTextos);
        andraHallazgos.getStyleClass().add("insights");
        andraNodos.add(AndraComponentes.andraSeccion(andraHallazgos));
        VBox andraSuperior = andraPanel("Trámites más pedidos", "Cantidad de solicitudes en el periodo");
        andraSuperior.getChildren().add(andraAnalisisA.dixonSuperior().isEmpty() ? AndraComponentes.andraParrafo("Sin datos en el periodo.", "hint")
                : AndraGraficos.andraBarrasHorizontales(andraAnalisisA.dixonSuperior(), andraBarraB -> String.valueOf((int) andraBarraB.dixonValor()), false));
        VBox andraSedes = andraPanel("Recaudación por sede", "Soles, pagos confirmados");
        andraSedes.getChildren().add(AndraGraficos.andraBarrasHorizontales(andraAnalisisA.dixonPorSede(), andraBarraB -> DixonMontos.dixonSoles(andraBarraB.dixonValor()), false));
        andraNodos.add(AndraComponentes.andraSeccion(AndraPanelControlador.andraDosColumnas(andraSuperior, andraSedes, 50)));
        VBox andraCalor = andraPanel("Horas punta en Mesa de Partes", "Atenciones estimadas por día y franja");
        andraCalor.getChildren().add(AndraGraficos.andraMapaCalorBnps(andraAnalisisA));
        VBox andraUso = andraPanel("Uso de espacios", "% del horario semanal ocupado");
        andraUso.getChildren().addAll(AndraComponentes.andraMayus("Subutilizados", "label-caps"), AndraGraficos.andraBarrasHorizontales(andraAnalisisA.dixonSubutilizados(), andraBarraB -> (int) andraBarraB.dixonValor() + " %", true),
                AndraComponentes.andraMayus("Más demandados", "label-caps"), AndraGraficos.andraBarrasHorizontales(andraAnalisisA.dixonDemandados(), andraBarraB -> (int) andraBarraB.dixonValor() + " %", true));
        andraNodos.add(AndraComponentes.andraSeccion(AndraPanelControlador.andraDosColumnas(andraCalor, andraUso, 50)));
        andraNodos.add(AndraComponentes.andraSeccion(andraValidador()));
        return andraNodos;
    }

    private VBox andraPanel(String andraTitulo, String andraSub) {
        VBox andraCajaVertical = new VBox(10, AndraComponentes.andraParrafo(andraTitulo, "h4"), AndraComponentes.andraParrafo(andraSub, "hint"));
        andraCajaVertical.getStyleClass().addAll("panel", "compacto");
        return andraCajaVertical;
    }

    private VBox andraValidador() {
        andraValidar = new TextField();
        AndraComponentes.andraIndicacion(andraValidar, "ANDRA-ID|… o 20240157");
        AndraComponentes.andraAccesible(andraValidar, "Código del carné");
        HBox.setHgrow(andraValidar, Priority.ALWAYS);
        andraResultado = new VBox();
        Button andraBoton = AndraComponentes.andraBoton("Validar", "btn-primary", null, andraEvento -> andraMostrar());
        andraValidar.setOnAction(andraEvento -> andraMostrar());
        HBox andraFila = new HBox(andraValidar, andraBoton);
        andraFila.getStyleClass().add("copy-row");
        VBox andraCajaVertical = andraPanel("Validar carné digital", "Pega el contenido del QR o escribe el código o DNI del estudiante.");
        andraCajaVertical.getChildren().addAll(andraFila, andraResultado);
        return andraCajaVertical;
    }

    private void andraMostrar() {
        DixonCarneServicio.DixonVerificacion andraVerificacion = andraCarne.dixonVerificar(andraValidar.getText());
        VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraVerificacion.dixonMensaje(), "strong"));
        DixonPersona andraPersona = andraVerificacion.dixonPersona();
        if (andraPersona != null) {
            andraTextos.getChildren().add(AndraComponentes.andraLiteralParrafo(andraPersona.getDixonNombre() + " · " + andraPersona.getDixonTipo() + " · " + andraPersona.getDixonCodigo()
                    + (andraPersona.getDixonEscuela() != null ? " · EP " + andraPersona.getDixonEscuela().getDixonNombre() : ""), "small"));
        }
        HBox andraCaja = new HBox(new FontIcon(andraVerificacion.dixonCorrecto() ? "fth-check-circle" : "fth-alert-triangle"), andraTextos);
        andraCaja.getStyleClass().addAll("vc", andraVerificacion.dixonCorrecto() ? "ok" : "no");
        andraCaja.setAlignment(Pos.TOP_LEFT);
        andraResultado.getChildren().setAll(andraCaja);
        AndraMovimiento.andraSubir(andraCaja);
    }
}
