package pe.andra.controller;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraPanelControlador extends AndraVistaBase {

    private final AndraAdministradorServicio andraAdministrador;
    private final AndraSesionServicio andraSesion;
    private final AndraContextoServicio andraContexto;
    private final AndraAccionesAdministrador andraAcciones;
    private final AndraAccionesPago andraAccionesPago;

    @Override
    protected List<Node> andraConstruir() {
        AndraAdministradorServicio.AndraPanel andraPanel = andraAdministrador.andraPanel();
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortadaAdministrador("Hola, " + andraSesion.andraAdministrador().andraObtenerPrimerNombre(),
                AndraFechas.andraLargo(AndraFechas.andraHoy()) + " · Esto es lo que pasa hoy en Andra.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")));
        AndraRejilla andraIndicadores = new AndraRejilla(4, 200, 16);
        andraIndicadores.getChildren().addAll(AndraComponentes.andraIndicador("Recaudado hoy", AndraMontos.andraSoles(andraPanel.andraHoy()), "success", "Pagos confirmados"),
                AndraComponentes.andraIndicador("Recaudado en el mes", AndraMontos.andraSoles(andraPanel.andraMes()), null, AndraFechas.andraMesAnio(AndraFechas.andraHoy())),
                AndraComponentes.andraIndicador("Trámites por atender", String.valueOf(andraPanel.andraPorAtender().size()), "warning", "Pagados o en revisión"),
                AndraComponentes.andraIndicador("Reservas y préstamos", String.valueOf(andraPanel.andraReservasPendientes().size() + andraPanel.andraPrestamosEnRevision().size()), "warning",
                        andraPanel.andraReservasPendientes().size() + " reservas · " + andraPanel.andraPrestamosEnRevision().size() + " préstamos"));
        VBox andraSeccion0 = AndraComponentes.andraSeccion(andraIndicadores);
        andraSeccion0.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion0);
        VBox andraBarras = new VBox(AndraComponentes.andraCabeceraSeccion("Recaudación de los últimos 7 días", "En soles, pagos confirmados",
                AndraComponentes.andraBotonSm("Ver transacciones", "btn-secondary", null, andraEvento -> andraIr("a-pagos"))), AndraGraficos.andraBarrasDias(andraPanel.andraDias()));
        andraBarras.getStyleClass().addAll("panel", "compacto");
        VBox andraMetodos = new VBox(14, AndraComponentes.andraParrafo("Por método de pago", "h4"), AndraGraficos.andraPorMetodo(andraPanel.andraPorMetodo()),
                AndraComponentes.andraParrafo("Por concepto", "h4"), AndraGraficos.andraLeyendaSimple(andraPanel.andraPorConcepto()));
        andraMetodos.getStyleClass().addAll("panel", "compacto");
        andraNodos.add(AndraComponentes.andraSeccion(andraDosColumnas(andraBarras, andraMetodos, 62)));
        List<Node> andraAtencion = new ArrayList<>();
        andraPanel.andraPorAtender().stream().limit(4).forEach(andraExpediente -> andraAtencion.add(AndraComponentes.andraFila(AndraComponentes.andraTextosFila(andraExpediente.getAndraTitulo(),
                        andraExpediente.getAndraPersona().getAndraNombre() + " · " + andraExpediente.getAndraCodigo() + " · " + andraExpediente.getAndraEstado()),
                AndraComponentes.andraBotonSm("Atender", "btn-primary", null, andraEvento -> andraAcciones.andraRevisar(andraExpediente.getAndraCodigo(), this::andraRedibujar)))));
        andraPanel.andraReservasPendientes().stream().limit(3).forEach(andraReserva -> andraAtencion.add(AndraComponentes.andraFila(AndraComponentes.andraTextosFila("Reserva · " + andraReserva.getAndraEspacio().getAndraNombre(),
                        andraReserva.getAndraPersona().getAndraNombre() + " · " + AndraFechas.andraDmy(andraReserva.getAndraFecha()) + " " + andraReserva.getAndraHorario()),
                AndraComponentes.andraBotonSm("Aprobar", "btn-secondary", null, andraEvento -> andraAcciones.andraAprobarReserva(andraReserva, this::andraRedibujar)),
                AndraComponentes.andraBotonSm("Rechazar", "btn-danger", null, andraEvento -> andraAcciones.andraRechazarReserva(andraReserva, this::andraRedibujar)))));
        if (andraAtencion.isEmpty()) {
            andraAtencion.add(AndraComponentes.andraFila(AndraComponentes.andraEtiqueta("Todo al día.", "muted")));
        }
        List<Node> andraRecientes = new ArrayList<>();
        for (AndraPago andraPago : andraPanel.andraRecientes()) {
            Circle andraPunto = new Circle(4.5);
            andraPunto.getStyleClass().add("pm-dot");
            if (andraPago.getAndraMetodo() == AndraMetodoPago.YAPE) {
                andraPunto.getStyleClass().add("pm-yape");
            } else if (andraPago.getAndraMetodo() == AndraMetodoPago.PLIN) {
                andraPunto.getStyleClass().add("pm-plin");
            }
            VBox andraTextos = AndraComponentes.andraTextosFila(andraPago.getAndraConcepto(), andraPago.getAndraPersona().getAndraNombre() + " · " + andraPago.getAndraMetodo().andraObtenerEtiqueta() + " · "
                    + AndraFechas.andraHm(andraPago.getAndraPagado()) + " " + AndraFechas.andraDmy(andraPago.getAndraPagado().toLocalDate()));
            HBox andraPrincipal = new HBox(12, andraPunto, andraTextos);
            andraPrincipal.setAlignment(Pos.CENTER_LEFT);
            HBox andraFila = AndraComponentes.andraFila(andraPrincipal, AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraPago.andraObtenerTotal()), "strong"));
            andraFila.getStyleClass().add("clic");
            andraFila.setOnMouseClicked(andraEvento -> andraAccionesPago.andraComprobante(andraPago.getAndraCodigo()));
            andraRecientes.add(andraFila);
        }
        VBox andraIzq = new VBox(AndraComponentes.andraCabeceraSeccion("Requieren tu atención", null), AndraComponentes.andraLista(andraAtencion));
        VBox andraDer = new VBox(AndraComponentes.andraCabeceraSeccion("Últimos pagos", null), AndraComponentes.andraLista(andraRecientes));
        andraNodos.add(AndraComponentes.andraSeccion(andraDosColumnas(andraIzq, andraDer, 58)));
        return andraNodos;
    }

    static GridPane andraDosColumnas(Node andraNodoA, Node andraNodoB, double andraPorcentaje) {
        GridPane andraRejilla = new GridPane();
        andraRejilla.setHgap(20);
        andraRejilla.setVgap(20);
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(andraPorcentaje);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(100 - andraPorcentaje);
        andraRejilla.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        andraRejilla.add(andraNodoA, 0, 0);
        andraRejilla.add(andraNodoB, 1, 0);
        GridPane.setValignment(andraNodoA, javafx.geometry.VPos.TOP);
        GridPane.setValignment(andraNodoB, javafx.geometry.VPos.TOP);
        return andraRejilla;
    }
}
