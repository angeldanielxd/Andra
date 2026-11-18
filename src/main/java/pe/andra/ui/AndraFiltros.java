package pe.andra.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.service.DixonEstadoFiltro;
import pe.andra.service.DixonFiltroServicio;
import pe.andra.util.DixonFechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class AndraFiltros {

    public record AndraAvanzado(String andraId, String andraTipo, String andraEtiqueta, String andraMarcador, List<AndraOpcion> andraOpciones, boolean andraAncho) {
        public static AndraAvanzado andraFecha(String andraId, String andraEtiqueta) {
            return new AndraAvanzado(andraId, "date", andraEtiqueta, null, List.of(), false);
        }

        public static AndraAvanzado andraTexto(String andraId, String andraEtiqueta, String andraMarcador) {
            return new AndraAvanzado(andraId, "text", andraEtiqueta, andraMarcador, List.of(), true);
        }

        public static AndraAvanzado andraSelector(String andraId, String andraEtiqueta, String andraMarcador, List<AndraOpcion> andraOpciones) {
            return new AndraAvanzado(andraId, "select", andraEtiqueta, andraMarcador, andraOpciones, false);
        }
    }

    private static final Map<String, String> ANDRA_NOMBRES = Map.of("from", "Desde", "to", "Hasta", "code", "Código",
            "fac", "Escuela", "tipo", "Tipo", "kind", "Concepto");

    private AndraFiltros() {
    }

    public static Button andraBoton(DixonEstadoFiltro andraEstado, List<String[]> andraPredefinidos, List<AndraAvanzado> andraAvanzados, AndraNotificador andraNotificador,
                               Runnable andraAlCambiar) {
        Button andraBoton = AndraComponentes.andraBotonSm("Filtros", "btn-secondary", "fth-filter", null);
        int andraCantidad = andraEstado.dixonCantidad();
        if (andraCantidad > 0) {
            andraBoton.getStyleClass().add("is-on");
            Label andraEtiqueta = AndraComponentes.andraLiteral(String.valueOf(andraCantidad), "flt-count");
            HBox andraConTexto = new HBox(6, new FontIcon("fth-filter"), AndraComponentes.andraEtiqueta("Filtros"), andraEtiqueta);
            andraConTexto.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            andraBoton.textProperty().unbind();
            andraBoton.setText(null);
            andraBoton.setGraphic(andraConTexto);
        }
        andraBoton.setOnAction(andraEvento -> andraNotificador.andraEmergente(andraPanel(andraEstado, andraPredefinidos, andraAvanzados, andraNotificador, andraAlCambiar), andraBoton));
        return andraBoton;
    }

    private static VBox andraPanel(DixonEstadoFiltro andraEstado, List<String[]> andraPredefinidos, List<AndraAvanzado> andraAvanzados, AndraNotificador andraNotificador,
                              Runnable andraAlCambiar) {
        VBox andraCaja = new VBox(4);
        andraCaja.getStyleClass().add("pop");
        if (andraAvanzados != null && !andraAvanzados.isEmpty()) {
            andraCaja.getStyleClass().add("ancho");
        }
        andraCaja.setPadding(new Insets(14));
        andraCaja.getChildren().add(AndraComponentes.andraEtiqueta("Filtros rápidos", "pop-group"));
        FlowPane andraFichas = AndraComponentes.andraFichas();
        for (String[] andraTextos : andraPredefinidos) {
            ToggleButton andraFicha = new ToggleButton();
            AndraComponentes.andraFijar(andraFicha, andraTextos[1]);
            andraFicha.getStyleClass().add("chip");
            andraFicha.setSelected(andraEstado.dixonObtenerActivos().contains(andraTextos[0]));
            andraFicha.setOnAction(andraEvento -> {
                andraEstado.dixonAlternar(andraTextos[0]);
                andraAlCambiar.run();
            });
            andraFichas.getChildren().add(andraFicha);
        }
        andraCaja.getChildren().add(andraFichas);
        Map<String, AndraCampo<?>> andraCampos = new LinkedHashMap<>();
        Label andraErrorRango = AndraComponentes.andraParrafo("", "err");
        andraErrorRango.setVisible(false);
        andraErrorRango.setManaged(false);
        if (andraAvanzados != null && !andraAvanzados.isEmpty()) {
            andraCaja.getChildren().add(AndraComponentes.andraEtiqueta("Búsqueda avanzada", "pop-group"));
            GridPane andraRejilla = new GridPane();
            andraRejilla.getStyleClass().add("flt-adv");
            andraRejilla.setHgap(12);
            andraRejilla.setVgap(10);
            ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
            andraRestriccionColumna1.setPercentWidth(50);
            ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
            andraRestriccionColumna2.setPercentWidth(50);
            andraRejilla.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
            int andraFila = 0;
            int andraCol = 0;
            for (AndraAvanzado andraAvanzado : andraAvanzados) {
                AndraCampo<?> andraCampo = switch (andraAvanzado.andraTipo()) {
                    case "date" -> {
                        String andraValor = andraEstado.dixonAvanzado(andraAvanzado.andraId());
                        yield AndraFormulario.andraFecha(andraAvanzado.andraEtiqueta(), andraValor == null ? null : LocalDate.parse(andraValor), null, null);
                    }
                    case "select" -> AndraFormulario.andraSelector(andraAvanzado.andraEtiqueta(), andraAvanzado.andraOpciones(), andraAvanzado.andraMarcador() == null ? "Todos" : andraAvanzado.andraMarcador(),
                            andraEstado.dixonAvanzado(andraAvanzado.andraId()));
                    default -> AndraFormulario.andraTexto(andraAvanzado.andraEtiqueta(), andraAvanzado.andraMarcador(), andraEstado.dixonAvanzado(andraAvanzado.andraId()));
                };
                andraCampos.put(andraAvanzado.andraId(), andraCampo);
                if (andraAvanzado.andraAncho()) {
                    if (andraCol != 0) {
                        andraFila++;
                        andraCol = 0;
                    }
                    andraRejilla.add(andraCampo.andraNodo(), 0, andraFila, 2, 1);
                    andraFila++;
                } else {
                    andraRejilla.add(andraCampo.andraNodo(), andraCol, andraFila);
                    andraCol++;
                    if (andraCol > 1) {
                        andraCol = 0;
                        andraFila++;
                    }
                }
            }
            andraCaja.getChildren().addAll(andraRejilla, andraErrorRango);
        }
        andraCaja.getChildren().add(AndraComponentes.andraEtiqueta("Tu propio filtro", "pop-group"));
        TextField andraPropio = new TextField(andraEstado.dixonObtenerTexto());
        AndraComponentes.andraIndicacion(andraPropio, "Escribe una palabra o código…");
        AndraComponentes.andraAccesible(andraPropio, "Escribe un filtro");
        HBox.setHgrow(andraPropio, Priority.ALWAYS);
        Button andraAplicar = AndraComponentes.andraBotonSm("Aplicar", "btn-primary", null, null);
        andraAplicar.setDefaultButton(true);
        HBox andraFila = new HBox(andraPropio, andraAplicar);
        andraFila.getStyleClass().add("copy-row");
        andraCaja.getChildren().add(andraFila);
        int andraCantidad = andraEstado.dixonCantidad();
        Label andraResumen = AndraComponentes.andraEtiqueta(andraCantidad == 0 ? "Sin filtros activos"
                : andraCantidad + " filtro" + (andraCantidad > 1 ? "s" : "") + " activo" + (andraCantidad > 1 ? "s" : ""), "hint");
        Button andraLimpiar = AndraComponentes.andraBotonSm("Limpiar", "btn-tertiary", null, andraEvento -> {
            andraEstado.dixonLimpiar();
            andraNotificador.andraCerrarEmergente();
            andraAlCambiar.run();
            andraNotificador.andraInformacion("Filtros quitados.");
        });
        andraLimpiar.setDisable(andraCantidad == 0);
        HBox andraPie = new HBox(andraResumen, AndraComponentes.andraEspacio(), andraLimpiar);
        andraPie.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        andraPie.setPadding(new Insets(10, 0, 0, 0));
        andraCaja.getChildren().add(andraPie);
        andraAplicar.setOnAction(andraEvento -> {
            Map<String, String> andraValores = new LinkedHashMap<>();
            andraCampos.forEach((andraTexto, andraCampo2) -> andraValores.put(andraTexto, andraCampo2.andraTexto()));
            String andraError = DixonFiltroServicio.dixonValidarRango(andraValores.get("from"), andraValores.get("to"));
            if (andraError != null) {
                AndraComponentes.andraFijar(andraErrorRango, andraError);
                andraErrorRango.setVisible(true);
                andraErrorRango.setManaged(true);
                andraCampos.get("to").andraControl().requestFocus();
                return;
            }
            andraEstado.dixonObtenerAvanzados().clear();
            andraValores.forEach((andraTexto, andraValor) -> {
                if (andraValor != null && !andraValor.isBlank()) {
                    andraEstado.dixonObtenerAvanzados().put(andraTexto, andraValor);
                }
            });
            andraEstado.dixonAsignarTexto(andraPropio.getText());
            andraNotificador.andraCerrarEmergente();
            andraAlCambiar.run();
            andraNotificador.andraInformacion(andraEstado.dixonCantidad() > 0 ? "Filtros aplicados." : "Sin filtros activos.");
        });
        return andraCaja;
    }

    public static Node andraResumen(DixonEstadoFiltro andraEstado, List<String[]> andraPredefinidos, Function<String, String> andraNombreEscuela,
                               Runnable andraAlCambiar) {
        List<Node> andraEtiquetas = new ArrayList<>();
        for (String andraId : andraEstado.dixonObtenerActivos()) {
            String andraNombre = andraPredefinidos.stream().filter(andraTextos -> andraTextos[0].equals(andraId)).map(andraTextos -> andraTextos[1]).findFirst().orElse(andraId);
            andraEtiquetas.add(AndraComponentes.andraDistintivo(andraNombre));
        }
        if (!andraEstado.dixonObtenerTexto().isBlank()) {
            andraEtiquetas.add(AndraComponentes.andraLiteral("“" + andraEstado.dixonObtenerTexto() + "”", "tag"));
        }
        andraEstado.dixonObtenerAvanzados().forEach((andraTexto, andraValor) -> {
            if (andraValor == null || andraValor.isBlank()) {
                return;
            }
            String andraValor2 = "fac".equals(andraTexto) && andraNombreEscuela != null ? andraNombreEscuela.apply(andraValor)
                    : andraValor.matches("\\d{4}-\\d{2}-\\d{2}") ? DixonFechas.dixonDmy(LocalDate.parse(andraValor)) : andraValor;
            andraEtiquetas.add(AndraComponentes.andraDistintivo(ANDRA_NOMBRES.getOrDefault(andraTexto, andraTexto) + ": " + andraValor2));
        });
        if (andraEtiquetas.isEmpty()) {
            Region andraVacio = new Region();
            andraVacio.setManaged(false);
            return andraVacio;
        }
        FlowPane andraPanelFlujo = new FlowPane();
        andraPanelFlujo.getStyleClass().add("flt-active");
        andraPanelFlujo.getChildren().addAll(andraEtiquetas);
        Button andraQuitar = AndraComponentes.andraEnlaceFlecha("Quitar filtros", andraEvento -> {
            andraEstado.dixonLimpiar();
            andraAlCambiar.run();
        });
        andraQuitar.setGraphic(null);
        andraPanelFlujo.getChildren().add(andraQuitar);
        return andraPanelFlujo;
    }
}
