package pe.andra.ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.util.DixonFechas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;

public final class AndraCalendario {

    private static final int ANDRA_DIAS_MAXIMOS = 90;

    private final AndraNotificador andraNotificador;
    private final Consumer<LocalDate> andraAlElegir;
    private final VBox andraCuerpo = new VBox();
    private LocalDate andraSeleccion;
    private YearMonth andraMes;

    private AndraCalendario(AndraNotificador andraNotificador, LocalDate andraInicial, Consumer<LocalDate> andraAlElegir) {
        this.andraNotificador = andraNotificador;
        this.andraAlElegir = andraAlElegir;
        this.andraSeleccion = andraInicial;
        this.andraMes = YearMonth.from(andraInicial);
    }

    public static void andraAbrir(AndraNotificador andraNotificador, LocalDate andraInicial, Consumer<LocalDate> andraAlElegir) {
        AndraCalendario andraCalendario = new AndraCalendario(andraNotificador, andraInicial, andraAlElegir);
        andraCalendario.andraDibujar();
        andraNotificador.andraModal("Elige una fecha", andraCalendario.andraCuerpo, false,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Ver horarios", "btn-primary", null, andraEvento -> andraCalendario.andraConfirmar()));
    }

    private void andraConfirmar() {
        andraNotificador.andraCerrarModal();
        andraAlElegir.accept(andraSeleccion);
    }

    private void andraDibujar() {
        LocalDate andraHoy = DixonFechas.dixonHoy();
        LocalDate andraMaximo = andraHoy.plusDays(ANDRA_DIAS_MAXIMOS);
        Button andraAnterior = AndraComponentes.andraIconoRedondo("fth-chevron-left", "Mes anterior", andraEvento -> {
            andraMes = andraMes.minusMonths(1);
            andraDibujar();
        });
        andraAnterior.setDisable(!andraMes.isAfter(YearMonth.from(andraHoy)));
        Button andraSiguiente = AndraComponentes.andraIconoRedondo("fth-chevron-right", "Mes siguiente", andraEvento -> {
            andraMes = andraMes.plusMonths(1);
            andraDibujar();
        });
        andraSiguiente.setDisable(!andraMes.isBefore(YearMonth.from(andraMaximo)));
        Label andraTitulo = AndraComponentes.andraEtiqueta(DixonFechas.dixonMesAnio(andraMes.atDay(1)), "cal-mes");
        HBox andraCabecera = new HBox(andraAnterior, AndraComponentes.andraEspacio(), andraTitulo, AndraComponentes.andraEspacio(), andraSiguiente);
        andraCabecera.getStyleClass().add("cal-head");

        GridPane andraGrilla = new GridPane();
        andraGrilla.setHgap(4);
        andraGrilla.setVgap(4);
        for (int andraIndice = 0; andraIndice < 7; andraIndice++) {
            ColumnConstraints andraCol = new ColumnConstraints();
            andraCol.setPercentWidth(100.0 / 7);
            andraCol.setHalignment(HPos.CENTER);
            andraGrilla.getColumnConstraints().add(andraCol);
        }
        String[] andraDias = {"L", "M", "M", "J", "V", "S", "D"};
        for (int andraIndice = 0; andraIndice < 7; andraIndice++) {
            andraGrilla.add(AndraComponentes.andraLiteral(andraDias[andraIndice], "cal-wd"), andraIndice, 0);
        }
        int andraDesfase = andraMes.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        for (int andraDias2 = 1; andraDias2 <= andraMes.lengthOfMonth(); andraDias2++) {
            LocalDate andraFecha = andraMes.atDay(andraDias2);
            Button andraDia = new Button(String.valueOf(andraDias2));
            andraDia.getStyleClass().add("cal-day");
            if (andraFecha.equals(andraHoy)) {
                andraDia.getStyleClass().add("today");
            }
            if (andraFecha.equals(andraSeleccion)) {
                andraDia.getStyleClass().add("on");
            }
            andraDia.setDisable(andraFecha.isBefore(andraHoy) || andraFecha.isAfter(andraMaximo));
            AndraComponentes.andraAccesible(andraDia, DixonFechas.dixonLargo(andraFecha) + (andraFecha.equals(andraHoy) ? ", hoy" : ""));
            andraDia.setMaxWidth(Double.MAX_VALUE);
            andraDia.setOnAction(andraEvento -> {
                andraSeleccion = andraFecha;
                andraDibujar();
            });
            andraDia.setOnMouseClicked(andraEvento -> {
                if (andraEvento.getButton() == MouseButton.PRIMARY && andraEvento.getClickCount() == 2) {
                    andraSeleccion = andraFecha;
                    andraConfirmar();
                }
            });
            int andraPosicion = andraDesfase + andraDias2 - 1;
            andraGrilla.add(andraDia, andraPosicion % 7, 1 + andraPosicion / 7);
        }
        TextFlow andraElegido = AndraComponentes.andraFlujo(AndraComponentes.andraTexto("Seleccionado:", "hint"), AndraComponentes.andraTexto(" "),
                AndraComponentes.andraTexto(DixonFechas.dixonLargo(andraSeleccion), "strong"));
        VBox.setMargin(andraElegido, new Insets(12, 0, 0, 0));
        andraCuerpo.getChildren().setAll(andraCabecera, andraGrilla, andraElegido);
    }
}
