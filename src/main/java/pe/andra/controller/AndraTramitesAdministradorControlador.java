package pe.andra.controller;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;
import pe.andra.util.DixonTextoPlano;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraTramitesAdministradorControlador extends AndraVistaBase {

    private final DixonAdministradorServicio andraAdministrador;
    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final AndraAccionesAdministrador andraAcciones;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();
    private String andraPestana = "todo";

    @Override
    protected List<Node> andraConstruir() {
        List<DixonExpediente> andraBase = andraAdministrador.dixonExpedientes();
        Map<String, Predicate<DixonExpediente>> andraPestanas = new LinkedHashMap<>();
        andraPestanas.put("todo", andraExpediente -> andraExpediente.getDixonGrupo() == DixonGrupoExpediente.TRAMITE);
        andraPestanas.put("obs", andraExpediente -> andraExpediente.getDixonGrupo() == DixonGrupoExpediente.OBSERVADO);
        andraPestanas.put("listo", andraExpediente -> andraExpediente.getDixonGrupo() == DixonGrupoExpediente.LISTO);
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
        List<DixonExpediente> andraLista = andraBase.stream().filter(andraPestanas.get(andraPestana)).toList();
        Map<String, Function<DixonExpediente, String>> andraCampos = new LinkedHashMap<>();
        andraCampos.put("fac", andraExpediente -> andraEscuela(andraExpediente));
        andraCampos.put("tipo", andraExpediente -> andraAcciones.andraSolicitante(andraExpediente).getDixonTipo());
        andraLista = DixonFiltroServicio.dixonAplicarAvanzado(andraLista, andraFiltros, new DixonFiltroServicio.DixonAccesores<>(DixonSeguimientoServicio::dixonFechaDe, this::andraClave, andraCampos));
        Map<String, Predicate<DixonExpediente>> andraPred = new HashMap<>(DixonFiltroServicio.dixonPredicadosFecha(DixonSeguimientoServicio::dixonFechaDe));
        andraPred.put("paid", andraExpediente -> andraExpediente.getDixonPago() != null && andraExpediente.getDixonPago().dixonEsPagado());
        andraPred.put("unpaid", andraExpediente -> andraExpediente.getDixonPago() != null && andraExpediente.getDixonPago().dixonEsPendiente());
        andraPred.put("carne", andraExpediente -> andraExpediente.getDixonTramite() != null && andraExpediente.getDixonTramite().dixonEsCarne());
        andraLista = DixonFiltroServicio.dixonAplicar(andraLista, andraFiltros, andraPred, andraExpediente -> andraExpediente.getDixonCodigo() + " " + andraExpediente.getDixonTitulo() + " " + andraExpediente.getDixonPersona().getDixonNombre() + " " + andraExpediente.getDixonEstado());
        List<AndraFiltros.AndraAvanzado> andraAvanzados = AndraAdministradorFiltros.andraFechas("Desde", "Hasta");
        andraAvanzados.addAll(AndraAdministradorFiltros.andraPersona(andraCatalogo, "Código o DNI del solicitante", "Ej. 20240157"));
        HBox andraCab = new HBox(16, andraSeg, AndraComponentes.andraEspacio(), AndraFiltros.andraBoton(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_TRAMITES, andraAvanzados, andraNotificador, this::andraRedibujar));
        andraCab.getStyleClass().add("section-h");
        Node andraCuerpo;
        if (andraLista.isEmpty()) {
            andraCuerpo = AndraComponentes.andraVacio("fth-inbox", "No hay trámites aquí", "Cambia de pestaña, de sede o quita filtros.");
        } else {
            List<Node[]> andraFilas = new ArrayList<>();
            for (DixonExpediente andraExpediente : andraLista) {
                DixonDatosSolicitante andraSol = andraAcciones.andraSolicitante(andraExpediente);
                LocalDate andraFecha = DixonSeguimientoServicio.dixonFechaDe(andraExpediente);
                DixonPago andraPago = andraExpediente.getDixonPago();
                Node andraPago2 = andraPago == null ? AndraComponentes.andraEtiqueta("No aplica", "hint") : andraPago.dixonEsPagado() ? AndraComponentes.andraPildora(DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "success")
                        : AndraComponentes.andraPildora(andraPago.getDixonEstado() == DixonEstadoPago.PENDIENTE ? "Pendiente" : andraPago.getDixonEstado().dixonObtenerEtiqueta(), andraPago.dixonEsPendiente() ? "warning" : "neutral");
                String andraEscuela = andraSol.getDixonEscuela() != null && andraCatalogo.dixonEscuela(andraSol.getDixonEscuela()).isPresent() ? " · " + andraCatalogo.dixonNombreEscuela(andraSol.getDixonEscuela()) : "";
                andraFilas.add(new Node[]{AndraTabla.andraCelda(andraExpediente.getDixonCodigo(), andraFecha != null ? DixonFechas.dixonDmy(andraFecha) : andraExpediente.getDixonDatos().get(0).getDixonValor()),
                        AndraTabla.andraCelda(andraSol.getDixonNombre(), andraSol.getDixonTipo() + " · " + (andraSol.getDixonCodigo() == null ? "—" : andraSol.getDixonCodigo()) + andraEscuela),
                        AndraComponentes.andraParrafo(andraExpediente.getDixonTitulo(), "small"), AndraComponentes.andraLiteral(andraExpediente.getDixonSede().getDixonNombre(), "small"), andraPago2,
                        AndraComponentes.andraPildora(DixonTextoPlano.dixonRecortar(andraExpediente.getDixonEstado(), 34), andraExpediente.getDixonTono()),
                        AndraComponentes.andraBotonSm("Revisar", "btn-secondary", null, andraEvento -> andraAcciones.andraRevisar(andraExpediente.getDixonCodigo(), this::andraRedibujar))});
            }
            andraCuerpo = AndraTabla.andraCrear(new String[]{"Expediente", "Solicitante", "Trámite", "Sede", "Pago", "Estado", "Acciones"},
                    new double[]{14, 22, 20, 9, 11, 15, 9}, new boolean[]{false, false, false, false, false, false, true}, andraFilas);
        }
        VBox andraSeccion = AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, DixonAdministradorServicio.DIXON_PREDEFINIDOS_TRAMITES, andraCatalogo::dixonNombreEscuela, this::andraRedibujar), andraCuerpo);
        andraSeccion.getStyleClass().add("sin-arriba");
        return List.of(AndraComponentes.andraPortadaAdministrador("Trámites y documentos", "Revisa lo que piden estudiantes, docentes, administrativos y público en general.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")), andraSeccion);
    }

    private String andraClave(DixonExpediente andraExpediente) {
        DixonDatosSolicitante andraSol = andraAcciones.andraSolicitante(andraExpediente);
        return (andraSol.getDixonCodigo() == null ? "" : andraSol.getDixonCodigo()) + " " + (andraSol.getDixonDni() == null ? "" : andraSol.getDixonDni()) + " " + andraSol.getDixonNombre();
    }

    private String andraEscuela(DixonExpediente andraExpediente) {
        DixonDatosSolicitante andraSol = andraAcciones.andraSolicitante(andraExpediente);
        return andraSol.getDixonEscuela();
    }
}
