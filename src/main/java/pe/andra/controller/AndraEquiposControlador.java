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
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.DixonFechas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraEquiposControlador extends AndraVistaBase {

    private final DixonContextoServicio andraContexto;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonEquipoServicio andraEquipos;
    private final DixonSesionServicio andraSesion;
    private final AndraAccionesEquipo andraAccionesEquipo;
    private final DixonDocumentoGeneradoServicio andraGenerador;
    private final AndraDescargas andraDescargas;

    private final DixonEstadoFiltro andraFiltros = new DixonEstadoFiltro();
    private String andraCategoria = "";
    private String andraPunto = "";
    private String andraFicha = "all";
    private LocalDate andraFechaUso;
    private String andraDuracion = "4h";
    private Timeline andraReloj;
    private VBox andraCatalogoSeccion;

    @Override
    public void andraSalir() {
        if (andraReloj != null) {
            andraReloj.stop();
        }
    }

    @Override
    protected List<Node> andraConstruir() {
        andraSalir();
        if (andraFechaUso == null || andraFechaUso.isBefore(DixonFechas.dixonHoy())) {
            andraFechaUso = DixonFechas.dixonHoy();
        }
        List<DixonEquipo> andraConjunto = andraEquipos.dixonEnContexto();
        List<String> andraPuntos = new ArrayList<>(new LinkedHashSet<>(andraConjunto.stream().map(DixonEquipo::getDixonPuntoEntrega).toList()));
        if (!andraPunto.isBlank() && !andraPuntos.contains(andraPunto)) {
            andraPunto = "";
        }
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada(null, "Equipos", "Solicita equipos propios de la universidad: sonido, instrumentos, implementos deportivos, tecnología e instrumental de laboratorio por escuela.",
                AndraComponentes.andraLineaContexto(andraContexto.dixonTextoSede(), andraContexto.dixonTextoEscuela(), "ctx:sede")));
        andraNodos.add(andraCategorias(andraConjunto));
        List<DixonEquipo> andraBase = andraConjunto.stream().filter(andraEquipo -> andraCategoria.isBlank() || andraEquipo.getDixonCategoria().getDixonCodigo().equals(andraCategoria))
                .filter(andraEquipo -> andraPunto.isBlank() || andraEquipo.getDixonPuntoEntrega().equals(andraPunto)).toList();
        andraNodos.add(andraBuscador(andraBase, andraPuntos));
        andraCatalogoSeccion = andraCatalogo(andraBase);
        andraNodos.add(andraCatalogoSeccion);
        andraNodos.add(andraActivo());
        andraNodos.add(andraHistorial());
        return andraNodos;
    }

    private VBox andraCategorias(List<DixonEquipo> andraConjunto) {
        List<Node> andraAcciones = new ArrayList<>();
        if (!andraCategoria.isBlank()) {
            andraAcciones.add(AndraComponentes.andraBotonSm("Ver todas", "btn-tertiary", null, andraEvento -> {
                andraCategoria = "";
                andraRedibujar();
            }));
        }
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Explorar por categoría", null, andraAcciones.toArray(new Node[0]));
        AndraRejilla andraRejilla = new AndraRejilla(5, 170, 12);
        for (DixonCategoriaEquipo andraCategoriaEquipo : andraCatalogo.dixonCategorias()) {
            int andraCantidad = andraEquipos.dixonDisponiblesEnCategoria(andraConjunto, andraCategoriaEquipo);
            ToggleButton andraBotonOpcion = new ToggleButton();
            HBox andraArriba = new HBox(AndraComponentes.andraIcono(AndraIlustraciones.ANDRA_ICONOS_CATEGORIA.getOrDefault(andraCategoriaEquipo.getDixonIcono(), "fth-box"), "cat-icono"), AndraComponentes.andraEspacio(),
                    AndraComponentes.andraPildoraPlano(andraCantidad + " disponibles", andraCantidad > 0 ? "success" : "neutral"));
            andraArriba.setAlignment(Pos.CENTER_LEFT);
            VBox andraContenido = new VBox(8, andraArriba, AndraComponentes.andraParrafo(andraCategoriaEquipo.getDixonNombre(), "cat-nombre"), AndraComponentes.andraParrafo(andraCategoriaEquipo.getDixonDescripcion(), "muted", "small"));
            andraBotonOpcion.setGraphic(andraContenido);
            andraContenido.prefWidthProperty().bind(andraBotonOpcion.widthProperty().subtract(34));
            andraBotonOpcion.getStyleClass().add("cat-tile");
            andraBotonOpcion.setMaxWidth(Double.MAX_VALUE);
            andraBotonOpcion.setMaxHeight(Double.MAX_VALUE);
            andraBotonOpcion.setSelected(andraCategoriaEquipo.getDixonCodigo().equals(andraCategoria));
            andraBotonOpcion.setOnAction(andraEvento -> {
                andraCategoria = andraCategoriaEquipo.getDixonCodigo().equals(andraCategoria) ? "" : andraCategoriaEquipo.getDixonCodigo();
                andraRedibujar();
            });
            andraRejilla.getChildren().add(andraBotonOpcion);
        }
        VBox andraSeccion = AndraComponentes.andraSeccion(andraCab, andraRejilla);
        andraSeccion.getStyleClass().add("sin-arriba");
        return andraSeccion;
    }

    private VBox andraBuscador(List<DixonEquipo> andraBase, List<String> andraPuntos) {
        AndraFormulario andraFormulario = new AndraFormulario();
        List<AndraOpcion> andraCats = new ArrayList<>();
        andraCatalogo.dixonCategorias().forEach(andraCategoriaEquipo -> andraCats.add(AndraOpcion.andraDe(andraCategoriaEquipo.getDixonCodigo(), andraCategoriaEquipo.getDixonNombre())));
        AndraCampo<ComboBox<AndraOpcion>> andraCat = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Categoría o tipo de equipo", andraCats, "Todos los equipos", andraCategoria));
        AndraCampo<ComboBox<AndraOpcion>> andraPto = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Punto de entrega · Sede " + andraContexto.dixonNombreSede(), AndraOpcion.andraLista(andraPuntos),
                "Cualquier punto de la sede", andraPunto));
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Fecha requerida de uso", andraFechaUso, DixonFechas.dixonHoy(), null)).andraRequerido("Elige la fecha de uso.");
        andraFecha.andraAyuda(DixonFechas.dixonLargo(andraFechaUso));
        AndraCampo<ComboBox<AndraOpcion>> andraDur = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Duración estimada del préstamo", AndraOpcion.andraPares(DixonEquipoServicio.DIXON_DURACIONES_LARGAS), null, andraDuracion));
        long andraNAll = andraBase.size();
        long andraNAhora = andraBase.stream().filter(DixonEquipo::dixonEsDisponibleAhora).count();
        FlowPane andraFichas = AndraComponentes.andraFichas();
        ToggleGroup andraGrupoOpciones = new ToggleGroup();
        for (String[] andraTextos : new String[][]{{"all", "Todos (" + andraNAll + ")"}, {"now", "Disponibles ahora (" + andraNAhora + ")"}, {"res", "Requiere reserva (" + (andraNAll - andraNAhora) + ")"}}) {
            ToggleButton andraBotonOpcion = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcion, andraTextos[1]);
            andraBotonOpcion.getStyleClass().add("chip");
            andraBotonOpcion.setToggleGroup(andraGrupoOpciones);
            andraBotonOpcion.setSelected(andraTextos[0].equals(andraFicha));
            andraBotonOpcion.setOnAction(andraEvento -> {
                andraFicha = andraTextos[0];
                andraRedibujar();
            });
            andraFichas.getChildren().add(andraBotonOpcion);
        }
        AndraComponentes.andraAccesible(andraFichas, "Filtrar por disponibilidad");
        HBox.setHgrow(andraFichas, Priority.ALWAYS);
        Button andraConsultar = AndraComponentes.andraBoton("Consultar inventario", "btn-primary", null, andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            andraCategoria = AndraFormulario.andraValor(andraCat.andraControl());
            andraPunto = AndraFormulario.andraValor(andraPto.andraControl());
            andraFechaUso = andraFecha.andraControl().getValue();
            andraDuracion = AndraFormulario.andraValor(andraDur.andraControl());
            andraRefrescar();
            Platform.runLater(() -> AndraComponentes.andraDesplazarHasta(andraDesplazable, andraCatalogoSeccion));
            andraNotificador.andraExito("Inventario actualizado.");
        });
        HBox andraPie = new HBox(andraFichas, andraConsultar);
        andraPie.getStyleClass().add("form-foot");
        VBox andraPanel = new VBox(20, AndraComponentes.andraParrafo("Buscar equipo y verificar disponibilidad", "h2"),
                AndraFormulario.andraRejilla(2, andraCat.andraNodo(), andraPto.andraNodo(), andraFecha.andraNodo(), andraDur.andraNodo()), andraPie);
        andraPanel.getStyleClass().add("panel");
        return AndraComponentes.andraSeccion(andraPanel);
    }

    private VBox andraCatalogo(List<DixonEquipo> andraBase) {
        List<DixonEquipo> andraPorFicha = andraBase.stream().filter(andraEquipo -> "all".equals(andraFicha) || ("now".equals(andraFicha) ? andraEquipo.dixonEsDisponibleAhora() : !andraEquipo.dixonEsDisponibleAhora())).toList();
        List<DixonEquipo> andraLista = DixonFiltroServicio.dixonAplicar(andraPorFicha, andraFiltros, andraEquipos.dixonPredicados(),
                andraEquipo -> andraEquipo.getDixonNombre() + " " + andraEquipo.getDixonEspecificaciones() + " " + andraEquipo.getDixonUbicacion() + " " + andraEquipo.getDixonRotulo());
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Catálogo de equipamiento", andraLista.size() + " equipo" + (andraLista.size() == 1 ? "" : "s") + " · uso el "
                + DixonFechas.dixonDmy(andraFechaUso) + " · pasa el cursor y elige uno para ver detalles",
                AndraFiltros.andraBoton(andraFiltros, DixonEquipoServicio.DIXON_PREDEFINIDOS, null, andraNotificador, this::andraRedibujar));
        Node andraContenido;
        if (andraLista.isEmpty()) {
            andraContenido = AndraComponentes.andraVacio("fth-search", "No hay equipos con esos filtros",
                    "Cambia la categoría, el punto de entrega o la sede (ahora: " + andraContexto.dixonNombreSede() + ").");
        } else {
            AndraGaleria andraGaleria = new AndraGaleria();
            for (int andraIndice = 0; andraIndice < andraLista.size(); andraIndice++) {
                andraGaleria.getChildren().add(andraAccionesEquipo.andraTarjeta(andraLista.get(andraIndice), andraIndice, this::andraRedibujar, andraFechaUso, andraDuracion));
            }
            andraContenido = andraGaleria;
        }
        return AndraComponentes.andraSeccion(andraCab, AndraFiltros.andraResumen(andraFiltros, DixonEquipoServicio.DIXON_PREDEFINIDOS, null, this::andraRedibujar), andraContenido);
    }

    private VBox andraActivo() {
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Préstamo activo en posesión", null);
        var andraActivo = andraEquipos.dixonActivo();
        if (andraActivo.isEmpty()) {
            return AndraComponentes.andraSeccion(andraCab, AndraComponentes.andraVacio("fth-box", "No tienes préstamos activos", "Cuando retires un equipo aparecerá aquí con su hora límite."));
        }
        DixonPrestamoActivo andraPrestamoActivo = andraActivo.get();
        DixonPersona andraEstudianteActual = andraSesion.dixonEstudiante();
        boolean andraVencido = andraPrestamoActivo.getDixonLimite().isBefore(LocalDateTime.now(DixonFechas.dixonReloj()));
        VBox andraTitulo = new VBox(4, AndraComponentes.andraLiteralParrafo("Préstamo en custodia N.° " + andraPrestamoActivo.getDixonCodigo() + " • " + andraPrestamoActivo.getDixonEquipo(), "loan-titulo"),
                AndraComponentes.andraParrafo("Retirado " + andraPrestamoActivo.getDixonRetiro() + " • Titular: " + andraEstudianteActual.getDixonNombre() + " (Código: " + andraEstudianteActual.getDixonCodigo() + ")", "muted", "small"));
        HBox.setHgrow(andraTitulo, Priority.ALWAYS);
        Label andraEstado = andraVencido ? AndraComponentes.andraPildora("Devolución vencida", "warning")
                : AndraComponentes.andraPildora(andraPrestamoActivo.getDixonIncidencia() != null ? "En uso • Incidencia reportada" : "En uso • A tiempo", "success");
        HBox andraArriba = new HBox(andraTitulo, andraEstado);
        Label andraLimite = AndraComponentes.andraEtiqueta(DixonEquipoServicio.dixonTextoLimite(andraPrestamoActivo.getDixonLimite(), LocalDateTime.now(DixonFechas.dixonReloj())), "loan-deadline");
        andraReloj = new Timeline(new KeyFrame(Duration.seconds(30), andraEvento -> AndraComponentes.andraFijar(andraLimite, DixonEquipoServicio.dixonTextoLimite(andraPrestamoActivo.getDixonLimite(), LocalDateTime.now(DixonFechas.dixonReloj())))));
        andraReloj.setCycleCount(Animation.INDEFINITE);
        andraReloj.play();
        VBox andraInformacion = new VBox(4, AndraComponentes.andraMayus("Hora límite de devolución", "label-caps"), andraLimite,
                AndraComponentes.andraParrafo("Lugar de devolución: " + andraPrestamoActivo.getDixonLugar(), "muted", "small"));
        HBox.setHgrow(andraInformacion, Priority.ALWAYS);
        Button andraRenovar = AndraComponentes.andraBoton(andraPrestamoActivo.isDixonRenovado() ? "Tiempo renovado" : "Renovar tiempo (+2h)", "btn-secondary", "fth-refresh-cw", andraEvento ->
                andraNotificador.andraConfirmar("Renovar tiempo de préstamo", "Se agregarán 2 horas a la hora límite de devolución. Solo puedes renovar una vez por préstamo.",
                        "Renovar +2h", false, () -> {
                            if (andraIntentar(andraEquipos::dixonRenovar)) {
                                andraRedibujar();
                                andraNotificador.andraExito("Préstamo renovado. Nueva hora límite actualizada.");
                            }
                        }));
        andraRenovar.setDisable(andraPrestamoActivo.isDixonRenovado());
        Button andraReportar = AndraComponentes.andraBoton("Reportar daño / falla", "btn-secondary", "fth-tool", andraEvento -> andraReportar(andraPrestamoActivo));
        HBox andraCaja = new HBox(andraInformacion, new HBox(8, andraRenovar, andraReportar));
        andraCaja.getStyleClass().add("loan-box");
        VBox andraPrestamo = new VBox(andraArriba, andraCaja);
        andraPrestamo.getStyleClass().add("loan");
        return AndraComponentes.andraSeccion(andraCab, andraPrestamo);
    }

    private void andraReportar(DixonPrestamoActivo andraPrestamoActivo) {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Tipo de incidencia", AndraOpcion.andraLista(DixonEquipoServicio.DIXON_TIPOS_INCIDENCIA), "Seleccionar", null))
                .andraRequerido("Elige el tipo de incidencia.");
        AndraCampo<TextArea> andraDesc = andraFormulario.andraAgregar(AndraFormulario.andraArea("Describe lo ocurrido", "Ej. El proyector se apaga después de 10 minutos de uso."))
                .andraRequerido("Describe la incidencia.").andraMinimo(15);
        AndraCampo<AndraFormulario.AndraArchivo> andraFoto = andraFormulario.andraAgregar(AndraFormulario.andraArchivo("Foto de evidencia (opcional)", "*.jpg *.png *.pdf", null, andraNotificador, null));
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Préstamo " + andraPrestamoActivo.getDixonCodigo() + " · " + andraPrestamoActivo.getDixonEquipo() + ". Se levantará un acta de conformidad técnica.", "muted", "small"),
                andraTipo.andraNodo(), andraDesc.andraNodo(), andraFoto.andraNodo());
        andraNotificador.andraModal("Reportar daño o falla", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Enviar reporte", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar() && andraIntentar(() -> andraEquipos.dixonReportar(AndraFormulario.andraValor(andraTipo.andraControl()), andraDesc.andraControl().getText()))) {
                        andraNotificador.andraCerrarModal();
                        andraRedibujar();
                        andraNotificador.andraExito("Reporte enviado. Un técnico te contactará.");
                    }
                }));
    }

    private VBox andraHistorial() {
        HBox andraCab = AndraComponentes.andraCabeceraSeccion("Historial de préstamos", null);
        List<DixonPrestamo> andraLista = andraEquipos.dixonHistorial();
        if (andraLista.isEmpty()) {
            return AndraComponentes.andraSeccion(andraCab, AndraComponentes.andraVacio("fth-box", "Sin historial", "Tus préstamos aparecerán aquí."));
        }
        List<Node> andraFilas = new ArrayList<>();
        DixonPersona andraEstudianteActual = andraSesion.dixonEstudiante();
        for (DixonPrestamo andraAlto : andraLista) {
            HBox andraTit = new HBox(6, AndraComponentes.andraParrafo(andraAlto.getDixonNombre(), "row-title"), AndraComponentes.andraLiteral("(" + andraAlto.getDixonCodigo() + ")", "muted"));
            andraTit.setAlignment(Pos.CENTER_LEFT);
            VBox andraTextos = new VBox(2, andraTit, AndraComponentes.andraParrafo(andraAlto.getDixonDetalle(), "row-sub"));
            boolean andraActivo = andraAlto.getDixonEstado().dixonEsEnCurso();
            andraFilas.add(AndraComponentes.andraFila(andraTextos, AndraComponentes.andraPildora(andraAlto.getDixonEstado().dixonObtenerEtiqueta()),
                    AndraComponentes.andraBotonSm("Ver cargo", "btn-secondary", null, andraEvento -> andraCargo(andraAlto, andraEstudianteActual)),
                    AndraComponentes.andraBotonIcono("fth-trash-2", (andraActivo ? "Cancelar solicitud" : "Eliminar del historial") + " " + andraAlto.getDixonCodigo(), andraEvento -> andraEliminar(andraAlto))));
        }
        return AndraComponentes.andraSeccion(andraCab, AndraComponentes.andraLista(andraFilas));
    }

    private void andraCargo(DixonPrestamo andraAlto, DixonPersona andraEstudianteActual) {
        andraNotificador.andraModal("Cargo de préstamo", AndraComponentes.andraKv("Código", AndraComponentes.andraLiteral(andraAlto.getDixonCodigo(), "dd"), "Equipo", AndraComponentes.andraParrafo(andraAlto.getDixonNombre(), "dd"),
                        "Detalle", AndraComponentes.andraParrafo(andraAlto.getDixonDetalle(), "dd"), "Titular", AndraComponentes.andraLiteral(andraEstudianteActual.getDixonNombre() + " (" + andraEstudianteActual.getDixonCodigo() + ")", "dd"),
                        "Estado", AndraComponentes.andraPildora(andraAlto.getDixonEstado().dixonObtenerEtiqueta())), false,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Descargar cargo", "btn-primary", "fth-download", andraEvento -> andraDescargas.andraGuardarPdf("Cargo " + andraAlto.getDixonCodigo(),
                        () -> andraGenerador.dixonPdfSimple("Cargo de préstamo " + andraAlto.getDixonCodigo(), andraAlto.getDixonNombre() + " · " + andraAlto.getDixonDetalle()))));
    }

    private void andraEliminar(DixonPrestamo andraAlto) {
        boolean andraActivo = andraAlto.getDixonEstado().dixonEsEnCurso();
        andraNotificador.andraConfirmar(andraActivo ? "¿Cancelar esta solicitud de préstamo?" : "¿Eliminar del historial?",
                andraActivo ? "La solicitud " + andraAlto.getDixonCodigo() + " se cancelará y el equipo quedará libre para otros usuarios."
                        : "El registro " + andraAlto.getDixonCodigo() + " se quitará de tu historial.",
                andraActivo ? "Cancelar solicitud" : "Eliminar", true, () -> {
                    andraEquipos.dixonEliminar(andraAlto.getDixonId());
                    andraRedibujar();
                    andraNotificador.andraExito(andraActivo ? "Solicitud cancelada." : "Registro eliminado.");
                });
    }
}
