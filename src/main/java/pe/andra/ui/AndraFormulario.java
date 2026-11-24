package pe.andra.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import pe.andra.service.AndraIdiomaServicio;
import pe.andra.util.AndraFechas;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AndraFormulario {

    private static final DateTimeFormatter ANDRA_DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<AndraCampo<?>> andraCampos = new ArrayList<>();

    public <C extends javafx.scene.layout.Region> AndraCampo<C> andraAgregar(AndraCampo<C> andraCampo) {
        andraCampos.add(andraCampo);
        return andraCampo;
    }

    public boolean andraValidar() {
        AndraCampo<?> andraPrimero = null;
        for (AndraCampo<?> andraCampo : andraCampos) {
            if (andraCampo.andraNodo().getScene() != null && andraCampo.andraValidar() != null && andraPrimero == null) {
                andraPrimero = andraCampo;
            }
        }
        if (andraPrimero != null) {
            andraPrimero.andraControl().requestFocus();
            return false;
        }
        return true;
    }

    public static AndraCampo<TextField> andraTexto(String andraEtiqueta, String andraMarcador, String andraValor) {
        TextField andraCampo = new TextField(andraValor == null ? "" : andraValor);
        if (andraMarcador != null) {
            AndraComponentes.andraIndicacion(andraCampo, andraMarcador);
        }
        return new AndraCampo<>(andraEtiqueta, andraCampo);
    }

    public static AndraCampo<TextField> andraNumero(String andraEtiqueta, String andraMarcador, String andraValor, int andraMaximoDigitos) {
        AndraCampo<TextField> andraCampo = andraTexto(andraEtiqueta, andraMarcador, andraValor);
        andraSoloDigitos(andraCampo.andraControl(), andraMaximoDigitos);
        return andraCampo;
    }

    public static AndraCampo<PasswordField> andraClave(String andraEtiqueta) {
        return new AndraCampo<>(andraEtiqueta, new PasswordField());
    }

    public static AndraCampo<TextArea> andraArea(String andraEtiqueta, String andraMarcador) {
        TextArea andraAreaTexto = new TextArea();
        andraAreaTexto.setWrapText(true);
        andraAreaTexto.setPrefRowCount(3);
        if (andraMarcador != null) {
            AndraComponentes.andraIndicacion(andraAreaTexto, andraMarcador);
        }
        return new AndraCampo<>(andraEtiqueta, andraAreaTexto);
    }

    public static AndraCampo<DatePicker> andraFecha(String andraEtiqueta, LocalDate andraValor, LocalDate andraMinimo, LocalDate andraMaximo) {
        DatePicker andraSelectorFecha = new DatePicker(andraValor);
        andraConfigurarFecha(andraSelectorFecha, andraMinimo, andraMaximo);
        return new AndraCampo<>(andraEtiqueta, andraSelectorFecha);
    }

    public static void andraConfigurarFecha(DatePicker andraSelectorFecha, LocalDate andraMinimo, LocalDate andraMaximo) {
        andraSelectorFecha.setEditable(false);
        andraSelectorFecha.setShowWeekNumbers(false);
        andraSelectorFecha.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate andraFecha) {
                return andraFecha == null ? "" : andraFecha.format(ANDRA_DMY);
            }

            @Override
            public LocalDate fromString(String andraTexto) {
                return andraTexto == null || andraTexto.isBlank() ? null : LocalDate.parse(andraTexto, ANDRA_DMY);
            }
        });
        andraSelectorFecha.setDayCellFactory(andraSelectorFecha2 -> new DateCell() {
            @Override
            public void updateItem(LocalDate andraElemento, boolean andraVacio) {
                super.updateItem(andraElemento, andraVacio);
                if (andraElemento != null && ((andraMinimo != null && andraElemento.isBefore(andraMinimo)) || (andraMaximo != null && andraElemento.isAfter(andraMaximo)))) {
                    setDisable(true);
                }
            }
        });
    }

    public static AndraCampo<ComboBox<AndraOpcion>> andraSelector(String andraEtiqueta, List<AndraOpcion> andraOpciones, String andraMarcador, String andraValor) {
        ComboBox<AndraOpcion> andraCombo = new ComboBox<>();
        andraConfigurarSelector(andraCombo, andraOpciones, andraMarcador, andraValor);
        return new AndraCampo<>(andraEtiqueta, andraCombo);
    }

    public static void andraConfigurarSelector(ComboBox<AndraOpcion> andraCombo, List<AndraOpcion> andraOpciones, String andraMarcador, String andraValor) {
        List<AndraOpcion> andraElementos = new ArrayList<>();
        if (andraMarcador != null) {
            andraElementos.add(AndraOpcion.andraDe("", andraMarcador));
        }
        andraElementos.addAll(andraOpciones);
        andraCombo.setItems(FXCollections.observableArrayList(andraElementos));
        andraCombo.setMaxWidth(Double.MAX_VALUE);
        andraCombo.setVisibleRowCount(12);
        andraCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AndraOpcion andraOpcion) {
                return andraOpcion == null ? "" : AndraIdiomaServicio.andraT(andraOpcion.andraEtiqueta());
            }

            @Override
            public AndraOpcion fromString(String andraTexto) {
                return null;
            }
        });
        andraCombo.setCellFactory(andraLista -> new ListCell<>() {
            @Override
            protected void updateItem(AndraOpcion andraOpcion, boolean andraVacio) {
                super.updateItem(andraOpcion, andraVacio);
                getStyleClass().remove("grupo");
                setDisable(false);
                if (andraVacio || andraOpcion == null) {
                    setText(null);
                    return;
                }
                setText(AndraIdiomaServicio.andraT(andraOpcion.andraEtiqueta()));
                if (andraOpcion.andraGrupo()) {
                    getStyleClass().add("grupo");
                    setDisable(true);
                }
            }
        });
        andraCombo.valueProperty().addListener((andraObservable, andraOpcionA, andraOpcionB) -> {
            if (andraOpcionB != null && andraOpcionB.andraGrupo()) {
                andraCombo.setValue(andraOpcionA);
            }
        });
        andraSeleccionar(andraCombo, andraValor == null ? "" : andraValor);
        if (AndraIdiomaServicio.andraActual() != null) {
            ChangeListener<String> andraRefresco = (andraObservable, andraTextoA, andraTextoB) -> {
                AndraOpcion andraActual = andraCombo.getValue();
                List<AndraOpcion> andraCopia = new ArrayList<>(andraCombo.getItems());
                andraCombo.getItems().setAll(andraCopia);
                andraCombo.setValue(andraActual);
            };
            andraCombo.getProperties().put("refrescoIdioma", andraRefresco);
            AndraIdiomaServicio.andraActual().andraIdiomaPropiedad().addListener(new WeakChangeListener<>(andraRefresco));
        }
    }

    public static void andraSeleccionar(ComboBox<AndraOpcion> andraCombo, String andraValor) {
        andraCombo.getItems().stream().filter(andraOpcion -> !andraOpcion.andraGrupo() && andraOpcion.andraValor().equals(andraValor)).findFirst()
                .ifPresentOrElse(andraCombo::setValue, () -> {
                    if (!andraCombo.getItems().isEmpty() && !andraCombo.getItems().get(0).andraGrupo()) {
                        andraCombo.setValue(andraCombo.getItems().get(0));
                    }
                });
    }

    public static String andraValor(ComboBox<AndraOpcion> andraCombo) {
        return andraCombo.getValue() == null ? "" : andraCombo.getValue().andraValor();
    }

    public static AndraCampo<CheckBox> andraCasilla(String andraTexto) {
        CheckBox andraCasilla = new CheckBox();
        AndraComponentes.andraFijar(andraCasilla, andraTexto);
        andraCasilla.setWrapText(true);
        andraCasilla.getStyleClass().add("casilla");
        AndraCampo<CheckBox> andraCampo = new AndraCampo<>(null, andraCasilla);
        andraCasilla.selectedProperty().addListener((andraObservable, andraBanderaA, andraBanderaB) -> andraCampo.andraLimpiar());
        return andraCampo;
    }

    public static class AndraArchivo extends HBox {
        private final Label andraNombre = AndraComponentes.andraEtiqueta("Ningún archivo seleccionado", "archivo-nombre");
        private File andraElegido;
        private Consumer<File> andraAlElegir;

        public AndraArchivo(String andraExtensiones, AndraNotificador andraNotificador, Consumer<File> andraAlElegirInicial) {
            this.andraAlElegir = andraAlElegirInicial;
            Button andraElegir = AndraComponentes.andraBotonSm("Elegir archivo", "btn-secondary", "fth-upload", null);
            andraElegir.setOnAction(andraEvento -> {
                FileChooser andraSelectorArchivo = new FileChooser();
                andraSelectorArchivo.getExtensionFilters().add(new FileChooser.ExtensionFilter(andraExtensiones, andraExtensiones.split(" ")));
                File andraArchivo = andraSelectorArchivo.showOpenDialog(andraNotificador.andraVentana());
                if (andraArchivo != null) {
                    if (andraArchivo.length() > 5L * 1024 * 1024) {
                        andraNotificador.andraError("El archivo supera los 5 MB.");
                        return;
                    }
                    andraElegido = andraArchivo;
                    andraNombre.textProperty().unbind();
                    andraNombre.setText(andraArchivo.getName());
                    if (andraAlElegir != null) {
                        andraAlElegir.accept(andraArchivo);
                    }
                }
            });
            getChildren().addAll(andraElegir, andraNombre);
            getStyleClass().add("archivo");
            setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(andraNombre, Priority.ALWAYS);
        }

        public File andraObtenerElegido() {
            return andraElegido;
        }
    }

    public static AndraCampo<AndraArchivo> andraArchivo(String andraEtiqueta, String andraExtensiones, String andraAyuda, AndraNotificador andraNotificador, Consumer<File> andraAlElegir) {
        AndraArchivo andraArchivo = new AndraArchivo(andraExtensiones, andraNotificador, null);
        AndraCampo<AndraArchivo> andraCampo = new AndraCampo<>(andraEtiqueta, andraArchivo);
        andraArchivo.andraAlElegir = andraArchivo2 -> {
            andraCampo.andraLimpiar();
            if (andraAlElegir != null) {
                andraAlElegir.accept(andraArchivo2);
            }
        };
        andraCampo.andraAyuda(andraAyuda == null ? "PDF, JPG o PNG. Máximo 5 MB." : andraAyuda);
        return andraCampo;
    }

    public static GridPane andraRejilla(int andraColumnas, javafx.scene.Node... andraCampos) {
        GridPane andraRejilla = new GridPane();
        andraRejilla.setHgap(24);
        andraRejilla.setVgap(16);
        for (int andraIndice = 0; andraIndice < andraColumnas; andraIndice++) {
            ColumnConstraints andraCol = new ColumnConstraints();
            andraCol.setPercentWidth(100.0 / andraColumnas);
            andraCol.setHgrow(Priority.ALWAYS);
            andraRejilla.getColumnConstraints().add(andraCol);
        }
        for (int andraIndice = 0; andraIndice < andraCampos.length; andraIndice++) {
            andraRejilla.add(andraCampos[andraIndice], andraIndice % andraColumnas, andraIndice / andraColumnas);
        }
        return andraRejilla;
    }

    public static void andraSoloDigitos(TextField andraCampo, int andraMaximo) {
        andraCampo.setTextFormatter(new TextFormatter<String>(andraCambio -> {
            String andraNuevo = andraCambio.getControlNewText();
            return andraNuevo.matches("\\d{0," + andraMaximo + "}") ? andraCambio : null;
        }));
    }

    public static String andraTextoFecha(LocalDate andraFecha) {
        return andraFecha == null ? "" : AndraFechas.andraDmy(andraFecha);
    }

    public static VBox andraColumna(javafx.scene.Node... andraNodos) {
        VBox andraCajaVertical = new VBox(14, andraNodos);
        return andraCajaVertical;
    }

    public static FontIcon andraIconoCampo(String andraLiteral) {
        return new FontIcon(andraLiteral);
    }
}
