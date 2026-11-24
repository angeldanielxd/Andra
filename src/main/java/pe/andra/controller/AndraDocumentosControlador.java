package pe.andra.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.AndraDocumento;
import pe.andra.model.AndraEscuelaProfesional;
import pe.andra.model.AndraTramite;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraMontos;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AndraDocumentosControlador extends AndraVistaBase {

    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraDocumentoServicio andraDocumentos;
    private final AndraAccionesDocumento andraAccionesDocumento;
    private final AndraAccionesPago andraAccionesPago;

    private final AndraEstadoFiltro andraFiltros = new AndraEstadoFiltro();
    private AndraDocumentoServicio.AndraCriterio andraAplicado = AndraDocumentoServicio.AndraCriterio.andraVacio();
    private String andraOrden = "rel";
    private String andraPublicoTramite = "all";
    private boolean andraTodosTramites;
    private final Map<String, Boolean> andraMas = new HashMap<>();
    private TextField andraBusqueda;

    @Override
    public void andraEnfocarBusqueda() {
        if (andraBusqueda != null) {
            AndraComponentes.andraDesplazarHasta(andraDesplazable, andraBusqueda);
            andraBusqueda.requestFocus();
        }
    }

    @Override
    protected List<Node> andraConstruir() {
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada(null, "Documentos", "Solicita constancias y otros documentos académicos. Realiza el seguimiento de tus solicitudes.",
                AndraComponentes.andraLineaContexto(null, andraContexto.andraTextoEscuela(), "ctx:escuela")));
        andraNodos.add(andraTramites());
        andraNodos.add(andraBuscador());
        andraNodos.add(andraResultados());
        andraNodos.add(andraMisDocumentos());
        return andraNodos;
    }

    private VBox andraTramites() {
        String[][] andraPublicos = {{"all", "Todos"}, {"Estudiante", "Estudiantes"}, {"Egresado", "Egresados"}, {"Docente", "Docentes"},
                {"Administrativo", "Administrativos"}, {"Público en general", "Público en general"}};
        FlowPane andraFichas = AndraComponentes.andraFichas();
        ToggleGroup andraGrupoOpciones = new ToggleGroup();
        for (String[] andraTextos : andraPublicos) {
            ToggleButton andraBotonOpcion = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcion, andraTextos[1]);
            andraBotonOpcion.getStyleClass().add("chip");
            andraBotonOpcion.setToggleGroup(andraGrupoOpciones);
            andraBotonOpcion.setSelected(andraTextos[0].equals(andraPublicoTramite));
            andraBotonOpcion.setOnAction(andraEvento -> {
                andraPublicoTramite = andraTextos[0];
                andraTodosTramites = false;
                andraRedibujar();
            });
            andraFichas.getChildren().add(andraBotonOpcion);
        }
        AndraComponentes.andraAccesible(andraFichas, "Para quién");
        VBox.setMargin(andraFichas, new Insets(0, 0, 16, 0));
        List<AndraTramite> andraLista = andraCatalogo.andraTramites().stream().filter(andraTramite -> "all".equals(andraPublicoTramite) || andraTramite.andraAdmite(andraPublicoTramite)).toList();
        List<AndraTramite> andraVisibles = andraTodosTramites ? andraLista : andraLista.subList(0, Math.min(8, andraLista.size()));
        AndraRejilla andraTarjetas = new AndraRejilla(4, 230, 14);
        for (AndraTramite andraTramite : andraVisibles) {
            HBox andraArriba = new HBox(AndraComponentes.andraMayus(andraTramite.getAndraCategoria(), "label-caps"), AndraComponentes.andraEspacio(),
                    AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraTramite.getAndraMonto()), "tc-price"));
            andraArriba.setAlignment(Pos.CENTER_LEFT);
            VBox andraTarjeta = new VBox(8, andraArriba);
            if (andraTramite.getAndraUnidad() != null) {
                Label andraEtiqueta = AndraComponentes.andraEtiqueta(andraTramite.getAndraUnidad(), "hint");
                HBox andraFila = new HBox(AndraComponentes.andraEspacio(), andraEtiqueta);
                andraTarjeta.getChildren().add(andraFila);
            }
            Label andraEntrega = AndraComponentes.andraEtiqueta(andraTramite.andraObtenerTextoEntrega());
            andraEntrega.setGraphic(new FontIcon("fth-clock"));
            Label andraQuien = AndraComponentes.andraEtiqueta(andraTramite.andraObtenerListaPublicos().size() > 3 ? "Varios solicitantes" : String.join(", ", andraTramite.andraObtenerListaPublicos()));
            andraQuien.setGraphic(new FontIcon("fth-user"));
            andraQuien.setWrapText(true);
            VBox andraMeta = new VBox(4, andraEntrega, andraQuien);
            andraMeta.getStyleClass().add("tc-meta");
            Button andraSolicitar = AndraComponentes.andraBloque(AndraComponentes.andraBotonSm("Solicitar", "btn-primary", null, andraEvento -> andraAccionesPago.andraIniciar(andraTramite, null, this::andraRedibujar)));
            andraTarjeta.getChildren().addAll(AndraComponentes.andraParrafo(andraTramite.getAndraNombre(), "h3", "sm"), AndraComponentes.andraParrafo(andraTramite.getAndraDescripcion(), "muted", "small"),
                    andraMeta, AndraComponentes.andraEspacioV(), andraSolicitar);
            andraTarjeta.getStyleClass().add("tupa-card");
            andraTarjetas.getChildren().add(andraTarjeta);
        }
        Label andraAntetitulo = AndraComponentes.andraEtiqueta("Con Andra Pay", "pay-eyebrow");
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Solicita un documento oficial",
                "Se genera con tus datos, lo pagas con Yape, Plin o tarjeta y, por lo general, lo recoges el mismo día.");
        VBox andraPanel = new VBox(andraAntetitulo, andraCab, andraFichas, andraTarjetas);
        if (andraLista.size() > 8) {
            Button andraMasBoton = AndraComponentes.andraBotonSm(andraTodosTramites ? "Mostrar menos" : "Ver los " + andraLista.size() + " trámites", "btn-secondary", null, andraEvento -> {
                andraTodosTramites = !andraTodosTramites;
                andraRedibujar();
            });
            HBox andraCentro = new HBox(andraMasBoton);
            andraCentro.setAlignment(Pos.CENTER);
            andraCentro.setPadding(new Insets(16, 0, 0, 0));
            andraPanel.getChildren().add(andraCentro);
        }
        Label andraNota = AndraComponentes.andraEtiqueta("Montos referenciales de demostración.", "hint");
        VBox.setMargin(andraNota, new Insets(14, 0, 0, 0));
        andraPanel.getChildren().add(andraNota);
        andraPanel.getStyleClass().add("tupa-panel");
        VBox andraSeccion = AndraComponentes.andraSeccion(andraPanel);
        andraSeccion.getStyleClass().add("sin-arriba");
        return andraSeccion;
    }

    private VBox andraBuscador() {
        AndraEscuelaProfesional andraFac = andraContexto.andraEscuela();
        andraBusqueda = new TextField(andraAplicado.andraTexto());
        AndraComponentes.andraIndicacion(andraBusqueda, "Busca formatos, resoluciones, sílabos o palabras clave…");
        AndraComponentes.andraAccesible(andraBusqueda, "Buscar documentos");
        StackPane andraCaja = new StackPane(andraBusqueda, AndraComponentes.andraIcono("fth-search"));
        StackPane.setAlignment(andraCaja.getChildren().get(1), Pos.CENTER_LEFT);
        StackPane.setMargin(andraCaja.getChildren().get(1), new Insets(0, 0, 0, 14));
        andraCaja.getStyleClass().addAll("search", "search-lg");
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Tipo de documento", AndraOpcion.andraLista(andraCatalogo.andraTiposDocumento()), "Todos los tipos", andraAplicado.andraTipo()));
        List<AndraOpcion> andraDeps = new ArrayList<>();
        andraDeps.add(AndraOpcion.andraGrupo("Escuelas profesionales"));
        andraCatalogo.andraEscuelas().forEach(andraEscuelaProfesional -> andraDeps.add(AndraOpcion.andraDe(andraEscuelaProfesional.getAndraNombre(), andraEscuelaProfesional.getAndraNombre())));
        andraDeps.add(AndraOpcion.andraGrupo("Dependencias administrativas"));
        andraCatalogo.andraDependencias().forEach(andraTexto -> andraDeps.add(AndraOpcion.andraDe(andraTexto, andraTexto)));
        AndraCampo<ComboBox<AndraOpcion>> andraDep = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Dependencia / Escuela profesional", andraDeps,
                andraFac != null ? "EP " + andraFac.getAndraNombre() + " e institucionales" : "Todas las dependencias", andraAplicado.andraDependencia()));
        AndraCampo<ComboBox<AndraOpcion>> andraPublico = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Público objetivo", AndraOpcion.andraLista(AndraDocumentoServicio.ANDRA_PUBLICOS), "Todo el público", andraAplicado.andraPublico()));
        AndraCampo<ComboBox<AndraOpcion>> andraRango = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Rango de fechas", AndraOpcion.andraPares(AndraDocumentoServicio.ANDRA_RANGOS), null, andraAplicado.andraRango()));
        AndraCampo<DatePicker> andraDesde = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Desde", andraAplicado.andraDesde(), null, pe.andra.util.AndraFechas.andraHoy()));
        AndraCampo<DatePicker> andraHasta = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Hasta", andraAplicado.andraHasta(), null, pe.andra.util.AndraFechas.andraHoy()));
        GridPane andraPersonalizado = AndraFormulario.andraRejilla(2, andraDesde.andraNodo(), andraHasta.andraNodo());
        andraPersonalizado.setVisible("custom".equals(andraAplicado.andraRango()));
        andraPersonalizado.managedProperty().bind(andraPersonalizado.visibleProperty());
        andraRango.andraControl().valueProperty().addListener((andraObservable, andraOpcionA, andraOpcionB) -> andraPersonalizado.setVisible(andraOpcionB != null && "custom".equals(andraOpcionB.andraValor())));
        FlowPane andraRapidos = AndraComponentes.andraFichas();
        for (String andraTexto2 : AndraDocumentoServicio.ANDRA_TIPOS_RAPIDOS) {
            ToggleButton andraBotonOpcionB = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcionB, andraTexto2);
            andraBotonOpcionB.getStyleClass().add("chip");
            andraBotonOpcionB.setSelected(andraTexto2.equals(andraAplicado.andraTipo()));
            andraBotonOpcionB.setOnAction(andraEvento -> {
                andraAplicado = andraAplicado.andraConTipo(andraTexto2.equals(andraAplicado.andraTipo()) ? "" : andraTexto2);
                andraMas.clear();
                andraRedibujar();
            });
            andraRapidos.getChildren().add(andraBotonOpcionB);
        }
        AndraComponentes.andraAccesible(andraRapidos, "Accesos rápidos por tipo");
        int andraCantidad = andraDocumentos.andraBuscar(andraAplicado, andraFac, andraFiltros, andraOrden).size();
        Label andraConteo = AndraComponentes.andraEtiqueta(String.format("%02d", andraCantidad) + " documento" + (andraCantidad == 1 ? "" : "s") + " encontrado" + (andraCantidad == 1 ? "" : "s"), "hint");
        HBox.setHgrow(andraConteo, Priority.ALWAYS);
        andraConteo.setMaxWidth(Double.MAX_VALUE);
        Button andraLimpiar = AndraComponentes.andraBoton("Limpiar", "btn-tertiary", null, andraEvento -> {
            andraAplicado = AndraDocumentoServicio.AndraCriterio.andraVacio();
            andraMas.clear();
            andraRedibujar();
        });
        Button andraBuscar = AndraComponentes.andraBoton("Buscar documento", "btn-primary", null, andraEvento -> {
            AndraDocumentoServicio.AndraCriterio andraCriterio = new AndraDocumentoServicio.AndraCriterio(andraBusqueda.getText().trim(), AndraFormulario.andraValor(andraTipo.andraControl()),
                    AndraFormulario.andraValor(andraDep.andraControl()), AndraFormulario.andraValor(andraPublico.andraControl()), AndraFormulario.andraValor(andraRango.andraControl()),
                    andraDesde.andraControl().getValue(), andraHasta.andraControl().getValue());
            String andraError = AndraDocumentoServicio.andraValidarRangoPersonalizado(andraCriterio);
            if (andraError != null) {
                andraHasta.andraMostrar(andraError);
                andraHasta.andraControl().requestFocus();
                return;
            }
            andraAplicado = andraCriterio;
            andraMas.clear();
            andraRedibujar();
        });
        andraBusqueda.setOnAction(andraEvento -> andraBuscar.fire());
        HBox andraPie = new HBox(andraConteo, andraLimpiar, andraBuscar);
        andraPie.getStyleClass().add("form-foot");
        VBox andraPanel = new VBox(16, AndraComponentes.andraParrafo("Buscador de documentos", "h2"), andraCaja,
                AndraFormulario.andraRejilla(2, andraTipo.andraNodo(), andraDep.andraNodo(), andraPublico.andraNodo(), andraRango.andraNodo()), andraPersonalizado, andraRapidos, andraPie);
        andraPanel.getStyleClass().add("panel");
        return AndraComponentes.andraSeccion(andraPanel);
    }

    private VBox andraResultados() {
        AndraEscuelaProfesional andraFac = andraContexto.andraEscuela();
        List<AndraDocumento> andraLista = andraDocumentos.andraBuscar(andraAplicado, andraFac, andraFiltros, andraOrden);
        MenuButton andraOrdenar = new MenuButton();
        AndraComponentes.andraFijar(andraOrdenar, "Ordenar por: " + AndraDocumentoServicio.andraEtiquetaOrden(andraOrden));
        andraOrdenar.setGraphic(new FontIcon("fth-bar-chart-2"));
        andraOrdenar.getStyleClass().addAll("btn", "btn-secondary", "btn-sm");
        ToggleGroup andraGrupoOrden = new ToggleGroup();
        for (String[] andraTextos : AndraDocumentoServicio.ANDRA_ORDENES) {
            RadioMenuItem andraElemento = new RadioMenuItem();
            andraElemento.textProperty().bind(AndraIdiomaServicio.andraEnlace(andraTextos[1]));
            andraElemento.setToggleGroup(andraGrupoOrden);
            andraElemento.setSelected(andraTextos[0].equals(andraOrden));
            andraElemento.setOnAction(andraEvento -> {
                andraOrden = andraTextos[0];
                andraRedibujar();
            });
            andraOrdenar.getItems().add(andraElemento);
        }
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Resultado(s)", null, AndraFiltros.andraBoton(andraFiltros, AndraDocumentoServicio.ANDRA_PREDEFINIDOS, null, andraNotificador, this::andraRedibujar), andraOrdenar);
        VBox andraSeccion = AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, AndraDocumentoServicio.ANDRA_PREDEFINIDOS, null, this::andraRedibujar));
        if (andraLista.isEmpty()) {
            andraSeccion.getChildren().add(AndraComponentes.andraVacio("fth-search", "No encontramos documentos", "Revisa la ortografía o quita algunos filtros."));
            return andraSeccion;
        }
        boolean andraPorEscuela = andraFac != null && (andraAplicado.andraDependencia() == null || andraAplicado.andraDependencia().isBlank());
        List<AndraDocumento> andraDeEscuela = andraPorEscuela ? andraLista.stream().filter(andraDocumento -> andraDocumento.getAndraEscuela() != null && andraDocumento.getAndraEscuela().getAndraCodigo().equals(andraFac.getAndraCodigo())).toList() : List.of();
        List<AndraDocumento> andraFrecuentes = andraLista.stream().filter(AndraDocumento::isAndraFrecuente).filter(andraDocumento -> !andraDeEscuela.contains(andraDocumento)).toList();
        List<AndraDocumento> andraGenerales = andraLista.stream().filter(andraDocumento -> !andraDocumento.isAndraFrecuente()).filter(andraDocumento -> !andraDeEscuela.contains(andraDocumento)).toList();
        VBox andraGrupos = new VBox(32);
        if (andraPorEscuela) {
            andraAgregarGrupo(andraGrupos, "fac", "EP " + andraFac.getAndraNombre(), andraDeEscuela);
        }
        andraAgregarGrupo(andraGrupos, "freq", "Frecuente", andraFrecuentes);
        andraAgregarGrupo(andraGrupos, "gen", andraPorEscuela ? "Institucionales y generales" : "General", andraGenerales);
        andraSeccion.getChildren().add(andraGrupos);
        return andraSeccion;
    }

    private void andraAgregarGrupo(VBox andraDestino, String andraClave, String andraTitulo, List<AndraDocumento> andraLista) {
        if (andraLista.isEmpty()) {
            return;
        }
        int andraLimite = 6;
        boolean andraAbierto = andraMas.getOrDefault(andraClave, false);
        Label andraEtiqueta = AndraComponentes.andraMayus(andraTitulo, "label-caps");
        Label andraEtiqueta2 = AndraComponentes.andraLiteral(" (" + andraLista.size() + ")", "hint");
        HBox andraCab = new HBox(andraEtiqueta, andraEtiqueta2);
        andraCab.setAlignment(Pos.CENTER_LEFT);
        AndraRejilla andraRejilla = new AndraRejilla(2, 360);
        (andraAbierto ? andraLista : andraLista.subList(0, Math.min(andraLimite, andraLista.size()))).forEach(andraDocumento -> andraRejilla.getChildren().add(andraAccionesDocumento.andraTarjeta(andraDocumento, this::andraRedibujar)));
        VBox andraGrupo = new VBox(12, andraCab, andraRejilla);
        andraGrupo.getStyleClass().add("fav-group");
        if (andraLista.size() > andraLimite) {
            Button andraBoton = AndraComponentes.andraBotonSm(andraAbierto ? "Mostrar menos" : "Mostrar " + (andraLista.size() - andraLimite) + " más", "btn-secondary", null, andraEvento -> {
                andraMas.put(andraClave, !andraAbierto);
                andraRedibujar();
            });
            HBox andraCentro = new HBox(andraBoton);
            andraCentro.setAlignment(Pos.CENTER);
            andraGrupo.getChildren().add(andraCentro);
        }
        andraDestino.getChildren().add(andraGrupo);
    }

    private VBox andraMisDocumentos() {
        List<Node> andraFilas = new ArrayList<>();
        andraDocumentos.andraMisDocumentos().forEach(andraDocumentoPersonal -> andraFilas.add(andraAccionesDocumento.andraFilaPersonal(andraDocumentoPersonal, this::andraRedibujar)));
        return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Mis documentos", "Documentos emitidos a tu nombre"), AndraComponentes.andraLista(andraFilas));
    }
}
