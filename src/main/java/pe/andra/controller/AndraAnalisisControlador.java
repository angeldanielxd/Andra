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
import pe.andra.model.AndraPersona;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraMontos;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraAnalisisControlador extends AndraVistaBase {

    private final AndraAnaliticaServicio andraAnalitica;
    private final AndraContextoServicio andraContexto;
    private final AndraCarneServicio andraCarne;

    private String andraPeriodo = "30";
    private TextField andraValidar;
    private VBox andraResultado;

    @Override
    protected List<Node> andraConstruir() {
        AndraAnaliticaServicio.AndraAnalisis andraAnalisisA = andraAnalitica.andraAnalizar(andraPeriodo);
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortadaAdministrador("Análisis", "Datos para decidir: qué se pide, cuándo se saturan las ventanillas y qué espacios se aprovechan poco.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")));
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
        andraIndicadores.getChildren().addAll(AndraComponentes.andraIndicador("Trámites y órdenes", String.valueOf(andraAnalisisA.andraTramitesYOrdenes()), null, "En el periodo"),
                AndraComponentes.andraIndicador("Recaudación", AndraMontos.andraSoles(andraAnalisisA.andraRecaudacion()), null, "Pagos confirmados"),
                AndraComponentes.andraIndicador("Atención promedio", "4 h 20 min", null, "Del pago a \"listo para recoger\""),
                AndraComponentes.andraIndicador("Uso de espacios", andraAnalisisA.andraUsoPromedio() + " %", null, "Promedio semanal"));
        VBox andraSeccion0 = AndraComponentes.andraSeccion(andraPer, andraIndicadores);
        andraSeccion0.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion0);
        VBox andraLista = new VBox(6);
        for (AndraAnaliticaServicio.AndraHallazgo andraAlto : andraAnalisisA.andraHallazgos()) {
            TextFlow andraFlujoTexto = new TextFlow();
            if (!andraAlto.andraAntes().isBlank()) {
                andraFlujoTexto.getChildren().add(AndraComponentes.andraTexto(andraAlto.andraAntes()));
            }
            if (!andraAlto.andraNegrita().isBlank()) {
                Text andraTexto = AndraComponentes.andraTexto(andraAlto.andraNegrita());
                andraTexto.setStyle("-fx-font-weight: 700;");
                andraFlujoTexto.getChildren().add(andraTexto);
            }
            if (!andraAlto.andraDespues().isBlank()) {
                andraFlujoTexto.getChildren().add(AndraComponentes.andraTexto(andraAlto.andraDespues()));
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
        andraSuperior.getChildren().add(andraAnalisisA.andraSuperior().isEmpty() ? AndraComponentes.andraParrafo("Sin datos en el periodo.", "hint")
                : AndraGraficos.andraBarrasHorizontales(andraAnalisisA.andraSuperior(), andraBarraB -> String.valueOf((int) andraBarraB.andraValor()), false));
        VBox andraSedes = andraPanel("Recaudación por sede", "Soles, pagos confirmados");
        andraSedes.getChildren().add(AndraGraficos.andraBarrasHorizontales(andraAnalisisA.andraPorSede(), andraBarraB -> AndraMontos.andraSoles(andraBarraB.andraValor()), false));
        andraNodos.add(AndraComponentes.andraSeccion(AndraPanelControlador.andraDosColumnas(andraSuperior, andraSedes, 50)));
        VBox andraCalor = andraPanel("Horas punta en Mesa de Partes", "Atenciones estimadas por día y franja");
        andraCalor.getChildren().add(AndraGraficos.andraMapaCalorBnps(andraAnalisisA));
        VBox andraUso = andraPanel("Uso de espacios", "% del horario semanal ocupado");
        andraUso.getChildren().addAll(AndraComponentes.andraMayus("Subutilizados", "label-caps"), AndraGraficos.andraBarrasHorizontales(andraAnalisisA.andraSubutilizados(), andraBarraB -> (int) andraBarraB.andraValor() + " %", true),
                AndraComponentes.andraMayus("Más demandados", "label-caps"), AndraGraficos.andraBarrasHorizontales(andraAnalisisA.andraDemandados(), andraBarraB -> (int) andraBarraB.andraValor() + " %", true));
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
        AndraCarneServicio.AndraVerificacion andraVerificacion = andraCarne.andraVerificar(andraValidar.getText());
        VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraVerificacion.andraMensaje(), "strong"));
        AndraPersona andraPersona = andraVerificacion.andraPersona();
        if (andraPersona != null) {
            andraTextos.getChildren().add(AndraComponentes.andraLiteralParrafo(andraPersona.getAndraNombre() + " · " + andraPersona.getAndraTipo() + " · " + andraPersona.getAndraCodigo()
                    + (andraPersona.getAndraEscuela() != null ? " · EP " + andraPersona.getAndraEscuela().getAndraNombre() : ""), "small"));
        }
        HBox andraCaja = new HBox(new FontIcon(andraVerificacion.andraCorrecto() ? "fth-check-circle" : "fth-alert-triangle"), andraTextos);
        andraCaja.getStyleClass().addAll("vc", andraVerificacion.andraCorrecto() ? "ok" : "no");
        andraCaja.setAlignment(Pos.TOP_LEFT);
        andraResultado.getChildren().setAll(andraCaja);
        AndraMovimiento.andraSubir(andraCaja);
    }
}
