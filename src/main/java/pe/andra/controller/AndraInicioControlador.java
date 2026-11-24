package pe.andra.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.AndraEspacio;
import pe.andra.model.AndraPago;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraInicioControlador extends AndraVistaBase {

    private static final List<AndraCarrusel.AndraDatos> ANDRA_DIAPOSITIVAS = List.of(
            new AndraCarrusel.AndraDatos("Bienvenida", "Todo lo que necesitas de tu universidad, en un solo lugar",
                    "Trámites, reservas y préstamos con seguimiento en tiempo real.", "/img/carrusel/01-bienvenida-campus.jpg",
                    "Portada de ingreso al campus", 0.5, 0.5),
            new AndraCarrusel.AndraDatos("Documentos", "Constancias y certificados con firma digital",
                    "Descárgalos con código QR y validez ante SUNEDU.", "/img/carrusel/02-documentos-firma.jpg",
                    "Firma de convenio institucional con documentos oficiales", 0.5, 0.45),
            new AndraCarrusel.AndraDatos("Reservas", "Aulas, laboratorios y auditorios disponibles hoy",
                    "Consulta horarios libres y reserva en segundos.", "/img/carrusel/03-reservas-laboratorio.jpg",
                    "Estudiante trabajando en un laboratorio de cómputo", 0.4, 0.55),
            new AndraCarrusel.AndraDatos("Equipos", "Préstamo de sonido, instrumentos, deportes y tecnología",
                    "Revisa el inventario y el punto de entrega.", "/img/carrusel/04-equipos-prestamo.jpg",
                    "Guitarra, parlante, balón y laptop disponibles para préstamo", 0.5, 0.6),
            new AndraCarrusel.AndraDatos("Mis Solicitudes", "Sigue cada expediente paso a paso",
                    "Recibe alertas cuando un trámite requiera tu acción.", "/img/carrusel/05-solicitudes-archivo.jpg",
                    "Archivo de expedientes ordenados por número", 0.5, 0.4));

    private final AndraContextoServicio andraContexto;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraReservaServicio andraReservas;
    private final AndraPagoServicio andraPagos;
    private final AndraOcupacionServicio andraOcupacion;

    private AndraCarrusel andraCarrusel;
    private String andraEspacioElegido;
    private int andraDia;
    private String andraFranja;

    @Override
    public void andraSalir() {
        if (andraCarrusel != null) {
            andraCarrusel.andraDetener();
        }
    }

    @Override
    protected List<Node> andraConstruir() {
        if (andraCarrusel != null) {
            andraCarrusel.andraDetener();
        }
        List<Node> andraNodos = new ArrayList<>();
        andraCarrusel = new AndraCarrusel(ANDRA_DIAPOSITIVAS);
        AndraComponentes.andraAccesible(andraCarrusel, "Novedades");
        VBox andraArriba = AndraComponentes.andraSeccion(andraCarrusel);
        andraArriba.setPadding(new Insets(32, 24, 0, 24));
        Label andraIntro = AndraComponentes.andraParrafo("Solicita documentos, reserva aulas y laboratorios, gestiona préstamos de equipos y consulta tus solicitudes de manera rápida y sencilla.", "intro");
        andraIntro.setAlignment(Pos.CENTER);
        VBox.setMargin(andraIntro, new Insets(32, 0, 0, 0));
        HBox andraAcciones = new HBox(AndraComponentes.andraBoton("Explorar reservas", "btn-primary", null, andraEvento -> andraIr("reservas")),
                AndraComponentes.andraBoton("Ver mis solicitudes", "btn-secondary", null, andraEvento -> andraIr("solicitudes")));
        andraAcciones.getStyleClass().add("hero-ctas");
        VBox.setMargin(andraAcciones, new Insets(24, 0, 0, 0));
        andraArriba.getChildren().addAll(andraIntro, andraAcciones);
        andraArriba.setAlignment(Pos.TOP_CENTER);
        List<AndraPago> andraPendientes = andraPagos.andraPendientes();
        if (!andraPendientes.isEmpty()) {
            int andraCantidad = andraPendientes.size();
            String andraTexto = andraCantidad > 1 ? "s" : "";
            Text andraMarca = AndraComponentes.andraTexto("Andra Pay", "strong");
            andraMarca.setStyle("-fx-font-weight: 700;");
            Text andraResto = AndraComponentes.andraTexto(" · Tienes " + andraCantidad + " pago" + andraTexto + " pendiente" + andraTexto + ", incluida tu matrícula "
                    + andraPagos.andraMatricula().getAndraPeriodo() + ".");
            javafx.scene.text.TextFlow andraFlujo = AndraComponentes.andraFlujo(andraMarca, andraResto);
            HBox.setHgrow(andraFlujo, Priority.ALWAYS);
            HBox andraCartel = new HBox(andraFlujo, AndraComponentes.andraLiteral(AndraMontos.andraSoles(AndraPagoServicio.andraTotal(andraPendientes)), "strong"), new FontIcon("fth-arrow-right"));
            andraCartel.getStyleClass().add("pay-banner");
            andraCartel.setFocusTraversable(true);
            andraCartel.setOnMouseClicked(andraEvento -> andraIr("pay"));
            AndraComponentes.andraAccesible(andraCartel, "Ir a Andra Pay");
            VBox.setMargin(andraCartel, new Insets(24, 0, 0, 0));
            andraArriba.getChildren().add(andraCartel);
        }
        AndraOcupacionServicio.AndraFranjaBnps andraFranjaBnps = andraOcupacion.andraFranjaActual();
        List<AndraEspacio> andraEnContexto = andraEspaciosContexto();
        long andraLibres = andraOcupacion.andraLibres(andraEnContexto, andraFranjaBnps);
        HBox andraCarne = andraRapido(AndraComponentes.andraIcono("fth-credit-card"), "Mi carné digital", () -> andraIr("carne"));
        Circle andraPunto = new Circle(5);
        andraPunto.getStyleClass().addAll("live-dot", "on");
        HBox andraMapa = andraRapido(andraPunto, andraLibres + " espacios libres " + (andraFranjaBnps.andraEnVivo() ? "ahora" : "en la próxima franja"), () -> andraIr("mapa"));
        andraMapa.getChildren().add(new FontIcon("fth-arrow-right"));
        HBox andraFila = new HBox(12, andraCarne, andraMapa);
        andraFila.setAlignment(Pos.CENTER);
        VBox.setMargin(andraFila, new Insets(14, 0, 0, 0));
        andraArriba.getChildren().add(andraFila);
        andraNodos.add(andraArriba);
        andraNodos.add(andraServicios());
        andraNodos.add(andraBuscador(andraEnContexto));
        return andraNodos;
    }

    private HBox andraRapido(Node andraIcono, String andraTexto, Runnable andraAccion) {
        HBox andraAlto = new HBox(andraIcono, AndraComponentes.andraEtiqueta(andraTexto));
        andraAlto.getStyleClass().add("quick");
        andraAlto.setFocusTraversable(true);
        andraAlto.setOnMouseClicked(andraEvento -> andraAccion.run());
        andraAlto.setOnKeyPressed(andraEvento -> {
            if (andraEvento.getCode() == javafx.scene.input.KeyCode.ENTER) {
                andraAccion.run();
            }
        });
        return andraAlto;
    }

    private List<AndraEspacio> andraEspaciosContexto() {
        return andraCatalogo.andraEspacios().stream().filter(andraEspacio -> andraContexto.andraIncluye(andraEspacio.getAndraSede(), andraEspacio.getAndraEscuela())).toList();
    }

    private VBox andraServicios() {
        Label andraTitulo = AndraComponentes.andraParrafo("¿Qué necesitas realizar?", "title-1");
        andraTitulo.setAlignment(Pos.CENTER);
        andraTitulo.setMaxWidth(Double.MAX_VALUE);
        HBox andraCab = new HBox(andraTitulo);
        andraCab.setAlignment(Pos.CENTER);
        andraCab.setPadding(new Insets(0, 0, 24, 0));
        AndraRejilla andraRejilla = new AndraRejilla(2, 320);
        String[][] andraDatos = {
                {"fth-file-text", "Solicitar documento", "Solicita constancias y documentos académicos y realiza seguimiento de tu solicitud.", "Solicitar ahora", "documentos"},
                {"fth-calendar", "Reservar espacio", "Consulta la disponibilidad y reserva aulas, laboratorios y otros espacios universitarios.", "Ver disponibilidad", "reservas"},
                {"fth-box", "Solicitar equipo", "Consulta equipos disponibles y gestiona préstamos de laptops, proyectores y otros recursos.", "Ver equipos", "equipos"},
                {"fth-list", "Mis solicitudes", "Revisa el estado y el historial de tus solicitudes.", "Consultar", "solicitudes"}};
        for (String[] andraTextos : andraDatos) {
            StackPane andraIcono = new StackPane(new FontIcon(andraTextos[0]));
            andraIcono.getStyleClass().add("sicon");
            Button andraBoton = AndraComponentes.andraBotonSm(andraTextos[3], "btn-secondary", "fth-arrow-right", andraEvento -> andraIr(andraTextos[4]));
            andraBoton.setContentDisplay(ContentDisplay.RIGHT);
            VBox andraTarjeta = new VBox(12, andraIcono, AndraComponentes.andraParrafo(andraTextos[1], "h3"), AndraComponentes.andraParrafo(andraTextos[2], "service-texto"), AndraComponentes.andraEspacioV(), new HBox(andraBoton));
            andraTarjeta.getStyleClass().add("service");
            andraRejilla.getChildren().add(andraTarjeta);
        }
        VBox andraSeccion = AndraComponentes.andraSeccion(andraCab, andraRejilla);
        andraSeccion.setPadding(new Insets(64, 24, 32, 24));
        return andraSeccion;
    }

    private VBox andraBuscador(List<AndraEspacio> andraEspacios) {
        if (andraEspacios.stream().noneMatch(andraEspacio -> andraEspacio.getAndraCodigo().equals(andraEspacioElegido))) {
            andraEspacioElegido = andraEspacios.isEmpty() ? null : andraEspacios.get(0).getAndraCodigo();
            andraFranja = null;
        }
        LocalDate andraFecha = AndraFechas.andraHoy().plusDays(andraDia);
        List<AndraOpcion> andraOpciones = new ArrayList<>();
        andraEspacios.forEach(andraEspacio -> andraOpciones.add(AndraOpcion.andraDe(andraEspacio.getAndraCodigo(), andraEspacio.getAndraNombre() + (andraEspacio.getAndraEscuela() == null ? " (uso general)" : ""))));
        AndraCampo<ComboBox<AndraOpcion>> andraSelector = AndraFormulario.andraSelector("Espacio · Sede " + andraContexto.andraNombreSede(), andraOpciones, null, andraEspacioElegido);
        andraSelector.andraControl().valueProperty().addListener((andraObservable, andraOpcionA, andraOpcionB) -> {
            if (andraOpcionB != null && !andraOpcionB.andraValor().equals(andraEspacioElegido)) {
                andraEspacioElegido = andraOpcionB.andraValor();
                andraFranja = null;
                andraRedibujar();
            }
        });
        Button andraCambiar = AndraComponentes.andraEnlaceFlecha("Cambiar sede o escuela", andraEvento -> andraIr("ctx:sede"));
        andraCambiar.setGraphic(null);
        HBox andraMeta = new HBox(6, AndraComponentes.andraIcono("fth-book-open"), AndraComponentes.andraParrafo(andraContexto.andraEscuela() != null
                ? "Mostrando espacios de EP " + andraContexto.andraEscuela().getAndraNombre() + " y de uso general." : "Mostrando todos los espacios de la sede.", "hint"), andraCambiar);
        andraMeta.getStyleClass().add("meta-line");
        andraMeta.setAlignment(Pos.CENTER_LEFT);
        AndraSegmentado andraDias = new AndraSegmentado(false).andraOpcion("0", "Hoy", null, null).andraOpcion("1", "Mañana", null, null)
                .andraOpcion("2", "Pasado mañana", null, null).andraOpcion("cal", andraDia > 2 ? AndraFechas.andraCorto(andraFecha) : "Otra fecha", "fth-calendar", null)
                .andraSeleccionar(andraDia > 2 ? "cal" : String.valueOf(andraDia));
        andraDias.andraAlCambiar(andraValor -> {
            if ("cal".equals(andraValor)) {
                AndraCalendario.andraAbrir(andraNotificador, andraFecha, andraFecha2 -> {
                    andraDia = (int) ChronoUnit.DAYS.between(AndraFechas.andraHoy(), andraFecha2);
                    andraFranja = null;
                    andraRedibujar();
                });
                andraDias.andraSeleccionar(andraDia > 2 ? "cal" : String.valueOf(andraDia));
                return;
            }
            andraDia = Integer.parseInt(andraValor);
            andraFranja = null;
            andraRedibujar();
        });
        VBox andraCampoDia = new VBox(6, AndraComponentes.andraEtiqueta("Día", "flabel"), andraDias, AndraComponentes.andraEtiqueta(AndraFechas.andraLargo(andraFecha), "hint"));
        andraCampoDia.getStyleClass().add("field");
        VBox andraIzquierda = new VBox(16, new VBox(6, AndraComponentes.andraParrafo("Buscar espacio", "h2"),
                AndraComponentes.andraParrafo("Elige un espacio y un día para ver los horarios libres. Selecciona un horario disponible para reservarlo.", "muted", "small")),
                andraSelector.andraNodo(), andraMeta, andraCampoDia);
        AndraEspacio andraElegido = andraEspacios.stream().filter(andraEspacio -> andraEspacio.getAndraCodigo().equals(andraEspacioElegido)).findFirst().orElse(null);
        VBox andraFranjas = new VBox();
        andraFranjas.getStyleClass().add("slots");
        for (String andraTexto : AndraCatalogoServicio.ANDRA_FRANJAS) {
            boolean andraCorrecto = andraElegido != null && andraReservas.andraLibreEnInicio(andraElegido, andraDia, andraTexto);
            String andraFin = String.format("%02d:00", Integer.parseInt(andraTexto.substring(0, 2)) + 2);
            Label andraHora = AndraComponentes.andraLiteral(andraTexto + " – " + andraFin, "slot-time");
            HBox andraContenido = new HBox(andraHora, AndraComponentes.andraEspacio(), AndraComponentes.andraPildora(andraCorrecto ? "Disponible" : "Ocupado"));
            andraContenido.setAlignment(Pos.CENTER_LEFT);
            Button andraBotonB = new Button();
            andraBotonB.setGraphic(andraContenido);
            andraBotonB.getStyleClass().add("slot");
            andraBotonB.setMaxWidth(Double.MAX_VALUE);
            andraContenido.prefWidthProperty().bind(andraBotonB.widthProperty().subtract(34));
            andraBotonB.setDisable(!andraCorrecto);
            AndraComponentes.andraAccesible(andraBotonB, andraTexto + ", " + (andraCorrecto ? "disponible" : "ocupado"));
            if (andraTexto.equals(andraFranja)) {
                andraBotonB.getStyleClass().add("on");
            }
            andraBotonB.setOnAction(andraEvento -> {
                andraFranja = andraTexto.equals(andraFranja) ? null : andraTexto;
                andraRedibujar();
            });
            andraFranjas.getChildren().add(andraBotonB);
        }
        String andraFinFranja = andraFranja == null ? "" : String.format("%02d:00", Integer.parseInt(andraFranja.substring(0, 2)) + 2);
        Button andraReservar = AndraComponentes.andraBloque(AndraComponentes.andraBoton(andraFranja == null ? "Reservar espacio" : "Reservar " + andraFranja + " – " + andraFinFranja,
                "btn-primary", null, andraEvento -> andraConfirmar(andraElegido, andraFecha)));
        andraReservar.setDisable(andraFranja == null || andraElegido == null);
        VBox.setMargin(andraReservar, new Insets(16, 0, 0, 0));
        VBox andraDerecha = new VBox(andraFranjas, andraReservar);
        GridPane andraPanel = new GridPane();
        andraPanel.setHgap(48);
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(50);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(50);
        andraPanel.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        andraPanel.add(andraIzquierda, 0, 0);
        andraPanel.add(andraDerecha, 1, 0);
        GridPane.setValignment(andraIzquierda, javafx.geometry.VPos.TOP);
        VBox andraCaja = new VBox(andraPanel);
        andraCaja.getStyleClass().add("panel");
        return AndraComponentes.andraSeccion(andraCaja);
    }

    private void andraConfirmar(AndraEspacio andraEspacio, LocalDate andraFecha) {
        if (andraEspacio == null || andraFranja == null) {
            return;
        }
        String andraHorario = AndraCatalogoServicio.ANDRA_HORARIOS.get(AndraCatalogoServicio.ANDRA_FRANJAS.indexOf(andraFranja));
        andraNotificador.andraConfirmar("Confirmar reserva", andraEspacio.getAndraNombre() + " · " + AndraFechas.andraLargo(andraFecha) + " · " + andraHorario
                + ". La solicitud quedará pendiente de aprobación y podrás verla en Reservas.", "Confirmar reserva", false, () -> {
            if (andraIntentar(() -> andraReservas.andraReservaRapida(andraEspacio, andraFecha, andraFranja))) {
                andraFranja = null;
                andraRedibujar();
                andraNotificador.andraExito("Reserva enviada. Puedes verla en Reservas.");
            }
        });
    }
}
