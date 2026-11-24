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
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraTransaccionesControlador extends AndraVistaBase {

    private final AndraAdministradorServicio andraAdministrador;
    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;
    private final AndraAccionesPago andraAccionesPago;

    private final AndraEstadoFiltro andraFiltros = new AndraEstadoFiltro();

    private static LocalDate andraFecha(AndraPago andraPago) {
        return andraPago.getAndraPagado() == null ? null : andraPago.getAndraPagado().toLocalDate();
    }

    @Override
    protected List<Node> andraConstruir() {
        List<AndraPago> andraBase = andraAdministrador.andraPagos();
        List<AndraPago> andraLista = andraBase.stream().filter(andraPago -> !andraPago.andraEsPendiente() && andraPago.getAndraPagado() != null).toList();
        Map<String, Function<AndraPago, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraPago -> AndraAdministradorFiltros.andraEscuela(andraPago.getAndraPersona()));
        andraCampos.put("tipo", andraPago -> andraPago.getAndraPersona().getAndraTipo());
        andraCampos.put("kind", andraPago -> andraPago.getAndraTipo().name().toLowerCase());
        andraLista = AndraFiltroServicio.andraAplicarAvanzado(andraLista, andraFiltros, new AndraFiltroServicio.AndraAccesores<>(AndraTransaccionesControlador::andraFecha, andraPago -> AndraAdministradorFiltros.andraClave(andraPago.getAndraPersona()), andraCampos));
        Map<String, Predicate<AndraPago>> andraPred = new HashMap<>(AndraFiltroServicio.andraPredicadosFecha(AndraTransaccionesControlador::andraFecha));
        andraPred.put("yape", andraPago -> andraPago.getAndraMetodo() == AndraMetodoPago.YAPE);
        andraPred.put("plin", andraPago -> andraPago.getAndraMetodo() == AndraMetodoPago.PLIN);
        andraPred.put("card", andraPago -> andraPago.getAndraMetodo() != null && andraPago.getAndraMetodo().andraEsTarjeta());
        andraLista = new ArrayList<>(AndraFiltroServicio.andraAplicar(andraLista, andraFiltros, andraPred, andraPago -> andraPago.getAndraCodigo() + " " + andraPago.getAndraConcepto() + " " + andraPago.getAndraOperacion() + " "
                + andraPago.getAndraBoleta() + " " + andraPago.getAndraPersona().getAndraNombre()));
        andraLista.sort(Comparator.comparing(AndraPago::getAndraPagado).reversed());
        List<AndraPago> andraPendientes = andraBase.stream().filter(AndraPago::andraEsPendiente).toList();
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortadaAdministrador("Transacciones", "Todos los pagos de Andra Pay: matrículas, documentos, carnés y otros derechos.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")));
        AndraRejilla andraIndicadores = new AndraRejilla(3, 220, 16);
        andraIndicadores.getChildren().addAll(AndraComponentes.andraIndicador("Total en la vista", AndraMontos.andraSoles(AndraPagoServicio.andraTotal(andraLista.stream().filter(AndraPago::andraEsPagado).toList())), "success",
                        andraLista.size() + " transacciones"),
                AndraComponentes.andraIndicador("Órdenes por cobrar", String.valueOf(andraPendientes.size()), "warning", AndraMontos.andraSoles(AndraPagoServicio.andraTotal(andraPendientes)) + " pendientes"),
                AndraComponentes.andraIndicador("Reembolsos", String.valueOf(andraBase.stream().filter(andraPago -> andraPago.getAndraEstado() == AndraEstadoPago.REEMBOLSADO).count()), null, "En la sede elegida"));
        VBox andraSeccion0 = AndraComponentes.andraSeccion(andraIndicadores);
        andraSeccion0.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion0);
        List<AndraFiltros.AndraAvanzado> andraAvanzados = AndraAdministradorFiltros.andraFechas("Desde", "Hasta");
        andraAvanzados.add(AndraFiltros.AndraAvanzado.andraTexto("code", "Código o DNI del usuario", "Ej. 20230418"));
        andraAvanzados.add(AndraFiltros.AndraAvanzado.andraSelector("fac", "Escuela profesional", "Todas las escuelas", AndraAdministradorFiltros.andraEscuelas(andraCatalogo)));
        andraAvanzados.add(AndraFiltros.AndraAvanzado.andraSelector("kind", "Concepto", "Todos", List.of(AndraOpcion.andraDe("matricula", "Matrícula"), AndraOpcion.andraDe("documento", "Documento"),
                AndraOpcion.andraDe("carne", "Carné"), AndraOpcion.andraDe("tramite", "Trámite"))));
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Pagos confirmados", null, AndraFiltros.andraBoton(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_PAGOS, andraAvanzados, andraNotificador, this::andraRedibujar));
        Node andraCuerpo;
        if (andraLista.isEmpty()) {
            andraCuerpo = AndraComponentes.andraVacio("fth-file", "Sin transacciones", "Cambia de sede o quita filtros.");
        } else {
            List<Node[]> andraFilas = new ArrayList<>();
            for (AndraPago andraPago : andraLista) {
                Circle andraPunto = new Circle(4.5);
                andraPunto.getStyleClass().add("pm-dot");
                if (andraPago.getAndraMetodo() == AndraMetodoPago.YAPE) {
                    andraPunto.getStyleClass().add("pm-yape");
                } else if (andraPago.getAndraMetodo() == AndraMetodoPago.PLIN) {
                    andraPunto.getStyleClass().add("pm-plin");
                }
                HBox andraMetodo = new HBox(6, andraPunto, AndraComponentes.andraLiteral(andraPago.getAndraMetodo().andraObtenerEtiqueta(), "small"));
                andraMetodo.setAlignment(Pos.CENTER_LEFT);
                HBox andraBotones = new HBox(4, AndraComponentes.andraBotonSm("Ver", "btn-tertiary", null, andraEvento -> andraAccionesPago.andraComprobante(andraPago.getAndraCodigo())));
                if (andraPago.andraEsPagado()) {
                    andraBotones.getChildren().add(AndraComponentes.andraBotonSm("Reembolsar", "btn-danger", null, andraEvento -> andraAcciones.andraReembolsar(andraPago, this::andraRedibujar)));
                }
                andraFilas.add(new Node[]{AndraTabla.andraCelda(AndraFechas.andraDmy(andraPago.getAndraPagado().toLocalDate()), AndraFechas.andraHm(andraPago.getAndraPagado())), AndraTabla.andraCelda(andraPago.getAndraOperacion(), andraPago.getAndraBoleta()),
                        AndraTabla.andraCelda(andraPago.getAndraPersona().getAndraNombre(), andraPago.getAndraPersona().getAndraTipo()), AndraComponentes.andraParrafo(andraPago.getAndraConcepto(), "small"), andraMetodo,
                        AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraPago.andraObtenerTotal()), "strong"),
                        andraPago.getAndraEstado() == AndraEstadoPago.REEMBOLSADO ? AndraComponentes.andraPildora("Reembolsado", "neutral") : AndraComponentes.andraPildora("Confirmado", "success"), andraBotones});
            }
            andraCuerpo = AndraTabla.andraCrear(new String[]{"Fecha", "Operación", "Usuario", "Concepto", "Método", "Monto", "Estado", ""},
                    new double[]{10, 13, 17, 19, 10, 9, 10, 12}, new boolean[]{false, false, false, false, false, true, false, true}, andraFilas);
        }
        andraNodos.add(AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_PAGOS, andraCatalogo::andraNombreEscuela, this::andraRedibujar), andraCuerpo));
        Node andraPorCobrar;
        if (andraPendientes.isEmpty()) {
            andraPorCobrar = AndraComponentes.andraVacio("fth-check", "Nada pendiente", "Todas las órdenes están pagadas.");
        } else {
            List<Node> andraFilas = new ArrayList<>();
            andraPendientes.forEach(andraPago -> andraFilas.add(AndraComponentes.andraFila(AndraComponentes.andraTextosFila(andraPago.getAndraConcepto(), andraPago.getAndraCodigo() + " · " + andraPago.getAndraPersona().getAndraNombre()
                    + " · vence " + AndraFechas.andraDmy(andraPago.getAndraVence())), AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraPago.andraObtenerTotal()), "strong"), AndraComponentes.andraPildora("Pendiente", "warning"))));
            andraPorCobrar = AndraComponentes.andraLista(andraFilas);
        }
        andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Órdenes por cobrar", null), andraPorCobrar));
        return andraNodos;
    }
}
