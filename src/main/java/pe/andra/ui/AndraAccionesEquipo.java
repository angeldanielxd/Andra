package pe.andra.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.DixonEquipo;
import pe.andra.model.DixonPrestamo;
import pe.andra.model.DixonTipoFavorito;
import pe.andra.service.DixonEquipoServicio;
import pe.andra.service.DixonFavoritoServicio;
import pe.andra.util.DixonFechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraAccionesEquipo {

    private final AndraNotificador andraNotificador;
    private final DixonEquipoServicio andraEquipos;
    private final DixonFavoritoServicio andraFavoritos;

    public Label andraEstado(DixonEquipo andraEquipo, boolean andraLargo) {
        if (andraEquipo.dixonEsPorTurno()) {
            return AndraComponentes.andraPildora("Próximo turno: " + andraEquipo.getDixonProximoTurno(), "warning");
        }
        if (andraEquipo.getDixonSaldo() > 0) {
            return AndraComponentes.andraPildora(andraLargo ? "Disponible (" + andraEquipo.getDixonSaldo() + " " + andraEquipo.getDixonUnidad() + ")" : andraEquipo.getDixonSaldo() + " " + andraEquipo.getDixonUnidad(), "success");
        }
        return AndraComponentes.andraPildora("Agotado", "neutral");
    }

    public AndraTarjetaGaleria andraTarjeta(DixonEquipo andraEquipo, int andraIndice, Runnable andraAlCambiar, LocalDate andraFechaUso, String andraDuracion) {
        Node[] andraGiro = new Node[1];
        StackPane andraArte = AndraIlustraciones.andraEquipo(andraEquipo, andraGiro);
        boolean andraFav = andraFavoritos.dixonEs(DixonTipoFavorito.EQUIPO, andraEquipo.getDixonCodigo());
        Button andraEstrella = AndraComponentes.andraEstrella(andraFav, andraEquipo.getDixonNombre(), () -> {
            boolean andraAhora = andraFavoritos.dixonAlternar(DixonTipoFavorito.EQUIPO, andraEquipo.getDixonCodigo());
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        return new AndraTarjetaGaleria(andraArte, andraGiro[0], andraEquipo.getDixonRotulo(), andraEstado(andraEquipo, false), andraEquipo.getDixonNombre(),
                AndraComponentes.andraMetaLinea("fth-map-pin", "Sede " + andraEquipo.getDixonSede().getDixonNombre() + " · " + andraEquipo.getDixonUbicacion()),
                AndraComponentes.andraEtiqueta(andraEquipo.getDixonEscuela() != null ? "EP " + andraEquipo.getDixonEscuela().getDixonNombre() : "Uso general", "hint"),
                false, null, andraEstrella, "Ver detalles de " + andraEquipo.getDixonNombre(), () -> andraDetalle(andraEquipo, andraAlCambiar, andraFechaUso, andraDuracion), andraIndice);
    }

    public void andraDetalle(DixonEquipo andraEquipo, Runnable andraAlCambiar, LocalDate andraFechaUso, String andraDuracion) {
        boolean andraFav = andraFavoritos.dixonEs(DixonTipoFavorito.EQUIPO, andraEquipo.getDixonCodigo());
        Node[] andraGiro = new Node[1];
        StackPane andraArte = AndraIlustraciones.andraEquipo(andraEquipo, andraGiro);
        andraArte.getStyleClass().add("det-art");
        andraArte.setPrefHeight(240);
        andraArte.setMinHeight(200);
        VBox andraIncluye = new VBox(6);
        for (String andraPosicionX : andraEquipo.getDixonEspecificaciones().split("•")) {
            Label andraEtiqueta = AndraComponentes.andraLiteralParrafo(andraPosicionX.trim(), "small");
            andraEtiqueta.setGraphic(AndraComponentes.andraIcono("fth-check", "azul"));
            andraIncluye.getChildren().add(andraEtiqueta);
        }
        VBox andraIzquierda = new VBox(12, AndraComponentes.andraFichas(andraEstado(andraEquipo, true), AndraComponentes.andraDistintivo(andraEquipo.getDixonRotulo())),
                AndraComponentes.andraMayus("Incluye", "label-caps"), andraIncluye);
        GridPane andraParesClaveValor = AndraComponentes.andraKv("Sede", andraEquipo.getDixonSede().getDixonNombreCompleto(), "Ubicación", AndraComponentes.andraLiteralParrafo(andraEquipo.getDixonUbicacion(), "dd"),
                "Punto de entrega", AndraComponentes.andraLiteralParrafo(andraEquipo.getDixonPuntoEntrega(), "dd"),
                "Uso", andraEquipo.getDixonEscuela() != null ? "Preferente para EP " + andraEquipo.getDixonEscuela().getDixonNombre() : "General",
                "Préstamo", "Hasta 4 horas lectivas, renovable una vez");
        VBox andraCuerpo = new VBox(18, andraArte, andraIzquierda, andraParesClaveValor);
        Button andraFavorito = AndraComponentes.andraBoton(andraFav ? "Quitar de favoritos" : "Agregar a favoritos", "btn-secondary", "fth-star", andraEvento -> {
            boolean andraAhora = andraFavoritos.dixonAlternar(DixonTipoFavorito.EQUIPO, andraEquipo.getDixonCodigo());
            andraNotificador.andraCerrarModal();
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        Button andraPedir = AndraComponentes.andraBoton(andraEquipo.dixonEsPorTurno() ? "Reservar turno" : "Solicitar préstamo", "btn-primary", null,
                andraEvento -> andraSolicitar(andraEquipo, andraAlCambiar, andraFechaUso, andraDuracion));
        andraPedir.setDisable(!andraEquipo.dixonEsPorTurno() && andraEquipo.getDixonSaldo() <= 0);
        andraNotificador.andraModal(andraEquipo.getDixonNombre(), andraCuerpo, true, andraFavorito, andraPedir);
    }

    public void andraSolicitar(DixonEquipo andraEquipo, Runnable andraAlCambiar, LocalDate andraFechaUso, String andraDuracion) {
        boolean andraTurno = andraEquipo.dixonEsPorTurno();
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Fecha de uso", andraFechaUso != null ? andraFechaUso : DixonFechas.dixonHoy(), DixonFechas.dixonHoy(), null))
                .andraRequerido("Elige la fecha.");
        AndraCampo<ComboBox<AndraOpcion>> andraHora = andraFormulario.andraAgregar(andraTurno
                ? AndraFormulario.andraSelector("Turno", AndraOpcion.andraLista(List.of("16:00 - 18:00", "18:00 - 20:00")), "Seleccionar", null).andraRequerido("Elige un turno.")
                : AndraFormulario.andraSelector("Hora de retiro", AndraOpcion.andraLista(List.of("08:00", "10:00", "12:00", "14:00", "16:00")), "Seleccionar", null)
                .andraRequerido("Elige la hora de retiro."));
        List<AndraOpcion> andraDuraciones = new ArrayList<>();
        DixonEquipoServicio.DIXON_DURACIONES.forEach((andraTexto, andraValor) -> andraDuraciones.add(AndraOpcion.andraDe(andraTexto, andraValor)));
        AndraCampo<ComboBox<AndraOpcion>> andraDur = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Duración", andraDuraciones, null, andraDuracion == null ? "4h" : andraDuracion));
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo de uso", "Ej. Ensayo del elenco universitario"))
                .andraRequerido("Describe para qué usarás el equipo.").andraMinimo(10);
        AndraCampo<CheckBox> andraAcepto = andraFormulario.andraAgregar(AndraFormulario.andraCasilla("Acepto el reglamento de préstamos y la penalización por devolución tardía o deterioro."))
                .andraRegla(andraCasilla -> andraCasilla.isSelected() ? null : "Debes aceptar el reglamento para continuar.");
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo(andraTurno ? "Este equipo está en uso. Puedes reservar el siguiente turno libre. Sede "
                + andraEquipo.getDixonSede().getDixonNombre() + "." : "Retiro en: Sede " + andraEquipo.getDixonSede().getDixonNombre() + " · " + andraEquipo.getDixonUbicacion() + ".", "muted", "small"),
                AndraFormulario.andraRejilla(2, andraFecha.andraNodo(), andraHora.andraNodo()), andraDur.andraNodo(), andraMotivo.andraNodo(), andraAcepto.andraNodo());
        Button andraEnviar = AndraComponentes.andraBoton(andraTurno ? "Reservar turno" : "Enviar solicitud", "btn-primary", null, null);
        andraEnviar.setOnAction(andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            try {
                DixonPrestamo andraPrestamo = andraEquipos.dixonSolicitar(andraEquipo, andraFecha.andraControl().getValue(), AndraFormulario.andraValor(andraHora.andraControl()),
                        AndraFormulario.andraValor(andraDur.andraControl()), andraMotivo.andraControl().getText(), andraAcepto.andraControl().isSelected());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Solicitud " + andraPrestamo.getDixonCodigo() + " registrada. Estado: En revisión.");
            } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        });
        andraNotificador.andraModal(andraTurno ? "Reservar turno: " + andraEquipo.getDixonNombre() : "Solicitar préstamo: " + andraEquipo.getDixonNombre(), andraCuerpo, false,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraEnviar);
    }
}
