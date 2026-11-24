package pe.andra.ui;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.model.AndraEquipo;
import pe.andra.model.AndraEspacio;
import pe.andra.model.AndraTipoEspacio;
import pe.andra.util.AndraHash;

import java.util.Map;

public final class AndraIlustraciones {

    private static final Color ANDRA_P = Color.web("#0071E3");
    private static final Color ANDRA_SF = Color.web("#E8F2FD");
    private static final Color ANDRA_W = Color.WHITE;
    private static final Color ANDRA_B = Color.web("#D2D2D7");
    private static final Color ANDRA_T = Color.web("#1D1D1F");

    public static final Map<String, String> ANDRA_ICONOS_CATEGORIA = Map.of("speaker", "fth-speaker", "music", "fth-music",
            "ball", "fth-dribbble", "monitor", "fth-monitor", "lab", "fth-droplet");

    private AndraIlustraciones() {
    }

    public static String andraIconoCategoria(AndraEquipo andraEquipo) {
        return ANDRA_ICONOS_CATEGORIA.getOrDefault(andraEquipo.getAndraCategoria().getAndraIcono(), "fth-box");
    }

    private static Rectangle andraRect(double andraPosicionX, double andraPosicionY, double andraAncho, double andraAlto, double andraRadio, Color andraColor, double andraOpacidad) {
        Rectangle andraRectangulo = new Rectangle(andraPosicionX, andraPosicionY, andraAncho, andraAlto);
        andraRectangulo.setArcWidth(andraRadio * 2);
        andraRectangulo.setArcHeight(andraRadio * 2);
        andraRectangulo.setFill(andraColor);
        andraRectangulo.setOpacity(andraOpacidad);
        return andraRectangulo;
    }

    public static Group andraEspacio(AndraEspacio andraEspacio) {
        long andraAlto = AndraHash.andraNumero(andraEspacio.getAndraCodigo());
        Group andraGrupo = new Group();
        andraGrupo.getChildren().add(andraRect(0, 0, 400, 250, 0, ANDRA_SF, 1));
        AndraTipoEspacio andraTipo = andraEspacio.getAndraTipo();
        if (andraTipo == AndraTipoEspacio.AUDITORIO) {
            int andraFilas = andraEspacio.getAndraCapacidad() >= 1000 ? 7 : andraEspacio.getAndraCapacidad() >= 250 ? 5 : 4;
            Path andraArco = new Path(new MoveTo(60, 70), new QuadCurveTo(200, 30, 340, 70), new LineTo(340, 92),
                    new QuadCurveTo(200, 56, 60, 92), new ClosePath());
            andraArco.setFill(ANDRA_P);
            andraArco.setStroke(null);
            andraArco.setOpacity(0.9);
            andraGrupo.getChildren().addAll(andraArco, andraRect(120, 86, 160, 16, 8, ANDRA_W, 1));
            for (int andraNumero = 0; andraNumero < andraFilas; andraNumero++) {
                double andraPosicionY = 118 + andraNumero * (110.0 / andraFilas);
                double andraAncho = 150 + andraNumero * (220.0 / andraFilas);
                int andraCantidad = (int) Math.round(8 + andraNumero * 2.6);
                for (int andraIndice = 0; andraIndice < andraCantidad; andraIndice++) {
                    double andraPosicionX = 200 - andraAncho / 2 + andraIndice * (andraAncho / (andraCantidad - 1));
                    boolean andraAzul = (andraIndice + andraNumero + andraAlto) % 9 == 0;
                    andraGrupo.getChildren().add(andraRect(andraPosicionX - 5, andraPosicionY, 10, 70.0 / andraFilas, 3, andraAzul ? ANDRA_P : ANDRA_T, andraAzul ? 0.9 : 0.14 + andraNumero * 0.04));
                }
            }
        } else if (andraTipo == AndraTipoEspacio.LABORATORIO) {
            andraGrupo.getChildren().add(andraRect(0, 190, 400, 60, 0, ANDRA_W, 0.7));
            for (int andraNumero = 0; andraNumero < 2; andraNumero++) {
                for (int andraIndice = 0; andraIndice < 4; andraIndice++) {
                    double andraPosicionX = 40 + andraIndice * 86;
                    double andraPosicionY = 58 + andraNumero * 82;
                    andraGrupo.getChildren().addAll(andraRect(andraPosicionX, andraPosicionY, 64, 42, 6, ANDRA_T, 0.85),
                            andraRect(andraPosicionX + 4, andraPosicionY + 4, 56, 30, 3, (andraIndice + andraNumero + andraAlto) % 5 == 0 ? ANDRA_P : ANDRA_W, 1),
                            andraRect(andraPosicionX + 28, andraPosicionY + 42, 8, 10, 0, ANDRA_T, 0.6), andraRect(andraPosicionX - 6, andraPosicionY + 52, 76, 6, 3, ANDRA_B, 1));
                }
            }
        } else if (andraTipo == AndraTipoEspacio.AULA) {
            andraGrupo.getChildren().add(andraRect(80, 26, 240, 60, 6, ANDRA_T, 0.85));
            Line andraLinea1 = new Line(100, 50, 180, 50);
            Line andraLinea2 = new Line(100, 64, 220, 64);
            for (Line andraLinea : new Line[]{andraLinea1, andraLinea2}) {
                andraLinea.setStroke(ANDRA_W);
                andraLinea.setStrokeWidth(5);
                andraLinea.setStrokeLineCap(StrokeLineCap.ROUND);
                andraLinea.setOpacity(0.8);
            }
            andraGrupo.getChildren().addAll(andraLinea1, andraLinea2);
            for (int andraNumero = 0; andraNumero < 3; andraNumero++) {
                for (int andraIndice = 0; andraIndice < 5; andraIndice++) {
                    Rectangle andraCarpeta = andraRect(60 + andraIndice * 58, 108 + andraNumero * 44, 42, 24, 5, (andraIndice * 3 + andraNumero + andraAlto) % 7 == 0 ? ANDRA_P : ANDRA_W, 1);
                    andraCarpeta.setStroke(ANDRA_B);
                    andraGrupo.getChildren().add(andraCarpeta);
                }
            }
        } else if (andraTipo == AndraTipoEspacio.SALA) {
            Ellipse andraMesa = new Ellipse(200, 140, 110, 54);
            andraMesa.setFill(ANDRA_W);
            andraMesa.setStroke(ANDRA_B);
            Ellipse andraCentro = new Ellipse(200, 140, 60, 26);
            andraCentro.setFill(ANDRA_P);
            andraCentro.setOpacity(0.18);
            andraGrupo.getChildren().addAll(andraMesa, andraCentro);
            for (int andraIndice = 0; andraIndice < 10; andraIndice++) {
                double andraValorA = andraIndice / 10.0 * Math.PI * 2;
                Circle andraSilla = new Circle(200 + Math.cos(andraValorA) * 140, 140 + Math.sin(andraValorA) * 78, 13);
                boolean andraAzul = andraIndice == andraAlto % 10;
                andraSilla.setFill(andraAzul ? ANDRA_P : ANDRA_T);
                andraSilla.setOpacity(andraAzul ? 1 : 0.18);
                andraGrupo.getChildren().add(andraSilla);
            }
        } else {
            andraGrupo.getChildren().addAll(andraRect(40, 150, 320, 18, 6, ANDRA_T, 0.8), andraRect(60, 168, 12, 60, 0, ANDRA_T, 0.6), andraRect(328, 168, 12, 60, 0, ANDRA_T, 0.6));
            Circle andraEngranaje = new Circle(130, 100, 40);
            andraEngranaje.setFill(null);
            andraEngranaje.setStroke(ANDRA_P);
            andraEngranaje.setStrokeWidth(14);
            andraEngranaje.getStrokeDashArray().addAll(14.0, 9.0);
            Circle andraEje = new Circle(130, 100, 14, ANDRA_P);
            Rectangle andraCaja = andraRect(220, 70, 90, 70, 12, ANDRA_W, 1);
            andraCaja.setStroke(ANDRA_B);
            andraGrupo.getChildren().addAll(andraEngranaje, andraEje, andraCaja, andraRect(236, 88, 58, 10, 5, ANDRA_P, 0.5), andraRect(236, 106, 40, 10, 5, ANDRA_T, 0.2));
        }
        return andraGrupo;
    }

