package pe.andra.controller;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.AndraEstadoReserva;
import pe.andra.model.AndraReserva;
import pe.andra.model.AndraTipoEspacio;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraFechas;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraReservasAdministradorControlador extends AndraVistaBase {

    private final AndraAdministradorServicio andraAdministrador;
    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;

    private final AndraEstadoFiltro andraFiltros = new AndraEstadoFiltro();

    @Override
    protected List<Node> andraConstruir() {
        List<AndraReserva> andraLista = andraAdministrador.andraReservas();
        Map<String, Function<AndraReserva, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraReserva -> AndraAdministradorFiltros.andraEscuela(andraReserva.getAndraPersona()));
        andraCampos.put("tipo", andraReserva -> andraReserva.getAndraPersona().getAndraTipo());
        andraLista = AndraFiltroServicio.andraAplicarAvanzado(andraLista, andraFiltros, new AndraFiltroServicio.AndraAccesores<>(AndraReserva::getAndraFecha, andraReserva -> AndraAdministradorFiltros.andraClave(andraReserva.getAndraPersona()), andraCampos));
        LocalDate andraHoy = AndraFechas.andraHoy();
        Map<String, Predicate<AndraReserva>> andraPred = Map.of("today", andraReserva -> andraReserva.getAndraFecha().equals(andraHoy),
                "d7", andraReserva -> !andraReserva.getAndraFecha().isBefore(andraHoy) && !andraReserva.getAndraFecha().isAfter(andraHoy.plusDays(7)),
                "big", andraReserva -> andraReserva.getAndraAsistentes() >= 100, "aud", andraReserva -> andraReserva.getAndraEspacio().getAndraTipo() == AndraTipoEspacio.AUDITORIO);
        andraLista = AndraFiltroServicio.andraAplicar(andraLista, andraFiltros, andraPred, andraReserva -> andraReserva.getAndraEspacio().getAndraNombre() + " " + andraReserva.getAndraMotivo() + " " + andraReserva.getAndraPersona().getAndraNombre());
        List<AndraReserva> andraPendientes = andraLista.stream().filter(andraReserva -> andraReserva.getAndraEstado() == AndraEstadoReserva.PENDIENTE).toList();
        List<AndraReserva> andraResto = andraLista.stream().filter(andraReserva -> andraReserva.getAndraEstado() != AndraEstadoReserva.PENDIENTE).toList();
        List<AndraFiltros.AndraAvanzado> andraAvanzados = AndraAdministradorFiltros.andraFechas("Fecha desde", "Fecha hasta");
        andraAvanzados.addAll(AndraAdministradorFiltros.andraPersona(andraCatalogo, "Código o DNI del solicitante", "Ej. 20240157"));
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Por aprobar (" + andraPendientes.size() + ")", null,
                AndraFiltros.andraBoton(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_RESERVAS, andraAvanzados, andraNotificador, this::andraRedibujar));
        VBox andraSeccion1 = AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_RESERVAS, andraCatalogo::andraNombreEscuela, this::andraRedibujar),
                andraPendientes.isEmpty() ? AndraComponentes.andraVacio("fth-calendar", "No hay reservas por aprobar", "Las nuevas solicitudes aparecerán aquí.") : andraLista(andraPendientes));
        andraSeccion1.getStyleClass().add("sin-arriba");
        List<Node> andraNodos = new ArrayList<>(List.of(AndraComponentes.andraPortadaAdministrador("Reservas de espacios",
                "Aprueba o rechaza las solicitudes de aulas, laboratorios, salas, talleres y auditorios.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")), andraSeccion1));
        if (!andraResto.isEmpty()) {
            andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Historial", null), andraLista(andraResto)));
        }
        return andraNodos;
    }

    private VBox andraLista(List<AndraReserva> andraReservas) {
        List<Node> andraFilas = new ArrayList<>();
        for (AndraReserva andraReserva : andraReservas) {
            VBox andraTextos = new VBox(2, AndraComponentes.andraLiteralParrafo(andraReserva.getAndraEspacio().getAndraNombre(), "row-title"),
                    AndraComponentes.andraEtiqueta(andraReserva.getAndraPersona().getAndraNombre() + " · " + AndraFechas.andraDmy(andraReserva.getAndraFecha()) + " · " + andraReserva.getAndraHorario() + " · " + andraReserva.getAndraAsistentes()
                            + " personas · Sede " + andraReserva.getAndraEspacio().getAndraSede().getAndraNombre(), "row-sub"),
                    AndraComponentes.andraParrafo(andraReserva.getAndraMotivo(), "row-sub"));
            List<Node> andraAcc = new ArrayList<>(List.of(AndraComponentes.andraPildora(andraReserva.getAndraEstado().andraObtenerEtiqueta())));
            if (andraReserva.getAndraEstado() == AndraEstadoReserva.PENDIENTE) {
                andraAcc.add(AndraComponentes.andraBotonSm("Aprobar", "btn-primary", null, andraEvento -> andraAcciones.andraAprobarReserva(andraReserva, this::andraRedibujar)));
                andraAcc.add(AndraComponentes.andraBotonSm("Rechazar", "btn-danger", null, andraEvento -> andraAcciones.andraRechazarReserva(andraReserva, this::andraRedibujar)));
            }
            andraFilas.add(AndraComponentes.andraFila(andraTextos, andraAcc.toArray(new Node[0])));
        }
        return AndraComponentes.andraLista(andraFilas);
    }
}
