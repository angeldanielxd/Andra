package pe.andra.controller;

import javafx.animation.PauseTransition;
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

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraSolicitudesControlador extends AndraVistaBase {

    private static final int ANDRA_POR_PAGINA = 5;

    private final AndraExpedienteServicio andraExpedientes;
    private final AndraAccionesExpediente andraAcciones;
    private final AndraChatAsesor andraChat;

    private final AndraEstadoFiltro andraFiltros = new AndraEstadoFiltro();
    private String andraGrupo = "all";
    private int andraPagina = 1;
    private String andraAbierta;
    private TextField andraBusqueda;
    private HBox andraBarra;

    @Override
    public void andraEnfocarBusqueda() {
        if (andraBusqueda != null) {
            AndraComponentes.andraDesplazarHasta(andraDesplazable, andraBusqueda);
            andraBusqueda.requestFocus();
        }
    }

    private TextField andraBusqueda() {
        if (andraBusqueda == null) {
            andraBusqueda = new TextField();
            AndraComponentes.andraIndicacion(andraBusqueda, "Busca por código, trámite o fecha…");
            AndraComponentes.andraAccesible(andraBusqueda, "Buscar solicitudes");
            PauseTransition andraEspera = new PauseTransition(Duration.millis(250));
            andraEspera.setOnFinished(andraEvento -> {
                andraPagina = 1;
                int andraFlecha = andraBusqueda.getCaretPosition();
                andraRedibujar();
                Platform.runLater(() -> {
                    andraBusqueda.requestFocus();
                    andraBusqueda.positionCaret(andraFlecha);
                });
            });
            andraBusqueda.textProperty().addListener((andraObservable, andraTextoA, andraTextoB) -> andraEspera.playFromStart());
        }
        return andraBusqueda;
    }

    @Override
    protected List<Node> andraConstruir() {
        List<AndraExpediente> andraTodos = andraExpedientes.andraMios();
        String andraConsulta = andraBusqueda().getText() == null ? "" : andraBusqueda.getText().trim();
        List<AndraExpediente> andraLista = andraTodos.stream().filter(andraExpediente -> "all".equals(andraGrupo) || andraExpediente.getAndraGrupo().andraObtenerClave().equals(andraGrupo))
                .filter(andraExpediente -> pe.andra.util.AndraTextoPlano.andraContiene(AndraExpedienteServicio.andraTextoBusqueda(andraExpediente), andraConsulta)).toList();
        andraLista = AndraFiltroServicio.andraAplicar(andraLista, andraFiltros, AndraExpedienteServicio.andraPredicados(), andraExpediente -> andraExpediente.getAndraCodigo() + " " + andraExpediente.getAndraTitulo() + " " + andraExpediente.getAndraEstado());
        int andraPaginas = Math.max(1, (int) Math.ceil(andraLista.size() / (double) ANDRA_POR_PAGINA));
        andraPagina = Math.min(andraPagina, andraPaginas);
        List<AndraExpediente> andraVisibles = andraLista.subList(Math.min((andraPagina - 1) * ANDRA_POR_PAGINA, andraLista.size()), Math.min(andraPagina * ANDRA_POR_PAGINA, andraLista.size()));
        AndraExpediente andraObservado = andraTodos.stream().filter(andraExpediente -> andraExpediente.getAndraGrupo() == AndraGrupoExpediente.OBSERVADO).findFirst().orElse(null);

        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada(null, "Mis Solicitudes", "Rastrea el estado de tus expedientes, descarga constancias firmadas digitalmente y gestiona tus requerimientos universitarios.", null));
        StackPane andraBuscador = new StackPane(andraBusqueda(), AndraComponentes.andraIcono("fth-search"));
        StackPane.setAlignment(andraBuscador.getChildren().get(1), Pos.CENTER_LEFT);
        StackPane.setMargin(andraBuscador.getChildren().get(1), new Insets(0, 0, 0, 14));
        andraBuscador.getStyleClass().add("search");
        HBox.setHgrow(andraBuscador, Priority.ALWAYS);
        andraBarra = new HBox(12, AndraComponentes.andraPildoraPlano(andraTodos.size() + " registros", "neutral"), andraBuscador,
                AndraFiltros.andraBoton(andraFiltros, AndraExpedienteServicio.ANDRA_PREDEFINIDOS, null, andraNotificador, this::andraRedibujar),
                AndraComponentes.andraBoton("Nueva solicitud", "btn-primary", "fth-plus", andraEvento -> andraAcciones.andraNueva(this::andraRedibujar)));
        andraBarra.setAlignment(Pos.CENTER_LEFT);
        VBox andraCuerpo = new VBox(20, andraBarra, AndraFiltros.andraResumen(andraFiltros, AndraExpedienteServicio.ANDRA_PREDEFINIDOS, null, this::andraRedibujar), andraPestanas(andraTodos), andraIndicadores(andraTodos, andraObservado));
        if (andraObservado != null) {
            andraCuerpo.getChildren().add(andraAlerta(andraObservado));
        }
        VBox andraTarjetas = new VBox(16);
        if (andraVisibles.isEmpty()) {
            andraTarjetas.getChildren().add(AndraComponentes.andraVacio("fth-search", "No hay expedientes que coincidan", "Prueba con otro código o cambia el filtro."));
        }
        andraVisibles.forEach(andraExpediente -> andraTarjetas.getChildren().add(andraTarjeta(andraExpediente)));
        andraCuerpo.getChildren().addAll(andraTarjetas, AndraPaginador.andraCrear(andraPagina, andraPaginas, andraLista.size(), ANDRA_POR_PAGINA, "expedientes", andraNumero -> {
            andraPagina = andraNumero;
            andraRefrescar();
            Platform.runLater(() -> AndraComponentes.andraDesplazarHasta(andraDesplazable, andraBarra));
        }));
        VBox andraSeccion1 = AndraComponentes.andraSeccion(andraCuerpo);
        andraSeccion1.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion1);
        andraNodos.add(andraOficinas());
        return andraNodos;
    }

    private FlowPane andraPestanas(List<AndraExpediente> andraTodos) {
        FlowPane andraFichas = AndraComponentes.andraFichas();
        ToggleGroup andraGrupoOpciones = new ToggleGroup();
        String[][] andraGrupos = {{"all", "Todos los trámites", null}, {"tramite", "En trámite", "-color-warning"},
                {"listo", "Emitidos / Listos", "-color-success"}, {"observado", "Observados", "-color-error"}};
        for (String[] andraGrupo2 : andraGrupos) {
            long andraCantidad = "all".equals(andraGrupo2[0]) ? andraTodos.size() : andraTodos.stream().filter(andraExpediente -> andraExpediente.getAndraGrupo().andraObtenerClave().equals(andraGrupo2[0])).count();
            ToggleButton andraBotonOpcion = new ToggleButton();
            AndraComponentes.andraFijar(andraBotonOpcion, andraGrupo2[1] + " (" + andraCantidad + ")");
            if (andraGrupo2[2] != null) {
                Circle andraCirculo = new Circle(3.5);
                andraCirculo.setStyle("-fx-fill: " + andraGrupo2[2] + ";");
                andraBotonOpcion.setGraphic(andraCirculo);
            }
            andraBotonOpcion.getStyleClass().add("chip");
            andraBotonOpcion.setToggleGroup(andraGrupoOpciones);
            andraBotonOpcion.setSelected(andraGrupo2[0].equals(andraGrupo));
            andraBotonOpcion.setOnAction(andraEvento -> {
                andraGrupo = andraGrupo2[0];
                andraPagina = 1;
                andraRedibujar();
            });
            andraFichas.getChildren().add(andraBotonOpcion);
        }
        AndraComponentes.andraAccesible(andraFichas, "Filtrar trámites");
        return andraFichas;
    }

    private AndraRejilla andraIndicadores(List<AndraExpediente> andraTodos, AndraExpediente andraObservado) {
        long andraTramite = AndraExpedienteServicio.andraContar(andraTodos, AndraGrupoExpediente.TRAMITE);
        long andraListo = AndraExpedienteServicio.andraContar(andraTodos, AndraGrupoExpediente.LISTO);
        long andraObs = AndraExpedienteServicio.andraContar(andraTodos, AndraGrupoExpediente.OBSERVADO);
        AndraRejilla andraRejilla = new AndraRejilla(4, 200, 16);
        andraRejilla.getChildren().addAll(AndraComponentes.andraIndicador("Total expedientes", String.valueOf(andraTodos.size()), null, "Semestre Académico 2026-1"),
                AndraComponentes.andraIndicador("En evaluación", String.valueOf(andraTramite), "warning", "Plazo de respuesta: 48 hrs"),
                AndraComponentes.andraIndicador("Emitidos y listos", String.valueOf(andraListo), "success", "Disponibles con firma digital"),
                AndraComponentes.andraIndicador("Acción requerida", String.valueOf(andraObs), andraObs > 0 ? "error" : null, andraObservado != null ? "Subsanación por descanso médico" : "Sin pendientes"));
        return andraRejilla;
    }

    private HBox andraAlerta(AndraExpediente andraExpediente) {
        VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo("Atención requerida en Expediente " + andraExpediente.getAndraCodigo() + " (" + andraExpediente.getAndraTitulo() + ")", "alerta-titulo"),
                AndraComponentes.andraParrafo(andraExpediente.getAndraObservacion() != null && !andraExpediente.getAndraObservacion().contains("Servicio Médico (Bienestar") ? "Motivo: " + andraExpediente.getAndraObservacion()
                        : "Motivo de Jefatura: Falta visado del Servicio Médico (Edificio Administrativo). Plazo límite de vencimiento: 36 horas.", "small"));
        HBox.setHgrow(andraTextos, Priority.ALWAYS);
        HBox andraCajaHorizontal = new HBox(new FontIcon("fth-alert-triangle"), andraTextos,
                AndraComponentes.andraBotonSm("Subsanar ahora", "btn-primary", null, andraEvento -> andraAcciones.andraSubsanar(andraExpediente.getAndraCodigo(), this::andraRedibujar)));
        andraCajaHorizontal.getStyleClass().add("alerta");
        return andraCajaHorizontal;
    }

    private VBox andraTarjeta(AndraExpediente andraExpediente) {
        FlowPane andraMeta = new FlowPane();
        andraMeta.getStyleClass().add("exp-meta");
        for (AndraDatoExpediente andraDatoExpediente : andraExpediente.getAndraDatos()) {
            andraMeta.getChildren().add(new VBox(2, AndraComponentes.andraMayus(andraDatoExpediente.getAndraEtiqueta(), "label-caps"), AndraComponentes.andraParrafo(andraDatoExpediente.getAndraValor(), "v")));
        }
        VBox andraCodigo = new VBox(2, AndraComponentes.andraMayus("Expediente N.°", "label-caps"), AndraComponentes.andraLiteral(andraExpediente.getAndraCodigo(), "v", "codigo"));
        andraMeta.getChildren().add(andraCodigo);
        VBox andraIcono = new VBox(AndraAccionesExpediente.andraIconoExpediente(andraExpediente.getAndraIcono()), AndraComponentes.andraEtiqueta(andraExpediente.getAndraRotuloIcono(), "rotulo"));
        andraIcono.getStyleClass().add("exp-icon");
        if ("error".equals(andraExpediente.getAndraTono())) {
            andraIcono.getStyleClass().add("error");
        }
        VBox andraPrincipal = new VBox(new HBox(AndraComponentes.andraPildora(andraExpediente.getAndraEstado(), andraExpediente.getAndraTono())), AndraComponentes.andraParrafo(andraExpediente.getAndraTitulo(), "h3"));
        andraPrincipal.getStyleClass().add("exp-main");
        if (andraExpediente.getAndraDescripcion() != null) {
            andraPrincipal.getChildren().add(AndraComponentes.andraParrafo(andraExpediente.getAndraDescripcion(), "exp-desc"));
        }
        if (!andraExpediente.getAndraEtiquetas().isEmpty()) {
            FlowPane andraEtiquetas = AndraComponentes.andraFichas();
            andraExpediente.getAndraEtiquetas().forEach(andraTexto -> andraEtiquetas.getChildren().add(AndraComponentes.andraDistintivo(andraTexto)));
            andraPrincipal.getChildren().add(andraEtiquetas);
        }
        andraPrincipal.getChildren().add(AndraRastreadorCjsv.andraMini(AndraSeguimientoServicio.andraCalcular(andraExpediente)));
        if (andraExpediente.getAndraObservacion() != null && andraExpediente.getAndraGrupo() == AndraGrupoExpediente.OBSERVADO && andraExpediente.getAndraCodigo().equals(andraAbierta)) {
            VBox andraObs = new VBox(AndraComponentes.andraParrafo("Motivo de observación emitido por Jefatura Académica:", "obs-titulo"),
                    AndraComponentes.andraParrafo("“" + andraExpediente.getAndraObservacion() + "”", "obs-texto"));
            andraObs.getStyleClass().add("obs");
            andraPrincipal.getChildren().add(andraObs);
        }
        if (andraExpediente.getAndraNota() != null && !andraExpediente.getAndraNota().isBlank()) {
            andraPrincipal.getChildren().add(AndraComponentes.andraParrafo(andraExpediente.getAndraNota(), "hint"));
        }
        HBox.setHgrow(andraPrincipal, Priority.ALWAYS);
        VBox andraBotones = new VBox();
        andraBotones.getStyleClass().add("exp-acts");
        for (AndraAccionExpediente andraAccionExpedienteA : andraExpediente.getAndraAcciones()) {
            String andraEstilo = switch (andraAccionExpedienteA.getAndraEstilo()) {
                case "primary" -> "btn-primary";
                case "secondary" -> "btn-secondary";
                case "danger" -> "btn-danger";
                default -> "btn-tertiary";
            };
            String andraTexto2 = "obs-sol".equals(andraAccionExpedienteA.getAndraAccion()) && andraExpediente.getAndraCodigo().equals(andraAbierta) ? "Ocultar observaciones" : andraAccionExpedienteA.getAndraTexto();
            Button andraBotonB = AndraComponentes.andraBloque(AndraComponentes.andraBotonSm(andraTexto2, andraEstilo, "download-sol".equals(andraAccionExpedienteA.getAndraAccion()) ? "fth-download" : null,
                    andraEvento -> andraAcciones.andraEjecutar(andraAccionExpedienteA.getAndraAccion(), andraExpediente, this::andraRedibujar, () -> {
                        andraAbierta = andraExpediente.getAndraCodigo().equals(andraAbierta) ? null : andraExpediente.getAndraCodigo();
                        andraRedibujar();
                    })));
            andraBotones.getChildren().add(andraBotonB);
        }
        if (AndraConsultaAsesorServicio.andraDisponible(andraExpediente)) {
            andraBotones.getChildren().add(AndraComponentes.andraBloque(AndraComponentes.andraBotonSm("Consulta en línea con asesor", "btn-secondary", "fth-message-circle",
                    andraEvento -> andraChat.andraAbrir(andraExpediente))));
        }
        HBox andraCuerpo = new HBox(andraIcono, andraPrincipal, andraBotones);
        andraCuerpo.getStyleClass().add("exp-body");
        VBox andraTarjeta = new VBox(andraMeta, andraCuerpo);
        andraTarjeta.getStyleClass().add("exp");
        if (andraExpediente.isAndraDestacado()) {
            andraTarjeta.getStyleClass().add("flag");
        }
        return andraTarjeta;
    }

    private VBox andraOficinas() {
        VBox andraMesa = new VBox(12, AndraComponentes.andraParrafo("Mesa de Partes Virtual", "h3"),
                AndraComponentes.andraParrafo("Atención de lunes a viernes de 08:30 a 18:00 hrs. Los documentos ingresados fuera de horario se registran automáticamente en el siguiente día hábil institucional.", "muted", "small"),
                new HBox(AndraComponentes.andraBotonSm("Iniciar consulta de expediente", "btn-secondary", null, andraEvento -> andraEnfocarBusqueda())));
        andraMesa.getStyleClass().add("office");
        VBox andraPlazos = new VBox(12, AndraComponentes.andraParrafo("Plazos oficiales de tramitación", "h3"),
                AndraComponentes.andraVineta("Constancias automáticas con código QR: emisión inmediata dentro de 24 horas.", "muted", "small"),
                AndraComponentes.andraVineta("Certificados oficiales con firma SUNEDU / Reniec: de 3 a 5 días hábiles.", "muted", "small"),
                AndraComponentes.andraVineta("Justificaciones médicas y permisos extraordinarios: evaluación en 48 horas útiles.", "muted", "small"));
        andraPlazos.getStyleClass().add("office");
        AndraRejilla andraRejilla = new AndraRejilla(2, 320);
        andraRejilla.getChildren().addAll(andraMesa, andraPlazos);
        return AndraComponentes.andraSeccion(andraRejilla);
    }
}
