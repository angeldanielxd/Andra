package pe.andra.controller;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;
import pe.andra.util.AndraTextoPlano;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraTramitesAdministradorControlador extends AndraVistaBase {

    private final AndraAdministradorServicio andraAdministrador;
    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;

    private final AndraEstadoFiltro andraFiltros = new AndraEstadoFiltro();
    private String andraPestana = "todo";

    @Override
    protected List<Node> andraConstruir() {
        List<AndraExpediente> andraBase = andraAdministrador.andraExpedientes();
        Map<String, Predicate<AndraExpediente>> andraPestanas = new LinkedHashMap<>();
        andraPestanas.put("todo", andraExpediente -> andraExpediente.getAndraGrupo() == AndraGrupoExpediente.TRAMITE);
        andraPestanas.put("obs", andraExpediente -> andraExpediente.getAndraGrupo() == AndraGrupoExpediente.OBSERVADO);
        andraPestanas.put("listo", andraExpediente -> andraExpediente.getAndraGrupo() == AndraGrupoExpediente.LISTO);
        andraPestanas.put("all", andraExpediente -> true);
        String[][] andraNombres = {{"todo", "Por atender"}, {"obs", "Observados"}, {"listo", "Listos y entregados"}, {"all", "Todos"}};
        AndraSegmentado andraSeg = new AndraSegmentado(true);
        for (String[] andraTextos : andraNombres) {
            andraSeg.andraOpcion(andraTextos[0], andraTextos[1], null, andraBase.stream().filter(andraPestanas.get(andraTextos[0])).count());
        }
        andraSeg.andraSeleccionar(andraPestana).andraAlCambiar(andraValor -> {
            andraPestana = andraValor;
            andraRedibujar();
        });
        andraSeg.setMaxWidth(640);
        List<AndraExpediente> andraLista = andraBase.stream().filter(andraPestanas.get(andraPestana)).toList();
        Map<String, Function<AndraExpediente, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraExpediente -> andraEscuela(andraExpediente));
        andraCampos.put("tipo", andraExpediente -> andraAcciones.andraSolicitante(andraExpediente).getAndraTipo());
        andraLista = AndraFiltroServicio.andraAplicarAvanzado(andraLista, andraFiltros, new AndraFiltroServicio.AndraAccesores<>(AndraSeguimientoServicio::andraFechaDe, this::andraClave, andraCampos));
        Map<String, Predicate<AndraExpediente>> andraPred = new HashMap<>(AndraFiltroServicio.andraPredicadosFecha(AndraSeguimientoServicio::andraFechaDe));
        andraPred.put("paid", andraExpediente -> andraExpediente.getAndraPago() != null && andraExpediente.getAndraPago().andraEsPagado());
        andraPred.put("unpaid", andraExpediente -> andraExpediente.getAndraPago() != null && andraExpediente.getAndraPago().andraEsPendiente());
        andraPred.put("carne", andraExpediente -> andraExpediente.getAndraTramite() != null && andraExpediente.getAndraTramite().andraEsCarne());
        andraLista = AndraFiltroServicio.andraAplicar(andraLista, andraFiltros, andraPred, andraExpediente -> andraExpediente.getAndraCodigo() + " " + andraExpediente.getAndraTitulo() + " " + andraExpediente.getAndraPersona().getAndraNombre() + " " + andraExpediente.getAndraEstado());
        List<AndraFiltros.AndraAvanzado> andraAvanzados = AndraAdministradorFiltros.andraFechas("Desde", "Hasta");
        andraAvanzados.addAll(AndraAdministradorFiltros.andraPersona(andraCatalogo, "Código o DNI del solicitante", "Ej. 20240157"));
        HBox andraCab = new HBox(16, andraSeg, AndraComponentes.andraEspacio(), AndraFiltros.andraBoton(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_TRAMITES, andraAvanzados, andraNotificador, this::andraRedibujar));
        andraCab.getStyleClass().add("section-h");
        Node andraCuerpo;
        if (andraLista.isEmpty()) {
            andraCuerpo = AndraComponentes.andraVacio("fth-inbox", "No hay trámites aquí", "Cambia de pestaña, de sede o quita filtros.");
        } else {
            List<Node[]> andraFilas = new ArrayList<>();
            for (AndraExpediente andraExpediente : andraLista) {
                AndraDatosSolicitante andraSol = andraAcciones.andraSolicitante(andraExpediente);
                LocalDate andraFecha = AndraSeguimientoServicio.andraFechaDe(andraExpediente);
                AndraPago andraPago = andraExpediente.getAndraPago();
                Node andraPago2 = andraPago == null ? AndraComponentes.andraEtiqueta("No aplica", "hint") : andraPago.andraEsPagado() ? AndraComponentes.andraPildora(AndraMontos.andraSoles(andraPago.andraObtenerTotal()), "success")
                        : AndraComponentes.andraPildora(andraPago.getAndraEstado() == AndraEstadoPago.PENDIENTE ? "Pendiente" : andraPago.getAndraEstado().andraObtenerEtiqueta(), andraPago.andraEsPendiente() ? "warning" : "neutral");
                String andraEscuela = andraSol.getAndraEscuela() != null && andraCatalogo.andraEscuela(andraSol.getAndraEscuela()).isPresent() ? " · " + andraCatalogo.andraNombreEscuela(andraSol.getAndraEscuela()) : "";
                andraFilas.add(new Node[]{AndraTabla.andraCelda(andraExpediente.getAndraCodigo(), andraFecha != null ? AndraFechas.andraDmy(andraFecha) : andraExpediente.getAndraDatos().get(0).getAndraValor()),
                        AndraTabla.andraCelda(andraSol.getAndraNombre(), andraSol.getAndraTipo() + " · " + (andraSol.getAndraCodigo() == null ? "—" : andraSol.getAndraCodigo()) + andraEscuela),
                        AndraComponentes.andraParrafo(andraExpediente.getAndraTitulo(), "small"), AndraComponentes.andraLiteral(andraExpediente.getAndraSede().getAndraNombre(), "small"), andraPago2,
                        AndraComponentes.andraPildora(AndraTextoPlano.andraRecortar(andraExpediente.getAndraEstado(), 34), andraExpediente.getAndraTono()),
                        AndraComponentes.andraBotonSm("Revisar", "btn-secondary", null, andraEvento -> andraAcciones.andraRevisar(andraExpediente.getAndraCodigo(), this::andraRedibujar))});
            }
            andraCuerpo = AndraTabla.andraCrear(new String[]{"Expediente", "Solicitante", "Trámite", "Sede", "Pago", "Estado", "Acciones"},
                    new double[]{14, 22, 20, 9, 11, 15, 9}, new boolean[]{false, false, false, false, false, false, true}, andraFilas);
        }
        VBox andraSeccion = AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, AndraAdministradorServicio.ANDRA_PREDEFINIDOS_TRAMITES, andraCatalogo::andraNombreEscuela, this::andraRedibujar), andraCuerpo);
        andraSeccion.getStyleClass().add("sin-arriba");
        return List.of(AndraComponentes.andraPortadaAdministrador("Trámites y documentos", "Revisa lo que piden estudiantes, docentes, administrativos y público en general.",
                AndraComponentes.andraLineaContexto(andraContexto.andraTextoSede(), andraContexto.andraTextoEscuela(), "ctx:sede")), andraSeccion);
    }

    private String andraClave(AndraExpediente andraExpediente) {
        AndraDatosSolicitante andraSol = andraAcciones.andraSolicitante(andraExpediente);
        return (andraSol.getAndraCodigo() == null ? "" : andraSol.getAndraCodigo()) + " " + (andraSol.getAndraDni() == null ? "" : andraSol.getAndraDni()) + " " + andraSol.getAndraNombre();
    }

    private String andraEscuela(AndraExpediente andraExpediente) {
        AndraDatosSolicitante andraSol = andraAcciones.andraSolicitante(andraExpediente);
        return andraSol.getAndraEscuela();
    }
}
