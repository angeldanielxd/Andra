package pe.andra.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonUsoCarne;
import pe.andra.service.DixonCarneServicio;
import pe.andra.service.DixonQrServicio;
import pe.andra.service.DixonSesionServicio;
import pe.andra.ui.AndraComponentes;
import pe.andra.ui.AndraMovimiento;
import pe.andra.ui.AndraRejilla;
import pe.andra.ui.AndraTarjetaCarne;
import pe.andra.util.DixonFechas;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraCarneControlador extends AndraVistaBase {

    private final DixonCarneServicio andraCarne;
    private final DixonSesionServicio andraSesion;

    private Timeline andraReloj;
    private long andraFranjaDibujada = -1;

    @Override
    public void andraSalir() {
        if (andraReloj != null) {
            andraReloj.stop();
        }
    }

    @Override
    protected List<Node> andraConstruir() {
        andraSalir();
        DixonPersona andraEstudianteActual = andraSesion.dixonEstudiante();
        boolean andraVigente = andraCarne.dixonVigente();
        List<Node> andraNodos = new ArrayList<>();
        andraNodos.add(AndraComponentes.andraPortada("Andra", "Mi carné digital",
                "Tu carné vive en tu celular. Muéstralo en la biblioteca, en el préstamo de equipos o al entrar a un laboratorio.", null));
        Label andraSinConexion = AndraComponentes.andraEtiqueta("Disponible sin conexión", "off-ok");
        andraSinConexion.setGraphic(new FontIcon("fth-cloud"));
        HBox andraInsignias = new HBox(8, andraSinConexion);
        andraInsignias.setAlignment(Pos.CENTER);
        if (!andraVigente) {
            andraInsignias.getChildren().add(AndraComponentes.andraBotonSm("Confirmar matrícula en Andra Pay", "btn-primary", null, andraEvento -> andraIr("pay")));
        }
        VBox andraIzquierda = new VBox(14, AndraTarjetaCarne.andraCrear(andraEstudianteActual, andraVigente, andraCarne.dixonFoto().orElse(null)), andraInsignias);
        ImageView andraImagenQr = new ImageView();
        andraImagenQr.setFitWidth(168);
        andraImagenQr.setFitHeight(168);
        StackPane andraMarco = new StackPane(andraImagenQr);
        andraMarco.getStyleClass().add("qr-img");
        andraMarco.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        AndraComponentes.andraAccesible(andraMarco, "Código QR dinámico del carné");
        Circle andraFondo = new Circle(19);
        andraFondo.getStyleClass().add("qr-ring-bg");
        Arc andraArco = new Arc(0, 0, 19, 19, 90, 360);
        andraArco.setType(ArcType.OPEN);
        andraArco.getStyleClass().add("qr-ring-fg");
        StackPane andraAnillo = new StackPane(andraFondo, andraArco);
        andraAnillo.setMaxSize(44, 44);
        StackPane andraConAnillo = new StackPane(andraMarco, andraAnillo);
        StackPane.setAlignment(andraAnillo, Pos.TOP_RIGHT);
        StackPane.setMargin(andraAnillo, new Insets(-12, -12, 0, 0));
        andraConAnillo.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Label andraRestante = AndraComponentes.andraEtiqueta("Se renueva en 15 s", "hint");
        andraFranjaDibujada = -1;
        Runnable andraActualizar = () -> {
            long andraAhora = DixonFechas.dixonMilis();
            long andraNumero = DixonCarneServicio.dixonFranja(andraAhora);
            if (andraNumero != andraFranjaDibujada) {
                andraImagenQr.setImage(DixonQrServicio.dixonImagen(DixonCarneServicio.dixonToken(andraEstudianteActual.getDixonCodigo(), andraNumero), 336));
                andraFranjaDibujada = andraNumero;
            }
            andraArco.setLength(360 * DixonCarneServicio.dixonFraccionRestante(andraAhora));
            AndraComponentes.andraFijar(andraRestante, "Se renueva en " + DixonCarneServicio.dixonSegundosRestantes(andraAhora) + " s");
        };
        andraActualizar.run();
        andraReloj = new Timeline(new KeyFrame(Duration.millis(500), andraEvento -> andraActualizar.run()));
        andraReloj.setCycleCount(Animation.INDEFINITE);
        andraReloj.play();
        HBox andraBotones = new HBox(8, AndraComponentes.andraBotonSm("Ampliar código", "btn-secondary", "fth-maximize-2", andraEvento -> andraAmpliar(andraEstudianteActual)),
                AndraComponentes.andraBotonSm("Simular lectura en puerta", "btn-primary", "fth-maximize", andraEvento -> andraLeer()));
        andraBotones.setAlignment(Pos.CENTER);
        VBox andraDerecha = new VBox(10, AndraComponentes.andraParrafo("Código de acceso", "h4"),
                AndraComponentes.andraParrafo("Este código cambia cada 15 segundos. Así nadie puede usar una captura de pantalla de tu carné.", "muted", "small"),
                andraConAnillo, andraRestante, andraBotones, AndraComponentes.andraParrafo("Sube el brillo de tu pantalla para que el lector lo capte mejor.", "hint"));
        andraDerecha.getStyleClass().add("qr-panel");
        GridPane andraDos = new GridPane();
        andraDos.setHgap(28);
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(52);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(48);
        andraDos.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        andraDos.add(andraIzquierda, 0, 0);
        andraDos.add(andraDerecha, 1, 0);
        GridPane.setValignment(andraIzquierda, javafx.geometry.VPos.TOP);
        VBox andraSeccion1 = AndraComponentes.andraSeccion(andraDos);
        andraSeccion1.getStyleClass().add("sin-arriba");
        andraNodos.add(andraSeccion1);
        AndraRejilla andraUsos = new AndraRejilla(4, 200, 16);
        String[][] andraDatos = {{"fth-book", "Biblioteca", "Ingreso y préstamo de libros"}, {"fth-box", "Equipos", "Retiro y devolución de préstamos"},
                {"fth-droplet", "Laboratorios", "Acceso a tus reservas"}, {"fth-home", "Auditorios", "Ingreso a eventos y ceremonias"}};
        for (String[] andraTextos : andraDatos) {
            StackPane andraCapa = new StackPane(new FontIcon(andraTextos[0]));
            andraCapa.getStyleClass().add("sicon-sm");
            VBox andraCajaVertical = new VBox(12, andraCapa, AndraComponentes.andraParrafo(andraTextos[1], "h3"), AndraComponentes.andraParrafo(andraTextos[2], "muted", "small"));
            andraCajaVertical.getStyleClass().add("office");
            andraUsos.getChildren().add(andraCajaVertical);
        }
        andraNodos.add(AndraComponentes.andraSeccion(andraUsos));
        List<Node> andraFilas = new ArrayList<>();
        for (DixonUsoCarne andraUsoCarne : andraCarne.dixonUltimos()) {
            andraFilas.add(AndraComponentes.andraFila(AndraComponentes.andraTextosFila(andraUsoCarne.getDixonLugar(), DixonFechas.dixonDmy(andraUsoCarne.getDixonFecha().toLocalDate()) + " · " + DixonFechas.dixonHm(andraUsoCarne.getDixonFecha())),
                    AndraComponentes.andraPildora(andraUsoCarne.isDixonConcedido() ? "Acceso concedido" : "Rechazado", andraUsoCarne.isDixonConcedido() ? "success" : "error")));
        }
        andraNodos.add(AndraComponentes.andraSeccion(AndraComponentes.andraCabeceraSeccion("Últimos usos", null),
                andraFilas.isEmpty() ? AndraComponentes.andraVacio("fth-credit-card", "Sin registros", "Aún no usaste tu carné.") : AndraComponentes.andraLista(andraFilas)));
        return andraNodos;
    }

    private void andraAmpliar(DixonPersona andraEstudianteActual) {
        ImageView andraImagenQr = new ImageView(DixonQrServicio.dixonImagen(andraCarne.dixonTokenActual(), 520));
        andraImagenQr.setFitWidth(260);
        andraImagenQr.setFitHeight(260);
        StackPane andraMarco = new StackPane(andraImagenQr);
        andraMarco.getStyleClass().add("qr-img");
        andraMarco.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        VBox andraCuerpo = new VBox(12, andraMarco, AndraComponentes.andraLiteral(andraEstudianteActual.getDixonNombre() + " · " + andraEstudianteActual.getDixonCodigo(), "hint"));
        andraCuerpo.setAlignment(Pos.CENTER);
        andraNotificador.andraModal("Código de acceso", andraCuerpo, false, AndraComponentes.andraBoton("Cerrar", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    private void andraLeer() {
        String andraLugar = DixonCarneServicio.DIXON_LUGARES.get(new java.util.Random().nextInt(DixonCarneServicio.DIXON_LUGARES.size()));
        Rectangle andraLinea = new Rectangle(160, 3);
        andraLinea.setArcWidth(3);
        andraLinea.setArcHeight(3);
        andraLinea.getStyleClass().add("scan-line");
        StackPane andraCaja = new StackPane(new FontIcon("fth-maximize"), andraLinea);
        andraCaja.getStyleClass().add("scan-box");
        if (!AndraMovimiento.andraReducido()) {
            TranslateTransition andraTraslacion = new TranslateTransition(Duration.millis(1200), andraLinea);
            andraTraslacion.setFromY(-70);
            andraTraslacion.setToY(70);
            andraTraslacion.setAutoReverse(true);
            andraTraslacion.setCycleCount(Animation.INDEFINITE);
            andraTraslacion.play();
        }
        VBox andraCuerpo = new VBox(14, andraCaja, AndraComponentes.andraParrafo("Leyendo tu código en " + andraLugar + "…", "muted"));
        andraCuerpo.setAlignment(Pos.CENTER);
        andraNotificador.andraModal("Lector de puerta", andraCuerpo, false);
        PauseTransition andraPausa = new PauseTransition(Duration.millis(1300));
        andraPausa.setOnFinished(andraEvento -> {
            if (!andraNotificador.andraHayModal()) {
                return;
            }
            DixonUsoCarne andraUso = andraCarne.dixonSimularLectura();
            boolean andraCorrecto = andraUso.isDixonConcedido();
            StackPane andraMarca = new StackPane(new FontIcon(andraCorrecto ? "fth-check" : "fth-x"));
            andraMarca.getStyleClass().add("ok-check");
            if (!andraCorrecto) {
                andraMarca.getStyleClass().add("no");
            }
            VBox andraCajaVertical = new VBox(6, andraMarca, AndraComponentes.andraParrafo(andraCorrecto ? "Acceso concedido" : "Acceso denegado", "h3"), AndraComponentes.andraLiteral(andraUso.getDixonLugar()),
                    AndraComponentes.andraParrafo(andraCorrecto ? "Carné vigente · " + andraSesion.dixonEstudiante().getDixonNombre() : "Tu carné está pendiente: confirma tu matrícula en Andra Pay.", "hint"));
            andraCajaVertical.getStyleClass().add("ok-hero");
            andraNotificador.andraModal("Lector de puerta", andraCajaVertical, false, AndraComponentes.andraBoton("Listo", "btn-primary", null, andraPosicionX -> {
                andraNotificador.andraCerrarModal();
                andraRedibujar();
            }));
        });
        andraPausa.play();
    }
}