    public static Pane andraMarcoEspacio(AndraEspacio andraEspacio, Node[] andraObjetivoGiro) {
        Group andraArte = andraEspacio(andraEspacio);
        Group andraGiratorio = new Group(andraArte);
        andraObjetivoGiro[0] = andraGiratorio;
        Pane andraMarco = new Pane(andraGiratorio) {
            @Override
            protected void layoutChildren() {
                double andraAncho = getWidth();
                double andraAlto = getHeight();
                double andraEscala = Math.max(andraAncho / 400, andraAlto / 250);
                andraArte.getTransforms().setAll(new javafx.scene.transform.Scale(andraEscala, andraEscala, 0, 0));
                andraGiratorio.relocate((andraAncho - 400 * andraEscala) / 2, (andraAlto - 250 * andraEscala) / 2);
            }
        };
        Rectangle andraRecorte = new Rectangle();
        andraRecorte.widthProperty().bind(andraMarco.widthProperty());
        andraRecorte.heightProperty().bind(andraMarco.heightProperty());
        andraMarco.setClip(andraRecorte);
        andraMarco.setMinSize(0, 0);
        return andraMarco;
    }

    public static StackPane andraEquipo(AndraEquipo andraEquipo, Node[] andraObjetivoGiro) {
        Circle andraCirculo1 = new Circle();
        andraCirculo1.getStyleClass().addAll("eq-ring", "r1");
        Circle andraCirculo2 = new Circle();
        andraCirculo2.getStyleClass().addAll("eq-ring", "r2");
        StackPane andraIco = new StackPane(new FontIcon(andraIconoCategoria(andraEquipo)));
        andraIco.getStyleClass().add("eq-ico");
        andraObjetivoGiro[0] = andraIco;
        StackPane andraArte = new StackPane(andraCirculo1, andraCirculo2, andraIco);
        andraArte.setAlignment(Pos.CENTER);
        andraArte.widthProperty().addListener((andraObservable, andraNumeroA, andraNumeroB) -> {
            andraCirculo1.setRadius(andraNumeroB.doubleValue() * 0.29);
            andraCirculo2.setRadius(andraNumeroB.doubleValue() * 0.43);
        });
        Rectangle andraRecorte = new Rectangle();
        andraRecorte.widthProperty().bind(andraArte.widthProperty());
        andraRecorte.heightProperty().bind(andraArte.heightProperty());
        andraArte.setClip(andraRecorte);
        andraArte.setMinSize(0, 0);
        return andraArte;
    }
}
