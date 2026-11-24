package pe.andra.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.AndraEquipo;
import pe.andra.model.AndraPrestamo;
import pe.andra.model.AndraTipoFavorito;
import pe.andra.service.AndraEquipoServicio;
import pe.andra.service.AndraFavoritoServicio;
import pe.andra.util.AndraFechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraAccionesEquipo {

    private final AndraNotificador andraNotificador;
    private final AndraEquipoServicio andraEquipos;
    private final AndraFavoritoServicio andraFavoritos;

    public Label andraEstado(AndraEquipo andraEquipo, boolean andraLargo) {
        if (andraEquipo.andraEsPorTurno()) {
            return AndraComponentes.andraPildora("Próximo turno: " + andraEquipo.getAndraProximoTurno(), "warning");
        }
        if (andraEquipo.getAndraSaldo() > 0) {
            return AndraComponentes.andraPildora(andraLargo ? "Disponible (" + andraEquipo.getAndraSaldo() + " " + andraEquipo.getAndraUnidad() + ")" : andraEquipo.getAndraSaldo() + " " + andraEquipo.getAndraUnidad(), "success");
        }
        return AndraComponentes.andraPildora("Agotado", "neutral");
    }

    public AndraTarjetaGaleria andraTarjeta(AndraEquipo andraEquipo, int andraIndice, Runnable andraAlCambiar, LocalDate andraFechaUso, String andraDuracion) {
        Node[] andraGiro = new Node[1];
        StackPane andraArte = AndraIlustraciones.andraEquipo(andraEquipo, andraGiro);
        boolean andraFav = andraFavoritos.andraEs(AndraTipoFavorito.EQUIPO, andraEquipo.getAndraCodigo());
        Button andraEstrella = AndraComponentes.andraEstrella(andraFav, andraEquipo.getAndraNombre(), () -> {
            boolean andraAhora = andraFavoritos.andraAlternar(AndraTipoFavorito.EQUIPO, andraEquipo.getAndraCodigo());
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        return new AndraTarjetaGaleria(andraArte, andraGiro[0], andraEquipo.getAndraRotulo(), andraEstado(andraEquipo, false), andraEquipo.getAndraNombre(),
                AndraComponentes.andraMetaLinea("fth-map-pin", "Sede " + andraEquipo.getAndraSede().getAndraNombre() + " · " + andraEquipo.getAndraUbicacion()),
                AndraComponentes.andraEtiqueta(andraEquipo.getAndraEscuela() != null ? "EP " + andraEquipo.getAndraEscuela().getAndraNombre() : "Uso general", "hint"),
                false, null, andraEstrella, "Ver detalles de " + andraEquipo.getAndraNombre(), () -> andraDetalle(andraEquipo, andraAlCambiar, andraFechaUso, andraDuracion), andraIndice);
    }

    public void andraDetalle(AndraEquipo andraEquipo, Runnable andraAlCambiar, LocalDate andraFechaUso, String andraDuracion) {
        boolean andraFav = andraFavoritos.andraEs(AndraTipoFavorito.EQUIPO, andraEquipo.getAndraCodigo());
        Node[] andraGiro = new Node[1];
        StackPane andraArte = AndraIlustraciones.andraEquipo(andraEquipo, andraGiro);
        andraArte.getStyleClass().add("det-art");
        andraArte.setPrefHeight(240);
        andraArte.setMinHeight(200);
        VBox andraIncluye = new VBox(6);
        for (String andraPosicionX : andraEquipo.getAndraEspecificaciones().split("•")) {
            Label andraEtiqueta = AndraComponentes.andraLiteralParrafo(andraPosicionX.trim(), "small");
            andraEtiqueta.setGraphic(AndraComponentes.andraIcono("fth-check", "azul"));
            andraIncluye.getChildren().add(andraEtiqueta);
        }
        VBox andraIzquierda = new VBox(12, AndraComponentes.andraFichas(andraEstado(andraEquipo, true), AndraComponentes.andraDistintivo(andraEquipo.getAndraRotulo())),
                AndraComponentes.andraMayus("Incluye", "label-caps"), andraIncluye);
        GridPane andraParesClaveValor = AndraComponentes.andraKv("Sede", andraEquipo.getAndraSede().getAndraNombreCompleto(), "Ubicación", AndraComponentes.andraLiteralParrafo(andraEquipo.getAndraUbicacion(), "dd"),
                "Punto de entrega", AndraComponentes.andraLiteralParrafo(andraEquipo.getAndraPuntoEntrega(), "dd"),
                "Uso", andraEquipo.getAndraEscuela() != null ? "Preferente para EP " + andraEquipo.getAndraEscuela().getAndraNombre() : "General",
                "Préstamo", "Hasta 4 horas lectivas, renovable una vez");
        VBox andraCuerpo = new VBox(18, andraArte, andraIzquierda, andraParesClaveValor);
        Button andraFavorito = AndraComponentes.andraBoton(andraFav ? "Quitar de favoritos" : "Agregar a favoritos", "btn-secondary", "fth-star", andraEvento -> {
            boolean andraAhora = andraFavoritos.andraAlternar(AndraTipoFavorito.EQUIPO, andraEquipo.getAndraCodigo());
            andraNotificador.andraCerrarModal();
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        Button andraPedir = AndraComponentes.andraBoton(andraEquipo.andraEsPorTurno() ? "Reservar turno" : "Solicitar préstamo", "btn-primary", null,
                andraEvento -> andraSolicitar(andraEquipo, andraAlCambiar, andraFechaUso, andraDuracion));
        andraPedir.setDisable(!andraEquipo.andraEsPorTurno() && andraEquipo.getAndraSaldo() <= 0);
        andraNotificador.andraModal(andraEquipo.getAndraNombre(), andraCuerpo, true, andraFavorito, andraPedir);
    }

    public void andraSolicitar(AndraEquipo andraEquipo, Runnable andraAlCambiar, LocalDate andraFechaUso, String andraDuracion) {
        boolean andraTurno = andraEquipo.andraEsPorTurno();
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Fecha de uso", andraFechaUso != null ? andraFechaUso : AndraFechas.andraHoy(), AndraFechas.andraHoy(), null))
                .andraRequerido("Elige la fecha.");
        AndraCampo<ComboBox<AndraOpcion>> andraHora = andraFormulario.andraAgregar(andraTurno
                ? AndraFormulario.andraSelector("Turno", AndraOpcion.andraLista(List.of("16:00 - 18:00", "18:00 - 20:00")), "Seleccionar", null).andraRequerido("Elige un turno.")
                : AndraFormulario.andraSelector("Hora de retiro", AndraOpcion.andraLista(List.of("08:00", "10:00", "12:00", "14:00", "16:00")), "Seleccionar", null)
                .andraRequerido("Elige la hora de retiro."));
        List<AndraOpcion> andraDuraciones = new ArrayList<>();
        AndraEquipoServicio.ANDRA_DURACIONES.forEach((andraTexto, andraValor) -> andraDuraciones.add(AndraOpcion.andraDe(andraTexto, andraValor)));
        AndraCampo<ComboBox<AndraOpcion>> andraDur = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Duración", andraDuraciones, null, andraDuracion == null ? "4h" : andraDuracion));
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo de uso", "Ej. Ensayo del elenco universitario"))
                .andraRequerido("Describe para qué usarás el equipo.").andraMinimo(10);
        AndraCampo<CheckBox> andraAcepto = andraFormulario.andraAgregar(AndraFormulario.andraCasilla("Acepto el reglamento de préstamos y la penalización por devolución tardía o deterioro."))
                .andraRegla(andraCasilla -> andraCasilla.isSelected() ? null : "Debes aceptar el reglamento para continuar.");
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo(andraTurno ? "Este equipo está en uso. Puedes reservar el siguiente turno libre. Sede "
                + andraEquipo.getAndraSede().getAndraNombre() + "." : "Retiro en: Sede " + andraEquipo.getAndraSede().getAndraNombre() + " · " + andraEquipo.getAndraUbicacion() + ".", "muted", "small"),
                AndraFormulario.andraRejilla(2, andraFecha.andraNodo(), andraHora.andraNodo()), andraDur.andraNodo(), andraMotivo.andraNodo(), andraAcepto.andraNodo());
        Button andraEnviar = AndraComponentes.andraBoton(andraTurno ? "Reservar turno" : "Enviar solicitud", "btn-primary", null, null);
        andraEnviar.setOnAction(andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            try {
                AndraPrestamo andraPrestamo = andraEquipos.andraSolicitar(andraEquipo, andraFecha.andraControl().getValue(), AndraFormulario.andraValor(andraHora.andraControl()),
                        AndraFormulario.andraValor(andraDur.andraControl()), andraMotivo.andraControl().getText(), andraAcepto.andraControl().isSelected());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Solicitud " + andraPrestamo.getAndraCodigo() + " registrada. Estado: En revisión.");
            } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        });
        andraNotificador.andraModal(andraTurno ? "Reservar turno: " + andraEquipo.getAndraNombre() : "Solicitar préstamo: " + andraEquipo.getAndraNombre(), andraCuerpo, false,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraEnviar);
    }
}
