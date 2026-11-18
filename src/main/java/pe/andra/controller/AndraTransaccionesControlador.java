package pe.andra.controller;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraTransaccionesControlador extends AndraVistaBase {

    private final DixonAdministradorServicio andraAdministrador;
    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;
    private final AndraAccionesPago andraAccionesPago;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();

    private static LocalDate andraFecha(DixonPago andraPago) {
        return andraPago.getDixonPagado() == null ? null : andraPago.getDixonPagado().toLocalDate();
    }

    @Override
    protected List<Node> andraConstruir() {
        List<DixonPago> andraBase = andraAdministrador.dixonPagos();
        List<DixonPago> andraLista = andraBase.stream().filter(andraPago -> !andraPago.dixonEsPendiente() && andraPago.getDixonPagado() != null).toList();
        Map<String, Function<DixonPago, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraPago -> AndraAdministradorFiltros.andraEscuela(andraPago.getDixonPersona()));
        andraCampos.put("tipo", andraPago -> andraPago.getDixonPersona().getDixonTipo());
        andraCampos.put("kind", andraPago -> andraPago.getDixonTipo().name().toLowerCase());
        andraLista = DixonFiltroServicio.dixonAplicarAvanzado(andraLista, andraFiltros, new DixonFiltroServicio.DixonAccesores<>(AndraTransaccionesControlador::andraFecha, andraPago -> AndraAdministradorFiltros.andraClave(andraPago.getDixonPersona()), andraCampos));
        Map<String, Predicate<DixonPago>> andraPred = new HashMap<>(DixonFiltroServicio.dixonPredicadosFecha(AndraTransaccionesControlador::andraFecha));
        andraPred.put("yape", andraPago -> andraPago.getDixonMetodo() == DixonMetodoPago.YAPE);
        andraPred.put("plin", andraPago -> andraPago.getDixonMetodo() == DixonMetodoPago.PLIN);
        andraPred.put("card", andraPago -> andraPago.getDixonMetodo() != null && andraPago.getDixonMetodo().dixonEsTarjeta());
        andraLista = new ArrayList<>(DixonFiltroServicio.dixonAplicar(andraLista, andraFiltros, andraPred, andraPago -> andraPago.getDixonCodigo() + " " + andraPago.getDixonConcepto() + " " + andraPago.getDixonOperacion() + " "
                + andraPago.getDixonBoleta() + " " + andraPago.getDixonPersona().getDixonNombre()));
        andraLista.sort(Comparator.comparing(DixonPago::getDixonPagado).reversed());
        List<DixonPago> andraPendientes = andraBase.stream().filter(DixonPago::dixonEsPendiente).toList();
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortadaAdministrador("Transacciones", "Todos los pagos de Andra Pay: matrículas, documentos, carnés y otros derechos.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")));
        AndraRejilla andraIndicadores = new AndraRejilla(3, 220, 16);
        andraIndicadores.getChildren().addAll(AndraComponentes.andraIndicador("Total en la vista", DixonMontos.dixonSoles(DixonPagoServicio.dixonTotal(andraLista.stream().filter(DixonPago::dixonEsPagado).toList())), "success",
                        andraLista.size() + " transacciones"),
                AndraComponentes.andraIndicador("Órdenes por cobrar", String.valueOf(andraPendientes.size()), "warning", DixonMontos.dixonSoles(DixonPagoServicio.dixonTotal(andraPendientes)) + " pendientes"),
                AndraComponentes.andraIndicador("Reembolsos", String.valueOf(andraBase.stream().filter(andraPago -> andraPago.getDixonEstado() == DixonEstadoPago.REEMBOLSADO).count()), null, "En la sede elegida"));
        VBox andraSeccion0 = AndraComponentes.andraSeccion(andraIndicadores);
        andraSeccion0.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion0);
        List<AndraFiltros.AndraAvanzado> andraAvanzados = AndraAdministradorFiltros.andraFechas("Desde", "Hasta");
        andraAvanzados.add(AndraFiltros.AndraAvanzado.andraTexto("code", "Código o DNI del usuario", "Ej. 20230418"));
        andraAvanzados.add(AndraFiltros.AndraAvanzado.andraSelector("fac", "Escuela profesional", "Todas las escuelas", AndraAdministradorFiltros.andraEscuelas(andraCatalogo)));
        andraAvanzados.add(AndraFiltros.AndraAvanzado.andraSelector("kind", "Concepto", "Todos", List.of(AndraOpcion.andraDe("matricula", "Matrícula"), AndraOpcion.andraDe("documento", "Documento"),
                AndraOpcion.andraDe("carne", "Carné"), AndraOpcion.andraDe("tramite", "Trámite"))));
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Pagos confirmados", null, AndraFiltros.andraBoton(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_PAGOS, andraAvanzados, andraNotificador, this::andraRedibujar));
        Node andraCuerpo;
        if (andraLista.isEmpty()) {
            andraCuerpo = AndraComponentes.andraVacio("fth-file", "Sin transacciones", "Cambia de sede o quita filtros.");
        } else {
            List<Node[]> andraFilas = new ArrayList<>();
            for (DixonPago andraPago : andraLista) {
                Circle andraPunto = new Circle(4.5);
                andraPunto.getStyleClass().add("pm-dot");
                if (andraPago.getDixonMetodo() == DixonMetodoPago.YAPE) {
                    andraPunto.getStyleClass().add("pm-yape");
                } else if (andraPago.getDixonMetodo() == DixonMetodoPago.PLIN) {
                    andraPunto.getStyleClass().add("pm-plin");
                }
                HBox andraMetodo = new HBox(6, andraPunto, AndraComponentes.andraLiteral(andraPago.getDixonMetodo().dixonObtenerEtiqueta(), "small"));
                andraMetodo.setAlignment(Pos.CENTER_LEFT);
                HBox andraBotones = new HBox(4, AndraComponentes.andraBotonSm("Ver", "btn-tertiary", null, andraEvento -> andraAccionesPago.andraComprobante(andraPago.getDixonCodigo())));
                if (andraPago.dixonEsPagado()) {
                    andraBotones.getChildren().add(AndraComponentes.andraBotonSm("Reembolsar", "btn-danger", null, andraEvento -> andraAcciones.andraReembolsar(andraPago, this::andraRedibujar)));
                }
                andraFilas.add(new Node[]{AndraTabla.andraCelda(DixonFechas.dixonDmy(andraPago.getDixonPagado().toLocalDate()), DixonFechas.dixonHm(andraPago.getDixonPagado())), AndraTabla.andraCelda(andraPago.getDixonOperacion(), andraPago.getDixonBoleta()),
                        AndraTabla.andraCelda(andraPago.getDixonPersona().getDixonNombre(), andraPago.getDixonPersona().getDixonTipo()), AndraComponentes.andraParrafo(andraPago.getDixonConcepto(), "small"), andraMetodo,
                        AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "strong"),
                        andraPago.getDixonEstado() == DixonEstadoPago.REEMBOLSADO ? AndraComponentes.andraPildora("Reembolsado", "neutral") : AndraComponentes.andraPildora("Confirmado", "success"), andraBotones});
            }
            andraCuerpo = AndraTabla.andraCrear(new String[]{"Fecha", "Operación", "Usuario", "Concepto", "Método", "Monto", "Estado", ""},
                    new double[]{10, 13, 17, 19, 10, 9, 10, 12}, new boolean[]{false, false, false, false, false, true, false, true}, andraFilas);
        }
        andraNodos.add(AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_PAGOS, andraCatalogo::dixonNombreEscuela, this::andraRedibujar), andraCuerpo));
        Node andraPorCobrar;
        if (andraPendientes.isEmpty()) {
            andraPorCobrar = AndraComponentes.andraVacio("fth-check", "Nada pendiente", "Todas las órdenes están pagadas.");
        } else {
            List<Node> andraFilas = new ArrayList<>();
            andraPendientes.forEach(andraPago -> andraFilas.add(AndraComponentes.andraFila(AndraComponentes.andraTextosFila(andraPago.getDixonConcepto(), andraPago.getDixonCodigo() + " · " + andraPago.getDixonPersona().getDixonNombre()
                    + " · vence " + DixonFechas.dixonDmy(andraPago.getDixonVence())), AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "strong"), AndraComponentes.andraPildora("Pendiente", "warning"))));
            andraPorCobrar = AndraComponentes.andraLista(andraFilas);
        }
        andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Órdenes por cobrar", null), andraPorCobrar));
        return andraNodos;
    }
}
