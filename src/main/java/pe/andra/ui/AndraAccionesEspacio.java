package pe.andra.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.AndraEspacio;
import pe.andra.model.AndraTipoEspacio;
import pe.andra.model.AndraTipoFavorito;
import pe.andra.service.AndraCatalogoServicio;
import pe.andra.service.AndraFavoritoServicio;
import pe.andra.service.AndraReservaServicio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AndraAccionesEspacio {

    public record AndraPrecarga(LocalDate andraFecha, String andraHorario, Integer andraAsistentes) {
        public static final AndraPrecarga ANDRA_VACIA = new AndraPrecarga(null, null, null);
    }

    private static final Map<AndraTipoEspacio, List<String>> ANDRA_EQUIPAMIENTO = Map.of(
            AndraTipoEspacio.AUDITORIO, List.of("Sistema de sonido profesional", "Proyector láser y pantalla", "Escenario con iluminación",
                    "Butacas numeradas", "Acceso para silla de ruedas"),
            AndraTipoEspacio.LABORATORIO, List.of("Estaciones de trabajo", "Proyector y pizarra", "Internet de alta velocidad",
                    "Software especializado de la escuela"),
            AndraTipoEspacio.AULA, List.of("Proyector multimedia", "Pizarra acrílica", "Carpetas individuales", "Wi-Fi"),
            AndraTipoEspacio.SALA, List.of("Mesa de reuniones", "Pantalla de 65\"", "Videoconferencia", "Pizarra"),
            AndraTipoEspacio.TALLER, List.of("Mesas de trabajo", "Equipos y herramientas de la especialidad",
                    "Equipo de protección personal", "Supervisión de técnico"));

    private final AndraNotificador andraNotificador;
    private final AndraReservaServicio andraReservas;
    private final AndraFavoritoServicio andraFavoritos;
    private final AndraCatalogoServicio andraCatalogo;

    public AndraTarjetaGaleria andraTarjeta(AndraEspacio andraEspacio, String andraEstado, int andraIndice, boolean andraDestacado, AndraPrecarga andraPrecarga, Runnable andraAlCambiar) {
        Node[] andraGiro = new Node[1];
        Pane andraArte = AndraIlustraciones.andraMarcoEspacio(andraEspacio, andraGiro);
        boolean andraFav = andraFavoritos.andraEs(AndraTipoFavorito.ESPACIO, andraEspacio.getAndraCodigo());
        Button andraEstrella = AndraComponentes.andraEstrella(andraFav, andraEspacio.getAndraNombre(), () -> {
            boolean andraAhora = andraFavoritos.andraAlternar(AndraTipoFavorito.ESPACIO, andraEspacio.getAndraCodigo());
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        Label andraCap = AndraComponentes.andraLiteral(AndraMontos.andraMiles(andraEspacio.getAndraCapacidad()), "tile-cap");
        andraCap.setGraphic(new FontIcon("fth-user"));
        return new AndraTarjetaGaleria(andraArte, andraGiro[0], andraEspacio.getAndraTipo().andraObtenerEtiqueta(), AndraComponentes.andraPildora(andraEstado), andraEspacio.getAndraNombre(),
                AndraComponentes.andraMetaLinea("fth-map-pin", "Sede " + andraEspacio.getAndraSede().getAndraNombre() + " · " + andraEspacio.getAndraEdificio()), andraCap,
                andraDestacado, andraDestacado ? "Auditorio principal" : null, andraEstrella, "Ver detalles de " + andraEspacio.getAndraNombre(),
                () -> andraDetalle(andraEspacio, andraPrecarga, andraAlCambiar), andraIndice);
    }

    public void andraDetalle(AndraEspacio andraEspacio, AndraPrecarga andraPrecarga, Runnable andraAlCambiar) {
        String andraEstado = andraEspacio.isAndraFueraDeServicio() ? "Ocupado" : "Disponible";
        boolean andraFav = andraFavoritos.andraEs(AndraTipoFavorito.ESPACIO, andraEspacio.getAndraCodigo());
        Node[] andraGiro = new Node[1];
        Pane andraArte = AndraIlustraciones.andraMarcoEspacio(andraEspacio, andraGiro);
        StackPane andraDet = new StackPane(andraArte);
        andraDet.getStyleClass().add("det-art");
        andraDet.setPrefHeight(280);
        andraDet.setMinHeight(220);
        javafx.scene.shape.Rectangle andraRectangulo = new javafx.scene.shape.Rectangle();
        andraRectangulo.setArcWidth(28);
        andraRectangulo.setArcHeight(28);
        andraRectangulo.widthProperty().bind(andraDet.widthProperty());
        andraRectangulo.heightProperty().bind(andraDet.heightProperty());
        andraDet.setClip(andraRectangulo);

        FlowPane andraFichas = AndraComponentes.andraFichas(AndraComponentes.andraPildora(andraEstado), AndraComponentes.andraDistintivo(andraEspacio.getAndraTipo().andraObtenerEtiqueta()),
                AndraComponentes.andraDistintivo(andraEspacio.getAndraEscuela() != null ? "EP " + andraEspacio.getAndraEscuela().getAndraNombre() : "Uso general"));
        GridPane andraParesClaveValor = AndraComponentes.andraKv("Sede", AndraComponentes.andraLiteralParrafo(andraEspacio.getAndraSede().getAndraNombreCompleto(), "dd"),
                "Ubicación", AndraComponentes.andraLiteralParrafo(andraEspacio.getAndraEdificio(), "dd"),
                "Capacidad", AndraMontos.andraMiles(andraEspacio.getAndraCapacidad()) + " personas");
        VBox andraEquipo = new VBox(6);
        for (String andraTexto : ANDRA_EQUIPAMIENTO.getOrDefault(andraEspacio.getAndraTipo(), List.of())) {
            Label andraEtiqueta = AndraComponentes.andraParrafo(andraTexto, "small");
            andraEtiqueta.setGraphic(AndraComponentes.andraIcono("fth-check", "azul"));
            andraEquipo.getChildren().add(andraEtiqueta);
        }
        VBox andraIzquierda = new VBox(12, andraFichas, andraParesClaveValor, AndraComponentes.andraMayus("Equipamiento", "label-caps"), andraEquipo);
        HBox andraCajitas = new HBox(6);
        for (String andraTexto2 : AndraCatalogoServicio.ANDRA_FRANJAS) {
            boolean andraCorrecto = "Disponible".equals(andraEstado) && andraReservas.andraLibreEnInicio(andraEspacio, 0, andraTexto2);
            VBox andraCaja = new VBox(2, AndraComponentes.andraLiteral(andraTexto2, "strong"), AndraComponentes.andraEtiqueta(andraCorrecto ? "Libre" : "Ocupado", "small"));
            andraCaja.setAlignment(Pos.CENTER);
            andraCaja.setPadding(new Insets(8, 10, 8, 10));
            andraCaja.getStyleClass().add("room");
            andraCaja.getStyleClass().add(andraCorrecto ? "free" : "busy");
            andraCaja.setStyle("-fx-min-height: 0;");
            HBox.setHgrow(andraCaja, Priority.ALWAYS);
            andraCaja.setMaxWidth(Double.MAX_VALUE);
            andraCajitas.getChildren().add(andraCaja);
        }
        VBox andraDerecha = new VBox(10, AndraComponentes.andraMayus("Horarios de hoy", "label-caps"), andraCajitas,
                AndraComponentes.andraParrafo(andraEspacio.getAndraTipo() == AndraTipoEspacio.AUDITORIO ? "Los auditorios requieren aprobación de Secretaría General."
                        : "La reserva queda pendiente hasta la aprobación del responsable.", "hint"));
        VBox andraCuerpo = new VBox(18, andraDet, andraIzquierda, andraDerecha);
        Button andraFavorito = AndraComponentes.andraBoton(andraFav ? "Quitar de favoritos" : "Agregar a favoritos", "btn-secondary", "fth-star", andraEvento -> {
            boolean andraAhora = andraFavoritos.andraAlternar(AndraTipoFavorito.ESPACIO, andraEspacio.getAndraCodigo());
            andraNotificador.andraCerrarModal();
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        Button andraReservar = AndraComponentes.andraBoton("Reservar este espacio", "btn-primary", null, andraEvento -> andraReservar(andraEspacio, andraPrecarga, andraAlCambiar));
        andraReservar.setDisable(!"Disponible".equals(andraEstado));
        andraNotificador.andraModal(andraEspacio.getAndraNombre(), andraCuerpo, true, andraFavorito, andraReservar);
    }

    public void andraReservar(AndraEspacio andraEspacio, AndraPrecarga andraPre, Runnable andraAlCambiar) {
        AndraPrecarga andraPrecarga = andraPre == null ? AndraPrecarga.ANDRA_VACIA : andraPre;
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Fecha", andraPrecarga.andraFecha() != null ? andraPrecarga.andraFecha() : AndraFechas.andraHoy(), AndraFechas.andraHoy(), null))
                .andraRequerido("Elige una fecha.").andraRegla(andraSelectorFecha -> andraSelectorFecha.getValue() != null && andraSelectorFecha.getValue().isBefore(AndraFechas.andraHoy()) ? "Elige hoy o una fecha posterior." : null);
        AndraCampo<ComboBox<AndraOpcion>> andraHorario = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Horario", AndraOpcion.andraLista(AndraCatalogoServicio.ANDRA_HORARIOS), "Seleccionar",
                andraPrecarga.andraHorario())).andraRequerido("Elige un horario.");
        AndraCampo<TextField> andraAsistentes = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Número de asistentes", null,
                andraPrecarga.andraAsistentes() == null ? "" : String.valueOf(andraPrecarga.andraAsistentes()), 5)).andraRequerido("Indica cuántas personas asistirán.")
                .andraRegla(andraCampo -> {
                    String andraTexto = andraCampo.getText().trim();
                    if (andraTexto.isEmpty()) {
                        return null;
                    }
                    int andraCantidad = Integer.parseInt(andraTexto);
                    if (andraCantidad > andraEspacio.getAndraCapacidad()) {
                        return "El máximo permitido es " + AndraMontos.andraMiles(andraEspacio.getAndraCapacidad()) + ".";
                    }
                    return andraCantidad < 1 ? "El mínimo es 1." : null;
                });
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo de la reserva", "Ej. Práctica calificada del curso de Redes"))
                .andraRequerido("Describe el motivo de la reserva.").andraMinimo(10);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Capacidad máxima: " + AndraMontos.andraMiles(andraEspacio.getAndraCapacidad())
                + " personas. La solicitud quedará pendiente hasta su aprobación.", "muted", "small"),
                AndraFormulario.andraRejilla(2, andraFecha.andraNodo(), andraHorario.andraNodo()), andraAsistentes.andraNodo(), andraMotivo.andraNodo());
        Button andraEnviar = AndraComponentes.andraBoton("Enviar solicitud", "btn-primary", null, null);
        andraEnviar.setOnAction(andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            try {
                andraReservas.andraReservar(andraEspacio, andraFecha.andraControl().getValue(), AndraFormulario.andraValor(andraHorario.andraControl()),
                        Integer.parseInt(andraAsistentes.andraControl().getText().trim()), andraMotivo.andraControl().getText());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Solicitud de reserva enviada. Estado: Pendiente.");
            } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        });
        andraNotificador.andraModal("Reservar " + andraEspacio.getAndraNombre(), andraCuerpo, false,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraEnviar);
    }
}
