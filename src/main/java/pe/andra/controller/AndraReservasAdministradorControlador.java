package pe.andra.controller;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.DixonEstadoReserva;
import pe.andra.model.DixonReserva;
import pe.andra.model.DixonTipoEspacio;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraReservasAdministradorControlador extends AndraVistaBase {

    private final DixonAdministradorServicio andraAdministrador;
    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();

    @Override
    protected List<Node> andraConstruir() {
        List<DixonReserva> andraLista = andraAdministrador.dixonReservas();
        Map<String, Function<DixonReserva, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraReserva -> AndraAdministradorFiltros.andraEscuela(andraReserva.getDixonPersona()));
        andraCampos.put("tipo", andraReserva -> andraReserva.getDixonPersona().getDixonTipo());
        andraLista = DixonFiltroServicio.dixonAplicarAvanzado(andraLista, andraFiltros, new DixonFiltroServicio.DixonAccesores<>(DixonReserva::getDixonFecha, andraReserva -> AndraAdministradorFiltros.andraClave(andraReserva.getDixonPersona()), andraCampos));
        LocalDate andraHoy = DixonFechas.dixonHoy();
        Map<String, Predicate<DixonReserva>> andraPred = Map.of("today", andraReserva -> andraReserva.getDixonFecha().equals(andraHoy),
                "d7", andraReserva -> !andraReserva.getDixonFecha().isBefore(andraHoy) && !andraReserva.getDixonFecha().isAfter(andraHoy.plusDays(7)),
                "big", andraReserva -> andraReserva.getDixonAsistentes() >= 100, "aud", andraReserva -> andraReserva.getDixonEspacio().getDixonTipo() == DixonTipoEspacio.AUDITORIO);
        andraLista = DixonFiltroServicio.dixonAplicar(andraLista, andraFiltros, andraPred, andraReserva -> andraReserva.getDixonEspacio().getDixonNombre() + " " + andraReserva.getDixonMotivo() + " " + andraReserva.getDixonPersona().getDixonNombre());
        List<DixonReserva> andraPendientes = andraLista.stream().filter(andraReserva -> andraReserva.getDixonEstado() == DixonEstadoReserva.PENDIENTE).toList();
        List<DixonReserva> andraResto = andraLista.stream().filter(andraReserva -> andraReserva.getDixonEstado() != DixonEstadoReserva.PENDIENTE).toList();
        List<AndraFiltros.AndraAvanzado> andraAvanzados = AndraAdministradorFiltros.andraFechas("Fecha desde", "Fecha hasta");
        andraAvanzados.addAll(AndraAdministradorFiltros.andraPersona(andraCatalogo, "Código o DNI del solicitante", "Ej. 20240157"));
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Por aprobar (" + andraPendientes.size() + ")", null,
                AndraFiltros.andraBoton(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_RESERVAS, andraAvanzados, andraNotificador, this::andraRedibujar));
        VBox andraSeccion1 = AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_RESERVAS, andraCatalogo::dixonNombreEscuela, this::andraRedibujar),
                andraPendientes.isEmpty() ? AndraComponentes.andraVacio("fth-calendar", "No hay reservas por aprobar", "Las nuevas solicitudes aparecerán aquí.") : andraLista(andraPendientes));
        andraSeccion1.getStyleClass().add("sin-arriba");
        List<Node> andraNodos = new ArrayList<>(List.of(AndraComponentes.andraPortadaAdministrador("Reservas de espacios",
                "Aprueba o rechaza las solicitudes de aulas, laboratorios, salas, talleres y auditorios.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")), andraSeccion1));
        if (!andraResto.isEmpty()) {
            andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Historial", null), andraLista(andraResto)));
        }
        return andraNodos;
    }

    private VBox andraLista(List<DixonReserva> andraReservas) {
        List<Node> andraFilas = new ArrayList<>();
        for (DixonReserva andraReserva : andraReservas) {
            VBox andraTextos = new VBox(2, AndraComponentes.andraLiteralParrafo(andraReserva.getDixonEspacio().getDixonNombre(), "row-title"),
                    AndraComponentes.andraEtiqueta(andraReserva.getDixonPersona().getDixonNombre() + " · " + DixonFechas.dixonDmy(andraReserva.getDixonFecha()) + " · " + andraReserva.getDixonHorario() + " · " + andraReserva.getDixonAsistentes()
                            + " personas · Sede " + andraReserva.getDixonEspacio().getDixonSede().getDixonNombre(), "row-sub"),
                    AndraComponentes.andraParrafo(andraReserva.getDixonMotivo(), "row-sub"));
            List<Node> andraAcc = new ArrayList<>(List.of(AndraComponentes.andraPildora(andraReserva.getDixonEstado().dixonObtenerEtiqueta())));
            if (andraReserva.getDixonEstado() == DixonEstadoReserva.PENDIENTE) {
                andraAcc.add(AndraComponentes.andraBotonSm("Aprobar", "btn-primary", null, andraEvento -> andraAcciones.andraAprobarReserva(andraReserva, this::andraRedibujar)));
                andraAcc.add(AndraComponentes.andraBotonSm("Rechazar", "btn-danger", null, andraEvento -> andraAcciones.andraRechazarReserva(andraReserva, this::andraRedibujar)));
            }
            andraFilas.add(AndraComponentes.andraFila(andraTextos, andraAcc.toArray(new Node[0])));
        }
        return AndraComponentes.andraLista(andraFilas);
    }
}
