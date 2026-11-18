package pe.andra.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AndraCarrusel extends StackPane {

    public record AndraDatos(String andraEtiqueta, String andraTitulo, String andraTexto, String andraImagen, String andraDescripcion,
                        double andraFocoX, double andraFocoY) {
    }

    private static final Duration ANDRA_INTERVALO = Duration.seconds(6);
    private static final double ANDRA_MARGEN = 48;

    private int andraActual;
    private boolean andraReproduciendo;
    private final List<AndraDiapositiva> andraDiapositivas = new ArrayList<>();
    private final List<StackPane> andraPuntos = new ArrayList<>();
    private final Button andraPausa = new Button();
    private final Timeline andraAvance = new Timeline(new KeyFrame(ANDRA_INTERVALO, andraEvento -> andraIr(andraActual + 1)));

    public AndraCarrusel(List<AndraDatos> andraDatos) {
        getStyleClass().add("carousel");
        andraReproduciendo = !AndraMovimiento.andraReducido();
        for (int andraIndice = 0; andraIndice < andraDatos.size(); andraIndice++) {
            AndraDiapositiva andraDiapositiva = new AndraDiapositiva(andraDatos.get(andraIndice), andraIndice == 0);
            andraDiapositiva.setOpacity(andraIndice == 0 ? 1 : 0);
            andraDiapositiva.setVisible(andraIndice == 0);
            andraDiapositivas.add(andraDiapositiva);
            getChildren().add(andraDiapositiva);
        }
        Rectangle andraRecorte = new Rectangle();
        andraRecorte.setArcWidth(32);
        andraRecorte.setArcHeight(32);
        andraRecorte.widthProperty().bind(widthProperty());
        andraRecorte.heightProperty().bind(heightProperty());
        setClip(andraRecorte);

        HBox andraControles = new HBox(4);
        andraControles.getStyleClass().add("car-ctrl");
        andraControles.setAlignment(Pos.CENTER_LEFT);
        for (int andraIndice = 0; andraIndice < andraDatos.size(); andraIndice++) {
            final int andraIndice2 = andraIndice;
            Region andraBarra = new Region();
            andraBarra.getStyleClass().add("barra");
            StackPane andraPunto = new StackPane(andraBarra);
            andraPunto.getStyleClass().add("cdot-btn");
            andraPunto.setFocusTraversable(true);
            AndraComponentes.andraAccesible(andraPunto, "Ir a diapositiva " + (andraIndice + 1) + ": " + andraDatos.get(andraIndice).andraEtiqueta());
            andraPunto.setOnMouseClicked(andraEvento -> {
                andraIr(andraIndice2);
                andraReiniciar();
            });
            andraPuntos.add(andraPunto);
            andraControles.getChildren().add(andraPunto);
        }
        andraPausa.getStyleClass().add("pause-btn");
        andraPausa.setOnAction(andraEvento -> andraAlternar());
        andraControles.getChildren().add(andraPausa);
        andraControles.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(andraControles, Pos.BOTTOM_LEFT);
        StackPane.setMargin(andraControles, new Insets(0, 0, 24, ANDRA_MARGEN - 8));
        getChildren().add(andraControles);
        andraAvance.setCycleCount(Timeline.INDEFINITE);
        andraActualizarControles();
        if (andraReproduciendo) {
            andraAvance.play();
        }
        AndraMovimiento.andraReducidoPropiedad().addListener((andraObservable, andraBanderaA, andraBanderaB) -> {
            if (andraBanderaB) {
                andraReproduciendo = false;
                andraAvance.stop();
                andraActualizarControles();
            }
        });
    }

    public void andraDetener() {
        andraAvance.stop();
    }

    public void andraReanudar() {
        if (andraReproduciendo && !AndraMovimiento.andraReducido()) {
            andraAvance.playFromStart();
        }
    }

    private void andraAlternar() {
        andraReproduciendo = !andraReproduciendo;
        if (andraReproduciendo) {
            andraAvance.playFromStart();
        } else {
            andraAvance.stop();
        }
        andraActualizarControles();
    }

    private void andraReiniciar() {
        if (andraReproduciendo) {
            andraAvance.playFromStart();
        }
    }

    private void andraIr(int andraIndice) {
        int andraNuevo = Math.floorMod(andraIndice, andraDiapositivas.size());
        if (andraNuevo == andraActual) {
            return;
        }
        AndraDiapositiva andraSaliente = andraDiapositivas.get(andraActual);
        AndraDiapositiva andraEntrante = andraDiapositivas.get(andraNuevo);
        andraEntrante.setVisible(true);
        if (AndraMovimiento.andraReducido()) {
            andraSaliente.setOpacity(0);
            andraSaliente.setVisible(false);
            andraEntrante.setOpacity(1);
        } else {
            FadeTransition andraFuera = new FadeTransition(Duration.millis(500), andraSaliente);
            andraFuera.setToValue(0);
            andraFuera.setOnFinished(andraEvento -> andraSaliente.setVisible(false));
            FadeTransition andraDentro = new FadeTransition(Duration.millis(500), andraEntrante);
            andraDentro.setToValue(1);
            andraFuera.play();
            andraDentro.play();
        }
        andraActual = andraNuevo;
        andraActualizarControles();
    }

    private void andraActualizarControles() {
        for (int andraIndice = 0; andraIndice < andraPuntos.size(); andraIndice++) {
            andraPuntos.get(andraIndice).getStyleClass().remove("on");
            if (andraIndice == andraActual) {
                andraPuntos.get(andraIndice).getStyleClass().add("on");
            }
        }
        andraPausa.setGraphic(new FontIcon(andraReproduciendo ? "fth-pause" : "fth-play"));
        AndraComponentes.andraAccesible(andraPausa, (andraReproduciendo ? "Pausar" : "Reproducir") + " carrusel");
    }

    private static final class AndraDiapositiva extends Pane {

        private final VBox andraTexto;
        private final ImageView andraFoto;
        private final Image andraImagen;
        private final Rectangle andraAdorno = new Rectangle(88, 88);
        private final Circle andraPunto = new Circle(9);
        private final AndraDatos andraDatos;

        AndraDiapositiva(AndraDatos andraDatos, boolean andraPrincipal) {
            this.andraDatos = andraDatos;
            andraImagen = new Image(Objects.requireNonNull(AndraCarrusel.class.getResourceAsStream(andraDatos.andraImagen())));
            andraFoto = new ImageView(andraImagen);
            andraFoto.setSmooth(true);
            andraFoto.setPreserveRatio(false);
            AndraComponentes.andraAccesible(andraFoto, andraDatos.andraDescripcion());
            andraAdorno.setArcWidth(48);
            andraAdorno.setArcHeight(48);
            andraAdorno.getStyleClass().add("slide-deco");
            andraPunto.getStyleClass().add("slide-punto");
            Label andraEtiqueta = AndraComponentes.andraMayus(andraDatos.andraEtiqueta(), "slide-label");
            Label andraTitulo = AndraComponentes.andraParrafo(andraDatos.andraTitulo(), "slide-titulo");
            if (andraPrincipal) {
                andraTitulo.getStyleClass().add("principal");
            }
            Label andraParrafo = AndraComponentes.andraParrafo(andraDatos.andraTexto(), "slide-texto");
            andraParrafo.setMaxWidth(520);
            VBox.setMargin(andraTitulo, new Insets(8, 0, 0, 0));
            VBox.setMargin(andraParrafo, new Insets(10, 0, 0, 0));
            andraTexto = new VBox(andraEtiqueta, andraTitulo, andraParrafo);
            getChildren().addAll(andraAdorno, andraFoto, andraPunto, andraTexto);
        }

        @Override
        protected void layoutChildren() {
            double andraAncho = getWidth();
            double andraAlto = getHeight();
            double andraAnchoFoto = Math.min(andraAncho * 0.46, 500);
            double andraPosicionXFoto = andraAncho - 24 - andraAnchoFoto;
            double andraPosicionYFoto = 24;
            double andraAltoFoto = andraAlto - 48;
            andraFoto.setFitWidth(andraAnchoFoto);
            andraFoto.setFitHeight(andraAltoFoto);
            andraFoto.setViewport(andraRecorteCubrir(andraAnchoFoto, andraAltoFoto));
            andraFoto.relocate(andraPosicionXFoto, andraPosicionYFoto);
            Rectangle andraEsquinas = new Rectangle(andraAnchoFoto, andraAltoFoto);
            andraEsquinas.setArcWidth(24);
            andraEsquinas.setArcHeight(24);
            andraFoto.setClip(andraEsquinas);
            andraAdorno.relocate(andraPosicionXFoto - 16, andraPosicionYFoto + andraAltoFoto + 12 - 88);
            andraPunto.relocate(andraPosicionXFoto + andraAnchoFoto + 6 - 18, andraPosicionYFoto - 6);
            double andraAnchoTexto = Math.max(200, andraAncho * 0.54 - ANDRA_MARGEN);
            double andraAltoTexto = andraTexto.prefHeight(andraAnchoTexto);
            andraTexto.resizeRelocate(ANDRA_MARGEN, Math.max(ANDRA_MARGEN, (andraAlto - andraAltoTexto) / 2 - 20), andraAnchoTexto, andraAltoTexto);
        }

        private Rectangle2D andraRecorteCubrir(double andraAnchoFoto, double andraAltoFoto) {
            double andraAnchoImagen = andraImagen.getWidth();
            double andraAltoImagen = andraImagen.getHeight();
            double andraEscala = Math.max(andraAnchoFoto / andraAnchoImagen, andraAltoFoto / andraAltoImagen);
            double andraAnchoVista = andraAnchoFoto / andraEscala;
            double andraAltoVista = andraAltoFoto / andraEscala;
            return new Rectangle2D((andraAnchoImagen - andraAnchoVista) * andraDatos.andraFocoX(), (andraAltoImagen - andraAltoVista) * andraDatos.andraFocoY(), andraAnchoVista, andraAltoVista);
        }
    }
}
