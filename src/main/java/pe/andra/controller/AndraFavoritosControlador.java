package pe.andra.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.ui.*;
import pe.andra.util.AndraMontos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AndraFavoritosControlador extends AndraVistaBase {

    private final AndraFavoritoServicio andraFavoritos;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraDocumentoServicio andraDocumentos;
    private final AndraAccionesDocumento andraAccionesDocumento;
    private final AndraAccionesEquipo andraAccionesEquipo;
    private final AndraAccionesEspacio andraAccionesEspacio;
    private final AndraReservaServicio andraReservas;

    private String andraPestana = "docs";

    @Override
    protected List<Node> andraConstruir() {
        Set<String> andraFavDocs = andraFavoritos.andraCodigos(AndraTipoFavorito.DOCUMENTO);
        Set<String> andraFavMios = andraFavoritos.andraCodigos(AndraTipoFavorito.MI_DOCUMENTO);
        Set<String> andraFavEq = andraFavoritos.andraCodigos(AndraTipoFavorito.EQUIPO);
        Set<String> andraFavEsp = andraFavoritos.andraCodigos(AndraTipoFavorito.ESPACIO);
        List<AndraDocumento> andraDocs = andraCatalogo.andraDocumentos().stream().filter(andraDocumento -> andraFavDocs.contains(andraDocumento.getAndraCodigo())).toList();
        List<AndraDocumentoPersonal> andraMios = andraDocumentos.andraMisDocumentos().stream().filter(andraDocumentoPersonal -> andraFavMios.contains(andraDocumentoPersonal.getAndraCodigo())).toList();
        List<AndraEquipo> andraEqs = andraCatalogo.andraEquipos().stream().filter(andraEquipo -> andraFavEq.contains(andraEquipo.getAndraCodigo())).toList();
        List<AndraEspacio> andraEsps = andraCatalogo.andraEspacios().stream().filter(andraEspacio -> andraFavEsp.contains(andraEspacio.getAndraCodigo())).toList();
        AndraSegmentado andraPestanas = new AndraSegmentado(true).andraOpcion("docs", "Documentación", null, (long) (andraDocs.size() + andraMios.size()))
                .andraOpcion("eq", "Equipos", null, (long) andraEqs.size()).andraOpcion("areas", "Áreas", null, (long) andraEsps.size()).andraSeleccionar(andraPestana);
        andraPestanas.andraAlCambiar(andraValor -> {
            andraPestana = andraValor;
            andraRedibujar();
        });
        andraPestanas.setMaxWidth(560);
        AndraComponentes.andraAccesible(andraPestanas, "Tipo de favorito");
        HBox andraCentro = new HBox(andraPestanas);
        andraCentro.setAlignment(Pos.CENTER);
        VBox.setMargin(andraCentro, new Insets(0, 0, 32, 0));
        HBox.setHgrow(andraPestanas, Priority.ALWAYS);
        Node andraCuerpo;
        if ("docs".equals(andraPestana)) {
            if (andraDocs.isEmpty() && andraMios.isEmpty()) {
                andraCuerpo = andraVacio("Aún no tienes documentación favorita", "Marca la estrella en cualquier documento para guardarlo aquí.", "documentos", "Ir a Documentos", "fth-file-text");
            } else {
                VBox andraCajaVertical = new VBox(32);
                if (!andraDocs.isEmpty()) {
                    AndraRejilla andraRejilla = new AndraRejilla(2, 360);
                    andraDocs.forEach(andraDocumento -> andraRejilla.getChildren().add(andraAccionesDocumento.andraTarjeta(andraDocumento, this::andraRedibujar)));
                    andraCajaVertical.getChildren().add(new VBox(12, AndraComponentes.andraMayus("Documentos y formatos", "label-caps"), andraRejilla));
                }
                if (!andraMios.isEmpty()) {
                    List<Node> andraFilas = new ArrayList<>();
                    andraMios.forEach(andraDocumentoPersonal -> andraFilas.add(andraAccionesDocumento.andraFilaPersonal(andraDocumentoPersonal, this::andraRedibujar)));
                    andraCajaVertical.getChildren().add(new VBox(12, AndraComponentes.andraMayus("Mis documentos", "label-caps"), AndraComponentes.andraLista(andraFilas)));
                }
                andraCuerpo = andraCajaVertical;
            }
        } else if ("eq".equals(andraPestana)) {
            if (andraEqs.isEmpty()) {
                andraCuerpo = andraVacio("Aún no tienes equipos favoritos", "Marca la estrella en un equipo del catálogo para tenerlo a mano.", "equipos", "Ir a Equipos", "fth-box");
            } else {
                AndraRejilla andraRejilla = new AndraRejilla(3, 260);
                andraEqs.forEach(andraEquipo -> andraRejilla.getChildren().add(andraTarjetaEquipo(andraEquipo)));
                andraCuerpo = andraRejilla;
            }
        } else {
            if (andraEsps.isEmpty()) {
                andraCuerpo = andraVacio("Aún no tienes áreas favoritas", "Marca la estrella en un aula, laboratorio o auditorio para reservarlo más rápido.", "reservas", "Ir a Reservas", "fth-calendar");
            } else {
                AndraRejilla andraRejilla = new AndraRejilla(4, 220);
                andraEsps.forEach(andraEspacio -> andraRejilla.getChildren().add(andraTarjetaEspacio(andraEspacio)));
                andraCuerpo = andraRejilla;
            }
        }
        VBox andraSeccion = AndraComponentes.andraSeccion(andraCentro, andraCuerpo);
        andraSeccion.getStyleClass().add("sin-arriba");
        return List.of(AndraComponentes.andraPortada(null, "Favoritos", "Tu documentación, equipos y áreas guardados en un solo lugar.", null), andraSeccion);
    }

    private VBox andraVacio(String andraTitulo, String andraSub, String andraRuta, String andraBoton, String andraIcono) {
        return AndraComponentes.andraVacio("fth-star", andraTitulo, andraSub, AndraComponentes.andraBotonSm(andraBoton, "btn-secondary", andraIcono, andraEvento -> andraIr(andraRuta)));
    }

    private StackPane andraMiniatura(String andraIcono) {
        StackPane andraCapa = new StackPane(new FontIcon(andraIcono));
        andraCapa.getStyleClass().add("thumb");
        andraCapa.setMinHeight(140);
        return andraCapa;
    }

    private VBox andraTarjetaEquipo(AndraEquipo andraEquipo) {
        boolean andraCorrecto = andraEquipo.andraEsDisponibleAhora();
        Button andraEstrella = AndraComponentes.andraEstrella(true, andraEquipo.getAndraNombre(), () -> {
            andraFavoritos.andraAlternar(AndraTipoFavorito.EQUIPO, andraEquipo.getAndraCodigo());
            andraNotificador.andraExito("Quitado de favoritos.");
            andraRedibujar();
        });
        StackPane andraArriba = new StackPane(andraMiniatura(AndraIlustraciones.andraIconoCategoria(andraEquipo)), andraEstrella);
        StackPane.setAlignment(andraEstrella, Pos.TOP_RIGHT);
        StackPane.setMargin(andraEstrella, new Insets(10));
        HBox andraCab = new HBox(AndraComponentes.andraMayus(andraEquipo.getAndraRotulo(), "label-caps"), AndraComponentes.andraEspacio(), andraAccionesEquipo.andraEstado(andraEquipo, true));
        andraCab.setAlignment(Pos.CENTER_LEFT);
        VBox andraCajaVertical = new VBox(8, andraArriba, andraCab, AndraComponentes.andraParrafo(andraEquipo.getAndraNombre(), "h3", "sm"), AndraComponentes.andraParrafo(andraEquipo.getAndraEspecificaciones(), "eq-specs"),
                AndraComponentes.andraMetaLinea("fth-map-pin", "Sede " + andraEquipo.getAndraSede().getAndraNombre() + " · " + andraEquipo.getAndraUbicacion()));
        if (andraEquipo.getAndraEscuela() != null) {
            andraCajaVertical.getChildren().add(AndraComponentes.andraMetaLinea("fth-book-open", "Uso preferente: EP " + andraEquipo.getAndraEscuela().getAndraNombre()));
        }
        Button andraPedir = AndraComponentes.andraBloque(AndraComponentes.andraBotonSm(andraEquipo.andraEsPorTurno() ? "Reservar turno" : andraCorrecto ? "Solicitar préstamo" : "Sin unidades",
                andraCorrecto ? "btn-primary" : "btn-secondary", null, andraEvento -> andraAccionesEquipo.andraSolicitar(andraEquipo, this::andraRedibujar, null, null)));
        andraPedir.setDisable(!andraCorrecto && !andraEquipo.andraEsPorTurno());
        andraCajaVertical.getChildren().addAll(AndraComponentes.andraEspacioV(), andraPedir);
        andraCajaVertical.getStyleClass().add("eq-card");
        return andraCajaVertical;
    }

    private VBox andraTarjetaEspacio(AndraEspacio andraEspacio) {
        String andraEstado = andraReservas.andraEstadoGaleria(andraEspacio, null, null);
        Button andraEstrella = AndraComponentes.andraEstrella(true, andraEspacio.getAndraNombre(), () -> {
            andraFavoritos.andraAlternar(AndraTipoFavorito.ESPACIO, andraEspacio.getAndraCodigo());
            andraNotificador.andraExito("Quitado de favoritos.");
            andraRedibujar();
        });
        String andraIcono = switch (andraEspacio.getAndraTipo()) {
            case LABORATORIO -> "fth-droplet";
            case TALLER -> "fth-tool";
            case SALA -> "fth-list";
            default -> "fth-image";
        };
        StackPane andraArriba = new StackPane(andraMiniatura(andraIcono), andraEstrella);
        StackPane.setAlignment(andraEstrella, Pos.TOP_RIGHT);
        StackPane.setMargin(andraEstrella, new Insets(10));
        HBox andraMeta = new HBox(AndraComponentes.andraEtiqueta("Capacidad: " + AndraMontos.andraMiles(andraEspacio.getAndraCapacidad()) + " personas", "muted", "small"), AndraComponentes.andraEspacio(),
                AndraComponentes.andraPildora(andraEstado));
        andraMeta.setAlignment(Pos.CENTER_LEFT);
        boolean andraCorrecto = "Disponible".equals(andraEstado);
        Button andraReservar = AndraComponentes.andraBloque(AndraComponentes.andraBotonSm(andraCorrecto ? "Reservar" : "No disponible", andraCorrecto ? "btn-primary" : "btn-secondary", null,
                andraEvento -> andraAccionesEspacio.andraReservar(andraEspacio, AndraAccionesEspacio.AndraPrecarga.ANDRA_VACIA, this::andraRedibujar)));
        andraReservar.setDisable(!andraCorrecto);
        VBox andraCajaVertical = new VBox(12, andraArriba, AndraComponentes.andraLiteralParrafo(andraEspacio.getAndraNombre(), "h3", "sm"),
                AndraComponentes.andraMetaLinea("fth-map-pin", "Sede " + andraEspacio.getAndraSede().getAndraNombre() + " · " + andraEspacio.getAndraEdificio()),
                AndraComponentes.andraMetaLinea("fth-book-open", andraEspacio.getAndraEscuela() != null ? "EP " + andraEspacio.getAndraEscuela().getAndraNombre() : "Uso general"), andraMeta,
                AndraComponentes.andraEspacioV(), andraReservar);
        andraCajaVertical.getStyleClass().add("space-card");
        return andraCajaVertical;
    }
}
