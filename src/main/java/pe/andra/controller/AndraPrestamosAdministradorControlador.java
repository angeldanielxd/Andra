package pe.andra.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.DixonEstadoPrestamo;
import pe.andra.model.DixonPrestamo;
import pe.andra.model.DixonPrestamoActivo;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraPrestamosAdministradorControlador extends AndraVistaBase {

    private final DixonAdministradorServicio andraAdministrador;
    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();

    @Override
    protected List<Node> andraConstruir() {
        List<DixonPrestamo> andraLista = andraAdministrador.dixonPrestamos();
        Map<String, Function<DixonPrestamo, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraPrestamo -> AndraAdministradorFiltros.andraEscuela(andraPrestamo.getDixonPersona()));
        andraCampos.put("tipo", andraPrestamo -> andraPrestamo.getDixonPersona().getDixonTipo());
        andraLista = DixonFiltroServicio.dixonAplicarAvanzado(andraLista, andraFiltros, new DixonFiltroServicio.DixonAccesores<>(andraPrestamo -> null, andraPrestamo -> AndraAdministradorFiltros.andraClave(andraPrestamo.getDixonPersona()), andraCampos));
        Map<String, Predicate<DixonPrestamo>> andraPred = Map.of("rev", andraPrestamo -> andraPrestamo.getDixonEstado() == DixonEstadoPrestamo.EN_REVISION,
                "apr", andraPrestamo -> andraPrestamo.getDixonEstado() == DixonEstadoPrestamo.APROBADO, "back", andraPrestamo -> andraPrestamo.getDixonEstado() == DixonEstadoPrestamo.DEVUELTO, "lab", andraAdministrador::dixonEsLaboratorio);
        andraLista = DixonFiltroServicio.dixonAplicar(andraLista, andraFiltros, andraPred, andraPrestamo -> andraPrestamo.getDixonNombre() + " " + andraPrestamo.getDixonCodigo() + " " + andraPrestamo.getDixonPersona().getDixonNombre());
        List<DixonPrestamo> andraCurso = andraLista.stream().filter(andraPrestamo -> andraPrestamo.getDixonEstado().dixonEsEnCurso()).toList();
        List<DixonPrestamo> andraResto = andraLista.stream().filter(andraPrestamo -> !andraPrestamo.getDixonEstado().dixonEsEnCurso()).toList();
        VBox andraSeccion1 = new VBox();
        andraSeccion1.getStyleClass().addAll("section", "sin-arriba");
        andraSeccion1.setMaxWidth(1168);
        Optional<DixonPrestamoActivo> andraCustodia = andraAdministrador.dixonCustodia();
        if (andraCustodia.isPresent()) {
            DixonPrestamoActivo andraPrestamoActivo = andraCustodia.get();
            HBox andraTit = new HBox(6, AndraComponentes.andraEtiqueta("En custodia · " + andraPrestamoActivo.getDixonEquipo(), "row-title"), AndraComponentes.andraLiteral("(" + andraPrestamoActivo.getDixonCodigo() + ")", "muted"));
            andraTit.setAlignment(Pos.CENTER_LEFT);
            VBox andraTextos = new VBox(2, andraTit, AndraComponentes.andraEtiqueta(andraPrestamoActivo.getDixonPersona().getDixonNombre() + " · " + DixonEquipoServicio.dixonTextoLimite(andraPrestamoActivo.getDixonLimite(), LocalDateTime.now(DixonFechas.dixonReloj())), "row-sub"));
            VBox andraLista2 = AndraComponentes.andraLista(List.of(AndraComponentes.andraFila(andraTextos, AndraComponentes.andraPildora("En uso", "info"),
                    AndraComponentes.andraBotonSm("Registrar devolución", "btn-secondary", null, andraEvento -> andraAcciones.andraDevolverCustodia(this::andraRedibujar)))));
            VBox.setMargin(andraLista2, new Insets(0, 0, 20, 0));
            andraSeccion1.getChildren().add(andraLista2);
        }
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("En curso (" + andraCurso.size() + ")", null, AndraFiltros.andraBoton(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_PRESTAMOS,
                AndraAdministradorFiltros.andraPersona(andraCatalogo, "Código o DNI del solicitante", "Ej. 20240157"), andraNotificador, this::andraRedibujar));
        andraSeccion1.getChildren().addAll(andraCab, AndraFiltros.andraResumen(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_PRESTAMOS, andraCatalogo::dixonNombreEscuela, this::andraRedibujar),
                andraCurso.isEmpty() ? AndraComponentes.andraVacio("fth-box", "Nada en curso", "No hay préstamos por revisar.") : andraLista(andraCurso));
        List<Node> andraNodos = new ArrayList<>(List.of(AndraComponentes.andraPortadaAdministrador("Préstamos de equipos", "Revisa solicitudes, entregas y devoluciones del inventario.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")), andraSeccion1));
        if (!andraResto.isEmpty()) {
            andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Historial", null), andraLista(andraResto)));
        }
        return andraNodos;
    }

    private VBox andraLista(List<DixonPrestamo> andraPrestamos) {
        List<Node> andraFilas = new ArrayList<>();
        for (DixonPrestamo andraAlto : andraPrestamos) {
            HBox andraTit = new HBox(6, AndraComponentes.andraParrafo(andraAlto.getDixonNombre(), "row-title"), AndraComponentes.andraLiteral("(" + andraAlto.getDixonCodigo() + ")", "muted"));
            andraTit.setAlignment(Pos.CENTER_LEFT);
            VBox andraTextos = new VBox(2, andraTit, AndraComponentes.andraParrafo(andraAlto.getDixonPersona().getDixonNombre() + " · " + andraAlto.getDixonDetalle(), "row-sub"));
            List<Node> andraAcc = new ArrayList<>(List.of(AndraComponentes.andraPildora(andraAlto.getDixonEstado().dixonObtenerEtiqueta())));
            if (andraAlto.getDixonEstado() == DixonEstadoPrestamo.EN_REVISION) {
                andraAcc.add(AndraComponentes.andraBotonSm("Aprobar", "btn-primary", null, andraEvento -> andraAcciones.andraAprobarPrestamo(andraAlto, this::andraRedibujar)));
                andraAcc.add(AndraComponentes.andraBotonSm("Rechazar", "btn-danger", null, andraEvento -> andraAcciones.andraRechazarPrestamo(andraAlto, this::andraRedibujar)));
            } else if (andraAlto.getDixonEstado() == DixonEstadoPrestamo.APROBADO) {
                andraAcc.add(AndraComponentes.andraBotonSm("Registrar devolución", "btn-secondary", null, andraEvento -> andraAcciones.andraDevolverPrestamo(andraAlto, this::andraRedibujar)));
            }
            andraFilas.add(AndraComponentes.andraFila(andraTextos, andraAcc.toArray(new Node[0])));
        }
        return AndraComponentes.andraLista(andraFilas);
    }
}
