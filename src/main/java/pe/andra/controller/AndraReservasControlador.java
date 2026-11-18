package pe.andra.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class AndraReservasControlador extends AndraVistaBase {

    private record AndraFiltro(String andraTipo, Integer andraCapacidad, LocalDate andraFecha, String andraHorario) {
    }

    private static final List<String[]> ANDRA_PREDEFINIDOS = List.of(new String[]{"free", "Disponibles"}, new String[]{"fav", "Mis favoritos"},
            new String[]{"big", "Capacidad 50+"}, new String[]{"gen", "Uso general"});

    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonReservaServicio andraReservas;
    private final DixonOcupacionServicio andraOcupacion;
    private final DixonFavoritoServicio andraFavoritos;
    private final AndraAccionesEspacio andraAccionesEspacio;
    private final DixonConexionServicio andraConexion;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();
    private AndraFiltro andraFiltro;
    private String andraCategoria = "all";
    private String andraVistaEspacios = "gal";
    private String andraFranjaMapa = "now";
    private VBox andraSeccionEspacios;
    private VBox andraFormulario;
    private Timeline andraRefrescoMapa;

    @Override
    public void andraSalir() {
        if (andraRefrescoMapa != null) {
            andraRefrescoMapa.stop();
        }
    }

    public void andraMostrarMapa() {
        andraVistaEspacios = "map";
        andraRefrescar();
        Platform.runLater(() -> AndraComponentes.andraDesplazarHasta(andraDesplazable, andraSeccionEspacios));
    }

    @Override
    protected List<Node> andraConstruir() {
        andraSalir();
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada("En este apartado podrás reservar espacios de la universidad", "Reservas",
                "Encuentra y reserva aulas, laboratorios y otros espacios universitarios.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")));
        andraNodos.add(andraBuscador());
        andraSeccionEspacios = andraEspacios();
        andraNodos.add(andraSeccionEspacios);
        andraNodos.add(andraMisReservas());
        if ("map".equals(andraVistaEspacios)) {
            andraRefrescoMapa = new Timeline(new KeyFrame(Duration.seconds(60), andraEvento -> {
                if (!andraNotificador.andraHayModal() && andraConexion.dixonEnLinea()) {
                    andraRedibujar();
                }
            }));
            andraRefrescoMapa.setCycleCount(Animation.INDEFINITE);
            andraRefrescoMapa.play();
        }
        return andraNodos;
    }

    private List<DixonEspacio> andraEnContexto() {
        return andraCatalogo.dixonEspacios().stream().filter(andraEspacio -> andraContexto.dixonIncluye(andraEspacio.getDixonSede(), andraEspacio.getDixonEscuela())).toList();
    }

    private String andraEstadoDe(DixonEspacio andraEspacio) {
        return andraReservas.dixonEstadoGaleria(andraEspacio, andraFiltro == null ? null : andraFiltro.andraFecha(), andraFiltro == null ? null : andraFiltro.andraHorario());
    }

    private VBox andraBuscador() {
        List<DixonEspacio> andraConjunto = andraEnContexto();
        List<AndraOpcion> andraTipos = new ArrayList<>();
        new LinkedHashSet<>(andraConjunto.stream().map(DixonEspacio::getDixonTipo).toList()).forEach(andraTipoEspacio -> andraTipos.add(AndraOpcion.andraDe(andraTipoEspacio.name(), andraTipoEspacio.dixonObtenerEtiqueta())));
        AndraFormulario andraFormulario2 = new AndraFormulario();
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario2.andraAgregar(AndraFormulario.andraSelector("Tipo de espacio", andraTipos, "Todos los tipos", andraFiltro != null ? andraFiltro.andraTipo() : null));
        AndraCampo<TextField> andraCap = andraFormulario2.andraAgregar(AndraFormulario.andraNumero("Capacidad", "Número de personas",
                andraFiltro != null && andraFiltro.andraCapacidad() != null ? String.valueOf(andraFiltro.andraCapacidad()) : "", 5));
        AndraCampo<DatePicker> andraFecha = andraFormulario2.andraAgregar(AndraFormulario.andraFecha("Fecha", andraFiltro != null ? andraFiltro.andraFecha() : DixonFechas.dixonHoy(), DixonFechas.dixonHoy(), null))
                .andraRequerido("Elige una fecha.").andraRegla(andraSelectorFecha -> andraSelectorFecha.getValue() != null && andraSelectorFecha.getValue().isBefore(DixonFechas.dixonHoy()) ? "Elige hoy o una fecha posterior." : null);
        AndraCampo<ComboBox<AndraOpcion>> andraHora = andraFormulario2.andraAgregar(AndraFormulario.andraSelector("Horario", AndraOpcion.andraLista(DixonCatalogoServicio.DIXON_HORARIOS), "Cualquier horario",
                andraFiltro != null ? andraFiltro.andraHorario() : null));
        long andraCoinciden = andraFiltro == null ? 0 : andraConjunto.stream().filter(andraEspacio -> (andraFiltro.andraTipo() == null || andraEspacio.getDixonTipo().name().equals(andraFiltro.andraTipo()))
                && (andraFiltro.andraCapacidad() == null || andraEspacio.getDixonCapacidad() >= andraFiltro.andraCapacidad())).count();
        Label andraResultado = AndraComponentes.andraParrafo(andraFiltro != null ? andraCoinciden + " espacio" + (andraCoinciden == 1 ? "" : "s") + " coinciden con tu búsqueda."
                : "Completa al menos la fecha para buscar.", "hint");
        HBox.setHgrow(andraResultado, Priority.ALWAYS);
        andraResultado.setMaxWidth(Double.MAX_VALUE);
        Button andraBuscar = AndraComponentes.andraBoton("Buscar disponibilidad", "btn-primary", "fth-search", andraEvento -> {
            if (!andraFormulario2.andraValidar()) {
                return;
            }
            String andraTexto = AndraFormulario.andraValor(andraTipo.andraControl());
            String andraTexto2 = andraCap.andraControl().getText().trim();
            String andraAlto = AndraFormulario.andraValor(andraHora.andraControl());
            andraFiltro = new AndraFiltro(andraTexto.isBlank() ? null : andraTexto, andraTexto2.isBlank() ? null : Integer.valueOf(andraTexto2), andraFecha.andraControl().getValue(), andraAlto.isBlank() ? null : andraAlto);
            andraCategoria = andraTexto.isBlank() ? "all" : andraTexto;
            andraVistaEspacios = "gal";
            andraRefrescar();
            Platform.runLater(() -> AndraComponentes.andraDesplazarHasta(andraDesplazable, andraSeccionEspacios));
        });
        andraBuscar.setDefaultButton(false);
        HBox andraPie = new HBox(andraResultado);
        if (andraFiltro != null) {
            andraPie.getChildren().add(AndraComponentes.andraBoton("Limpiar filtros", "btn-tertiary", null, andraEvento -> {
                andraFiltro = null;
                andraRefrescar();
            }));
        }
        andraPie.getChildren().add(andraBuscar);
        andraPie.getStyleClass().add("form-foot");
        andraFormulario = new VBox(AndraComponentes.andraParrafo("Buscar disponibilidad", "h2"), AndraFormulario.andraRejilla(2, andraTipo.andraNodo(), andraCap.andraNodo(), andraFecha.andraNodo(), andraHora.andraNodo()), andraPie);
        andraFormulario.setSpacing(20);
        andraFormulario.getStyleClass().add("panel");
        andraFormulario.getProperties().put("tipo", andraTipo.andraControl());
        VBox andraSeccion = AndraComponentes.andraSeccion(andraFormulario);
        andraSeccion.getStyleClass().add("sin-arriba");
        return andraSeccion;
    }

    private VBox andraEspacios() {
        AndraSegmentado andraVista = new AndraSegmentado(false).andraOpcion("gal", "Galería", "fth-image", null);
        Circle andraVivo = new Circle(4);
        andraVivo.getStyleClass().addAll("live-dot", "on");
        andraVista.andraOpcionNodo("map", "Mapa en vivo", andraVivo).andraSeleccionar(andraVistaEspacios);
        andraVista.andraAlCambiar(andraValor -> {
            andraVistaEspacios = andraValor;
            andraRedibujar();
        });
        if ("map".equals(andraVistaEspacios)) {
            return AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Espacios", "Mira qué está libre ahora mismo y toca un espacio para reservarlo.", andraVista), andraMapa());
        }
        List<DixonEspacio> andraConjunto = andraEnContexto().stream().filter(andraEspacio -> andraFiltro == null || andraFiltro.andraCapacidad() == null || andraEspacio.getDixonCapacidad() >= andraFiltro.andraCapacidad()).toList();
        List<DixonEspacio> andraLista = andraConjunto.stream().filter(andraEspacio -> "all".equals(andraCategoria) || andraEspacio.getDixonTipo().name().equals(andraCategoria)).toList();
        Set<String> andraFav = andraFavoritos.dixonCodigos(DixonTipoFavorito.ESPACIO);
        Map<String, Predicate<DixonEspacio>> andraPred = Map.of("free", andraEspacio -> "Disponible".equals(andraEstadoDe(andraEspacio)), "fav", andraEspacio -> andraFav.contains(andraEspacio.getDixonCodigo()),
                "big", andraEspacio -> andraEspacio.getDixonCapacidad() >= 50, "gen", andraEspacio -> andraEspacio.getDixonEscuela() == null);
        andraLista = new ArrayList<>(DixonFiltroServicio.dixonAplicar(andraLista, andraFiltros, andraPred, andraEspacio -> andraEspacio.getDixonNombre() + " " + andraEspacio.getDixonEdificio() + " " + andraEspacio.getDixonTipo().dixonObtenerEtiqueta()
                + " " + (andraEspacio.getDixonEscuela() == null ? "Uso general" : andraEspacio.getDixonEscuela().getDixonNombre())));
        Optional<DixonEspacio> andraAuditorio = andraLista.stream().filter(andraEspacio -> "aud-principal".equals(andraEspacio.getDixonCodigo())).findFirst();
        if (andraAuditorio.isPresent()) {
            andraLista.remove(andraAuditorio.get());
            andraLista.add(0, andraAuditorio.get());
        }
        String andraSub = (andraFiltro != null ? "Para el " + DixonFechas.dixonDmy(andraFiltro.andraFecha()) + (andraFiltro.andraHorario() != null ? ", " + andraFiltro.andraHorario() : "") + " · " : "")
                + "Sede " + andraContexto.dixonNombreSede() + (andraContexto.dixonEscuela() != null ? " · EP " + andraContexto.dixonEscuela().getDixonNombre() : "")
                + ". Pasa el cursor y elige uno para ver sus detalles.";
        Button andraBotonFiltros = AndraFiltros.andraBoton(andraFiltros, ANDRA_PREDEFINIDOS, null, andraNotificador, this::andraRedibujar);
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Espacios", andraSub, andraVista, andraBotonFiltros);
        FlowPane andraPestanas = AndraComponentes.andraFichas();
        ToggleGroup andraGrupo = new ToggleGroup();
        String[][] andraPestanas2 = {{"all", "Todos"}, {"AULA", "Aulas"}, {"AUDITORIO", "Auditorios"}, {"LABORATORIO", "Laboratorios"}, {"SALA", "Salas"}, {"TALLER", "Talleres"}};
        for (String[] andraTextos : andraPestanas2) {
            long andraCantidad = "all".equals(andraTextos[0]) ? andraConjunto.size() : andraConjunto.stream().filter(andraEspacio -> andraEspacio.getDixonTipo().name().equals(andraTextos[0])).count();
            ToggleButton andraBotonOpcion = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcion, andraTextos[1]);
            andraBotonOpcion.setGraphic(AndraComponentes.andraLiteral(String.valueOf(andraCantidad), "conteo"));
            andraBotonOpcion.setContentDisplay(ContentDisplay.RIGHT);
            andraBotonOpcion.getStyleClass().add("chip");
            andraBotonOpcion.setToggleGroup(andraGrupo);
            andraBotonOpcion.setSelected(andraTextos[0].equals(andraCategoria));
            andraBotonOpcion.setOnAction(andraEvento -> {
                andraCategoria = andraTextos[0];
                if (andraFiltro != null) {
                    andraFiltro = new AndraFiltro("all".equals(andraTextos[0]) ? null : andraTextos[0], andraFiltro.andraCapacidad(), andraFiltro.andraFecha(), andraFiltro.andraHorario());
                }
                andraRedibujar();
            });
            andraPestanas.getChildren().add(andraBotonOpcion);
        }
        VBox.setMargin(andraPestanas, new Insets(0, 0, 16, 0));
        Node andraResumen = AndraFiltros.andraResumen(andraFiltros, ANDRA_PREDEFINIDOS, null, this::andraRedibujar);
        Node andraContenido;
        if (andraLista.isEmpty()) {
            boolean andraAuditorioFuera = "AUDITORIO".equals(andraCategoria) && !"central".equals(andraContexto.dixonCodigoSede());
            andraContenido = AndraComponentes.andraVacio("fth-search", "No hay espacios aquí", andraAuditorioFuera
                    ? "Los auditorios están en la sede Central. Cambia de sede para reservarlos."
                    : "Prueba con otro tipo, quita filtros o cambia de sede.");
        } else {
            AndraGaleria andraGaleria = new AndraGaleria();
            AndraAccionesEspacio.AndraPrecarga andraPre = andraFiltro == null ? AndraAccionesEspacio.AndraPrecarga.ANDRA_VACIA
                    : new AndraAccionesEspacio.AndraPrecarga(andraFiltro.andraFecha(), andraFiltro.andraHorario(), andraFiltro.andraCapacidad());
            for (int andraIndice = 0; andraIndice < andraLista.size(); andraIndice++) {
                DixonEspacio andraEspacio = andraLista.get(andraIndice);
                boolean andraDestacado = "aud-principal".equals(andraEspacio.getDixonCodigo()) && ("all".equals(andraCategoria) || "AUDITORIO".equals(andraCategoria));
                andraGaleria.getChildren().add(andraAccionesEspacio.andraTarjeta(andraEspacio, andraEstadoDe(andraEspacio), andraIndice, andraDestacado, andraPre, this::andraRedibujar));
            }
            andraContenido = andraGaleria;
        }
        return AndraComponentes.andraSeccion(andraCab, andraPestanas, andraResumen, andraContenido);
    }

    private VBox andraMapa() {
        DixonOcupacionServicio.DixonFranjaBnps andraAhora = andraOcupacion.dixonFranjaActual();
        DixonOcupacionServicio.DixonFranjaBnps andraSel = "now".equals(andraFranjaMapa) ? andraAhora
                : new DixonOcupacionServicio.DixonFranjaBnps(Integer.parseInt(andraFranjaMapa), andraAhora.dixonDia(), false);
        List<DixonEspacio> andraConjunto = andraEnContexto();
        Map<String, List<DixonEspacio>> andraGrupos = andraOcupacion.dixonPorPabellon(andraConjunto);
        Map<DixonEspacio, DixonOcupacionServicio.DixonEstadoOcupacion> andraEstados = new LinkedHashMap<>();
        andraConjunto.forEach(andraEspacio -> andraEstados.put(andraEspacio, andraOcupacion.dixonEstado(andraEspacio, andraSel.dixonIndice(), andraSel.dixonDia())));
        long andraLibres = andraEstados.values().stream().filter(andraPosicionX -> andraPosicionX == DixonOcupacionServicio.DixonEstadoOcupacion.LIBRE).count();
        long andraPronto = andraEstados.values().stream().filter(andraPosicionX -> andraPosicionX == DixonOcupacionServicio.DixonEstadoOcupacion.POR_TERMINAR).count();
        long andraOcupados = andraEstados.values().stream().filter(andraPosicionX -> andraPosicionX == DixonOcupacionServicio.DixonEstadoOcupacion.OCUPADO).count();
        Circle andraPunto = new Circle(5);
        andraPunto.getStyleClass().add("live-dot");
        if ("now".equals(andraFranjaMapa) && andraAhora.dixonEnVivo()) {
            andraPunto.getStyleClass().add("on");
            if (!AndraMovimiento.andraReducido()) {
                javafx.animation.FadeTransition andraPulso = new javafx.animation.FadeTransition(Duration.millis(900), andraPunto);
                andraPulso.setFromValue(1);
                andraPulso.setToValue(0.35);
                andraPulso.setAutoReverse(true);
                andraPulso.setCycleCount(Animation.INDEFINITE);
                andraPulso.play();
            }
        }
        String andraEtiqueta = "now".equals(andraFranjaMapa) ? (andraAhora.dixonEnVivo() ? "Ahora" : "Próxima franja") : "Vista a las " + andraSel.dixonInicio();
        Label andraEtiqueta2 = AndraComponentes.andraEtiqueta(andraEtiqueta, "strong");
        Label andraDet = AndraComponentes.andraEtiqueta(" · " + andraContexto.dixonTextoSede() + " · " + andraSel.dixonInicio() + "–" + andraSel.dixonFin() + (andraSel.dixonDia() > 0 ? " de mañana" : ""));
        HBox andraIzquierda = new HBox(8, andraPunto, andraEtiqueta2, andraDet);
        andraIzquierda.setAlignment(Pos.CENTER_LEFT);
        HBox andraConteos = new HBox(6, AndraComponentes.andraEtiqueta(andraLibres + " libres", "mc", "free"), AndraComponentes.andraEtiqueta(andraPronto + " por terminar", "mc", "soon"),
                AndraComponentes.andraEtiqueta(andraOcupados + " ocupados", "mc", "busy"));
        HBox andraCab = new HBox(andraIzquierda, AndraComponentes.andraEspacio(), andraConteos);
        andraCab.getStyleClass().add("map-head");
        VBox andraCaja = new VBox(andraCab);
        andraCaja.getStyleClass().add("map-wrap");
        if (!andraAhora.dixonEnVivo() && "now".equals(andraFranjaMapa)) {
            andraCaja.getChildren().add(AndraComponentes.andraParrafo("Fuera del horario de atención (08:00–18:00). Te mostramos la primera franja "
                    + (andraAhora.dixonDia() > 0 ? "de mañana" : "de hoy") + ".", "hint"));
        }
        FlowPane andraFichas = AndraComponentes.andraFichas();
        ToggleGroup andraGrupoOpciones = new ToggleGroup();
        ToggleButton andraBotonAhora = new ToggleButton();
        AndraComponentes.andraFijar(andraBotonAhora, "Ahora");
        andraBotonAhora.setGraphic(new FontIcon("fth-clock"));
        andraBotonAhora.getStyleClass().add("chip");
        andraBotonAhora.setToggleGroup(andraGrupoOpciones);
        andraBotonAhora.setSelected("now".equals(andraFranjaMapa));
        andraBotonAhora.setOnAction(andraEvento -> {
            andraFranjaMapa = "now";
            andraRedibujar();
        });
        andraFichas.getChildren().add(andraBotonAhora);
        for (int andraIndice = 0; andraIndice < DixonCatalogoServicio.DIXON_FRANJAS.size(); andraIndice++) {
            int andraIndice2 = andraIndice;
            ToggleButton andraBotonOpcion = new ToggleButton(DixonCatalogoServicio.DIXON_FRANJAS.get(andraIndice));
            andraBotonOpcion.getStyleClass().add("chip");
            andraBotonOpcion.setToggleGroup(andraGrupoOpciones);
            andraBotonOpcion.setSelected(String.valueOf(andraIndice).equals(andraFranjaMapa));
            andraBotonOpcion.setOnAction(andraEvento -> {
                andraFranjaMapa = String.valueOf(andraIndice2);
                andraRedibujar();
            });
            andraFichas.getChildren().add(andraBotonOpcion);
        }
        andraCaja.getChildren().add(andraFichas);
        AndraRejilla andraCampus = new AndraRejilla(4, 260, 16);
        andraCampus.getStyleClass().add("campus");
        for (Map.Entry<String, List<DixonEspacio>> andraGrupo : andraGrupos.entrySet()) {
            HBox andraCabeceraGrupo = new HBox(AndraComponentes.andraIcono("fth-home"), AndraComponentes.andraLiteral(andraGrupo.getKey(), "strong"), AndraComponentes.andraEspacio(),
                    AndraComponentes.andraLiteral(String.valueOf(andraGrupo.getValue().size()), "hint"));
            andraCabeceraGrupo.getStyleClass().add("bld-h");
            AndraRejilla andraSalas = new AndraRejilla(4, 110, 8);
            for (DixonEspacio andraEspacio : andraGrupo.getValue()) {
                DixonOcupacionServicio.DixonEstadoOcupacion andraEstadoOcupacion = andraEstados.get(andraEspacio);
                String andraTexto = andraEstadoOcupacion == DixonOcupacionServicio.DixonEstadoOcupacion.POR_TERMINAR ? "Libre hasta " + andraSel.dixonFin() : andraEstadoOcupacion.dixonObtenerEtiqueta();
                Label andraCap = AndraComponentes.andraLiteral(DixonMontos.dixonMiles(andraEspacio.getDixonCapacidad()), "room-c");
                andraCap.setGraphic(new FontIcon("fth-user"));
                VBox andraSala = new VBox(2, AndraComponentes.andraLiteralParrafo(DixonOcupacionServicio.dixonNombreCorto(andraEspacio.getDixonNombre()), "room-n"),
                        AndraComponentes.andraEtiqueta(andraTexto, "room-s"), AndraComponentes.andraEspacioV(), andraCap);
                andraSala.getStyleClass().addAll("room", andraEstadoOcupacion.dixonObtenerClase());
                andraSala.setFocusTraversable(true);
                AndraComponentes.andraAccesible(andraSala, andraEspacio.getDixonNombre() + " · " + andraTexto);
                Tooltip.install(andraSala, new Tooltip(andraEspacio.getDixonNombre() + " · " + DixonIdiomaServicio.dixonT(andraTexto)));
                andraSala.setOnMouseClicked(andraEvento -> andraAccionesEspacio.andraDetalle(andraEspacio, AndraAccionesEspacio.AndraPrecarga.ANDRA_VACIA, this::andraRedibujar));
                andraSala.setOnKeyPressed(andraEvento -> {
                    if (andraEvento.getCode() == javafx.scene.input.KeyCode.ENTER) {
                        andraAccionesEspacio.andraDetalle(andraEspacio, AndraAccionesEspacio.AndraPrecarga.ANDRA_VACIA, this::andraRedibujar);
                    }
                });
                andraSalas.getChildren().add(andraSala);
            }
            VBox andraBld = new VBox(andraCabeceraGrupo, andraSalas);
            andraBld.getStyleClass().add("bld");
            andraCampus.getChildren().add(andraBld);
        }
        andraCaja.getChildren().add(andraCampus);
        HBox andraLeyenda = new HBox(14, andraLeyenda("free", "Libre"), andraLeyenda("soon", "Libre hasta el fin de la franja"), andraLeyenda("busy", "Ocupado"),
                AndraComponentes.andraEtiqueta("Plano esquemático · se actualiza cada minuto · toca un espacio para reservarlo", "hint"));
        andraLeyenda.setAlignment(Pos.CENTER_LEFT);
        andraCaja.getChildren().add(andraLeyenda);
        return andraCaja;
    }

    private HBox andraLeyenda(String andraClase, String andraTexto) {
        Region andraRegion = new Region();
        andraRegion.getStyleClass().addAll("lg", andraClase);
        HBox andraAlto = new HBox(6, andraRegion, AndraComponentes.andraEtiqueta(andraTexto, "small"));
        andraAlto.setAlignment(Pos.CENTER_LEFT);
        return andraAlto;
    }

    private VBox andraMisReservas() {
        List<DixonReserva> andraMias = andraReservas.dixonMias();
        Button andraNueva = AndraComponentes.andraBotonSm("Reservar espacio", "btn-secondary", "fth-plus", andraEvento -> {
            AndraComponentes.andraDesplazarHasta(andraDesplazable, andraFormulario);
            Object andraTipo = andraFormulario.getProperties().get("tipo");
            if (andraTipo instanceof Node andraNodo) {
                andraNodo.requestFocus();
            }
        });
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Mis reservas", andraMias.size() + " reserva" + (andraMias.size() == 1 ? "" : "s") + " registradas", andraNueva);
        Node andraCuerpo;
        if (andraMias.isEmpty()) {
            andraCuerpo = AndraComponentes.andraVacio("fth-calendar", "Aún no tienes reservas", "Busca un espacio disponible y resérvalo.");
        } else {
            List<Node> andraFilas = new ArrayList<>();
            for (DixonReserva andraReserva : andraMias) {
                VBox andraTextos = new VBox(2, AndraComponentes.andraLiteralParrafo(andraReserva.getDixonEspacio().getDixonNombre(), "row-title"),
                        AndraComponentes.andraEtiqueta(DixonFechas.dixonDmy(andraReserva.getDixonFecha()) + " · " + DixonFechas.dixonHora12(andraReserva.dixonObtenerInicio()) + " – " + DixonFechas.dixonHora12(andraReserva.dixonObtenerFin())
                                + " · Sede " + andraReserva.getDixonEspacio().getDixonSede().getDixonNombre(), "row-sub"));
                boolean andraActiva = andraReserva.getDixonEstado().dixonEsActiva();
                andraFilas.add(AndraComponentes.andraFila(andraTextos, AndraComponentes.andraPildora(andraReserva.getDixonEstado().dixonObtenerEtiqueta()),
                        AndraComponentes.andraBotonSm("Detalles", "btn-secondary", null, andraEvento -> andraDetalle(andraReserva)),
                        AndraComponentes.andraBotonIcono("fth-trash-2", (andraActiva ? "Cancelar" : "Eliminar") + " reserva " + andraReserva.getDixonEspacio().getDixonNombre(), andraEvento -> andraEliminar(andraReserva))));
            }
            andraCuerpo = AndraComponentes.andraLista(andraFilas);
        }
        return AndraComponentes.andraSeccion(andraCab, andraCuerpo);
    }

    private void andraDetalle(DixonReserva andraReserva) {
        DixonEspacio andraEspacio = andraReserva.getDixonEspacio();
        Object[] andraParesClaveValor = {"Espacio", AndraComponentes.andraLiteralParrafo(andraEspacio.getDixonNombre(), "dd"), "Sede", AndraComponentes.andraLiteralParrafo(andraEspacio.getDixonSede().getDixonNombreCompleto() + " · " + andraEspacio.getDixonEdificio(), "dd"),
                "Escuela", andraEspacio.getDixonEscuela() != null ? "EP " + andraEspacio.getDixonEscuela().getDixonNombre() : "Uso general", "Fecha", DixonFechas.dixonLargo(andraReserva.getDixonFecha()),
                "Horario", andraReserva.getDixonHorario(), "Asistentes", String.valueOf(andraReserva.getDixonAsistentes()), "Motivo", AndraComponentes.andraParrafo(andraReserva.getDixonMotivo(), "dd"),
                "Estado", AndraComponentes.andraPildora(andraReserva.getDixonEstado().dixonObtenerEtiqueta()), andraReserva.getDixonNota() != null ? "Observación" : null, andraReserva.getDixonNota() == null ? "" : AndraComponentes.andraParrafo(andraReserva.getDixonNota(), "dd")};
        andraNotificador.andraModal("Detalle de la reserva", AndraComponentes.andraKv(andraParesClaveValor), false, AndraComponentes.andraBoton("Cerrar", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    private void andraEliminar(DixonReserva andraReserva) {
        boolean andraActiva = andraReserva.getDixonEstado().dixonEsActiva();
        andraNotificador.andraConfirmar(andraActiva ? "¿Cancelar esta reserva?" : "¿Eliminar del historial?", andraActiva
                        ? "Se liberará " + andraReserva.getDixonEspacio().getDixonNombre() + " el " + DixonFechas.dixonDmy(andraReserva.getDixonFecha()) + ". Recuerda cancelar con al menos 30 minutos de anticipación."
                        : "La reserva de " + andraReserva.getDixonEspacio().getDixonNombre() + " se quitará de tu lista.",
                andraActiva ? "Cancelar reserva" : "Eliminar", true, () -> {
                    andraReservas.dixonEliminar(andraReserva.getDixonId());
                    andraRedibujar();
                    andraNotificador.andraExito(andraActiva ? "Reserva cancelada." : "Reserva eliminada del historial.");
                });
    }
}
