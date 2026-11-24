package pe.andra.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.AndraEstadoPrestamo;
import pe.andra.model.AndraPrestamo;
import pe.andra.model.AndraPrestamoActivo;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraFechas;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraPrestamosAdministradorControlador extends AndraVistaBase {

    private final AndraAdministradorServicio andraAdministrador;
    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;

    private final AndraEstadoFiltro andraFiltros = new AndraEstadoFiltro();

    @Override
    protected List<Node> andraConstruir() {
        List<AndraPrestamo> andraLista = andraAdministrador.andraPrestamos();
        Map<String, Function<AndraPrestamo, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraPrestamo -> AndraAdministradorFiltros.andraEscuela(andraPrestamo.getAndraPersona()));
        andraCampos.put("tipo", andraPrestamo -> andraPrestamo.getAndraPersona().getAndraTipo());
        andraLista = AndraFiltroServicio.andraAplicarAvanzado(andraLista, andraFiltros, new AndraFiltroServicio.AndraAccesores<>(andraPrestamo -> null, andraPrestamo -> AndraAdministradorFiltros.andraClave(andraPrestamo.getAndraPersona()), andraCampos));
        Map<String, Predicate<AndraPrestamo>> andraPred = Map.of("rev", andraPrestamo -> andraPrestamo.getAndraEstado() == AndraEstadoPrestamo.EN_REVISION,
                "apr", andraPrestamo -> andraPrestamo.getAndraEstado() == AndraEstadoPrestamo.APROBADO, "back", andraPrestamo -> andraPrestamo.getAndraEstado() == AndraEstadoPrestamo.DEVUELTO, "lab", andraAdministrador::andraEsLaboratorio);
        andraLista = AndraFiltroServicio.andraAplicar(andraLista, andraFiltros, andraPred, andraPrestamo -> andraPrestamo.getAndraNombre() + " " + andraPrestamo.getAndraCodigo() + " " + andraPrestamo.getAndraPersona().getAndraNombre());
        List<AndraPrestamo> andraCurso = andraLista.stream().filter(andraPrestamo -> andraPrestamo.getAndraEstado().andraEsEnCurso()).toList();
        List<AndraPrestamo> andraResto = andraLista.stream().filter(andraPrestamo -> !andraPrestamo.getAndraEstado().andraEsEnCurso()).toList();
        VBox andraSeccion1 = new VBox();
        andraSeccion1.getStyleClass().addAll("section", "sin-arriba");
        andraSeccion1.setMaxWidth(1168);
        Optional<AndraPrestamoActivo> andraCustodia = andraAdministrador.andraCustodia();
        if (andraCustodia.isPresent()) {
            AndraPrestamoActivo andraPrestamoActivo = andraCustodia.get();
            HBox andraTit = new HBox(6, AndraComponentes.andraEtiqueta("En custodia · " + andraPrestamoActivo.getAndraEquipo(), "row-title"), AndraComponentes.andraLiteral("(" + andraPrestamoActivo.getAndraCodigo() + ")", "muted"));
            andraTit.setAlignment(Pos.CENTER_LEFT);
            VBox andraTextos = new VBox(2, andraTit, AndraComponentes.andraEtiqueta(andraPrestamoActivo.getAndraPersona().getAndraNombre() + " · " + AndraEquipoServicio.andraTextoLimite(andraPrestamoActivo.getAndraLimite(), LocalDateTime.now(AndraFechas.andraReloj())), "row-sub"));
            VBox andraLista2 = AndraComponentes.andraLista(List.of(AndraComponentes.andraFila(andraTextos, AndraComponentes.andraPildora("En uso", "info"),
                    AndraComponentes.andraBotonSm("Registrar devolución", "btn-secondary", null, andraEvento -> andraAcciones.andraDevolverCustodia(this::andraRedibujar)))));
            VBox.setMargin(andraLista2, new Insets(0, 0, 20, 0));
            andraSeccion1.getChildren().add(andraLista2);
        }
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("En curso (" + andraCurso.size() + ")", null, AndraFiltros.andraBoton(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_PRESTAMOS,
                AndraAdministradorFiltros.andraPersona(andraCatalogo, "Código o DNI del solicitante", "Ej. 20240157"), andraNotificador, this::andraRedibujar));
        andraSeccion1.getChildren().addAll(andraCab, AndraFiltros.andraResumen(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_PRESTAMOS, andraCatalogo::andraNombreEscuela, this::andraRedibujar),
                andraCurso.isEmpty() ? AndraComponentes.andraVacio("fth-box", "Nada en curso", "No hay préstamos por revisar.") : andraLista(andraCurso));
        List<Node> andraNodos = new ArrayList<>(List.of(AndraComponentes.andraPortadaAdministrador("Préstamos de equipos", "Revisa solicitudes, entregas y devoluciones del inventario.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")), andraSeccion1));
        if (!andraResto.isEmpty()) {
            andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Historial", null), andraLista(andraResto)));
        }
        return andraNodos;
    }

    private VBox andraLista(List<AndraPrestamo> andraPrestamos) {
        List<Node> andraFilas = new ArrayList<>();
        for (AndraPrestamo andraAlto : andraPrestamos) {
            HBox andraTit = new HBox(6, AndraComponentes.andraParrafo(andraAlto.getAndraNombre(), "row-title"), AndraComponentes.andraLiteral("(" + andraAlto.getAndraCodigo() + ")", "muted"));
            andraTit.setAlignment(Pos.CENTER_LEFT);
            VBox andraTextos = new VBox(2, andraTit, AndraComponentes.andraParrafo(andraAlto.getAndraPersona().getAndraNombre() + " · " + andraAlto.getAndraDetalle(), "row-sub"));
            List<Node> andraAcc = new ArrayList<>(List.of(AndraComponentes.andraPildora(andraAlto.getAndraEstado().andraObtenerEtiqueta())));
            if (andraAlto.getAndraEstado() == AndraEstadoPrestamo.EN_REVISION) {
                andraAcc.add(AndraComponentes.andraBotonSm("Aprobar", "btn-primary", null, andraEvento -> andraAcciones.andraAprobarPrestamo(andraAlto, this::andraRedibujar)));
                andraAcc.add(AndraComponentes.andraBotonSm("Rechazar", "btn-danger", null, andraEvento -> andraAcciones.andraRechazarPrestamo(andraAlto, this::andraRedibujar)));
            } else if (andraAlto.getAndraEstado() == AndraEstadoPrestamo.APROBADO) {
                andraAcc.add(AndraComponentes.andraBotonSm("Registrar devolución", "btn-secondary", null, andraEvento -> andraAcciones.andraDevolverPrestamo(andraAlto, this::andraRedibujar)));
            }
            andraFilas.add(AndraComponentes.andraFila(andraTextos, andraAcc.toArray(new Node[0])));
        }
        return AndraComponentes.andraLista(andraFilas);
    }
}
