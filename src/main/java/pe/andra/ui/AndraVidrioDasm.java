package pe.andra.ui;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Supplier;

public class AndraVidrioDasm extends StackPane {

    private static final double ANDRA_MARGEN = 30;
    private static final long ANDRA_INTERVALO_NS = 33_000_000L;
    private static final double ANDRA_LIMITE_MS = 24;

    private final ImageView andraCapa = new ImageView();
    private final Region andraTinte = new Region();
    private Supplier<Node> andraFuente = () -> null;
    private boolean andraSucio = true;
    private boolean andraDesenfoque = true;
    private long andraUltimo;
    private double andraCostoPromedio;
    private WritableImage andraLienzo;
    private final Rectangle andraRecorte = new Rectangle();

    public AndraVidrioDasm(String andraClaseTinte) {
        this(andraClaseTinte, true);
    }

    public AndraVidrioDasm(String andraClaseTinte, boolean andraSaturar) {
        andraCapa.setManaged(false);
        andraCapa.setMouseTransparent(true);
        andraCapa.setSmooth(true);
        ColorAdjust andraSaturacion = new ColorAdjust();
        andraSaturacion.setSaturation(0.8);
        GaussianBlur andraDesenfoqueGauss = new GaussianBlur(28);
        if (andraSaturar) {
            andraDesenfoqueGauss.setInput(andraSaturacion);
        }
        andraCapa.setEffect(andraDesenfoqueGauss);
        andraTinte.getStyleClass().add(andraClaseTinte);
        andraTinte.setMouseTransparent(true);
        getChildren().addAll(andraCapa, andraTinte);
        andraRecorte.widthProperty().bind(widthProperty());
        andraRecorte.heightProperty().bind(heightProperty());
        setClip(andraRecorte);
        widthProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> andraMarcar());
        heightProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> andraMarcar());
        AnimationTimer andraReloj = new AnimationTimer() {
            @Override
            public void handle(long andraAhora) {
                if (andraSucio && andraAhora - andraUltimo > ANDRA_INTERVALO_NS) {
                    andraUltimo = andraAhora;
                    andraSucio = false;
                    andraRefrescar();
                }
            }
        };
        sceneProperty().addListener((andraObservable, andraEscenaA, andraEscena) -> {
            if (andraEscena == null) {
                andraReloj.stop();
            } else {
                andraMarcar();
                andraReloj.start();
            }
        });
        andraReloj.start();
    }

    public void andraAsignarRadio(double andraRadio) {
        andraRecorte.setArcWidth(andraRadio * 2);
        andraRecorte.setArcHeight(andraRadio * 2);
    }

    public void andraAsignarFuente(Supplier<Node> andraFuente) {
        this.andraFuente = andraFuente;
        andraMarcar();
    }

    public void andraMarcar() {
        andraSucio = true;
    }

    public boolean andraEsDesenfoqueActivo() {
        return andraDesenfoque;
    }

    private void andraRefrescar() {
        Node andraNodo = andraFuente.get();
        if (!andraDesenfoque || andraNodo == null || andraNodo.getScene() == null || getScene() == null || getWidth() <= 0 || getHeight() <= 0
                || andraNodo.getParent() == null) {
            andraCapa.setImage(null);
            return;
        }
        long andraInicio = System.nanoTime();
        Bounds andraPropio = localToScene(getLayoutBounds());
        Bounds andraEnPadre = andraNodo.getParent().sceneToLocal(andraPropio);
        if (andraEnPadre == null) {
            return;
        }
        SnapshotParameters andraParametros = new SnapshotParameters();
        andraParametros.setFill(Color.web("#F5F5F7"));
        andraParametros.setViewport(new Rectangle2D(andraEnPadre.getMinX() - ANDRA_MARGEN, andraEnPadre.getMinY() - ANDRA_MARGEN,
                andraEnPadre.getWidth() + ANDRA_MARGEN * 2, andraEnPadre.getHeight() + ANDRA_MARGEN * 2));
        int andraAncho = (int) Math.ceil(andraEnPadre.getWidth() + ANDRA_MARGEN * 2);
        int andraAlto = (int) Math.ceil(andraEnPadre.getHeight() + ANDRA_MARGEN * 2);
        if (andraLienzo == null || andraLienzo.getWidth() != andraAncho || andraLienzo.getHeight() != andraAlto) {
            andraLienzo = new WritableImage(andraAncho, andraAlto);
        }
        WritableImage andraImagen = andraNodo.snapshot(andraParametros, andraLienzo);
        andraCapa.setImage(andraImagen);
        andraCapa.setFitWidth(getWidth() + ANDRA_MARGEN * 2);
        andraCapa.setFitHeight(getHeight() + ANDRA_MARGEN * 2);
        andraCapa.relocate(-ANDRA_MARGEN, -ANDRA_MARGEN);
        double andraMilisegundos = (System.nanoTime() - andraInicio) / 1_000_000.0;
        andraCostoPromedio = andraCostoPromedio == 0 ? andraMilisegundos : andraCostoPromedio * 0.8 + andraMilisegundos * 0.2;
        if (andraCostoPromedio > ANDRA_LIMITE_MS) {
            andraDesenfoque = false;
            andraCapa.setImage(null);
            andraTinte.getStyleClass().add("sin-desenfoque");
        }
    }
}
