package pe.andra.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.andra.config.AndraJsonRecursos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DixonIdiomaServicio {

    public static final List<String[]> DIXON_IDIOMAS = List.of(
            new String[]{"es", "Español", "Castellano"},
            new String[]{"en", "English", "Inglés"},
            new String[]{"qu", "Runasimi", "Quechua"},
            new String[]{"ay", "Aymar aru", "Aimara"});

    private static final Pattern DIXON_TOK = Pattern.compile(
            "S/\\s?\\d[\\d.,]*|\\b[A-Z][A-Z0-9]*(?:-[A-Z0-9]+)+\\b|\\d[\\d.,:/]*(?:\\s?%)?|“[^”]*”");
    private static final Pattern DIXON_DOS_LETRAS = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúñÑ]{2}");
    private static final Pattern DIXON_UNA_LETRA = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúñÑ]");
    private static final Pattern DIXON_MARCAS = Pattern.compile("\\{(\\d+)}");
    private static final String DIXON_MESES_RX = "(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)";
    private static final Pattern DIXON_FECHA_CON_DIA = Pattern.compile(
            "\\b(lunes|martes|miércoles|jueves|viernes|sábado|domingo),?\\s+(\\d{1,2}) de " + DIXON_MESES_RX
                    + "(?:,? (?:de )?(\\d{4}))?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern DIXON_FECHA_SIN_DIA = Pattern.compile(
            "\\b(\\d{1,2}) de " + DIXON_MESES_RX + "(?:,? (?:de )?(\\d{4}))?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern DIXON_DIA_CORTO = Pattern.compile("\\b(Lun|Mar|Mié|Jue|Vie|Sáb|Dom)\\b(\\.?)");
    private static final Pattern DIXON_MES_CORTO = Pattern.compile("\\b(\\d{1,2}) (ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)\\b\\.?");
    private static final Pattern DIXON_DIA_CAPITAL = Pattern.compile(
            "(^|[^A-Za-zÁ-ú])(Lunes|Martes|Miércoles|Jueves|Viernes|Sábado|Domingo)(?![A-Za-zá-ú])");
    private static final String[] DIXON_SEPARADORES = {" · ", " • ", " — ", " – ", ". ", ": ", " | ", ", ", " ("};
    private static final List<String> DIXON_DIAS_ES = List.of("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo");
    private static final List<String> DIXON_MESES_ES = List.of("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre");
    private static final List<String> DIXON_DIAS_ES_CORTOS = List.of("lun", "mar", "mié", "jue", "vie", "sáb", "dom");
    private static final List<String> DIXON_MESES_ES_CORTOS = List.of("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago",
            "sep", "oct", "nov", "dic");
    private static final String[] DIXON_MESES_EN = {"January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"};

    private record DixonPrefijo(Pattern dixonPatron, Map<String, String> dixonPlantillas) {
    }

    private static final List<DixonPrefijo> DIXON_PREFIJOS = List.of(
            new DixonPrefijo(Pattern.compile("^Ver detalles de (.+)$"), Map.of("en", "View details of $", "qu", "$ sut'ichaykunata qhaway", "ay", "$ qhanancht'awinak uñjaña")),
            new DixonPrefijo(Pattern.compile("^Reservar (.+)$"), Map.of("en", "Book $", "qu", "$ waqaychay", "ay", "$ katuyasiña")),
            new DixonPrefijo(Pattern.compile("^Cancelar reserva (.+)$"), Map.of("en", "Cancel booking $", "qu", "$ waqaychayta saqiy", "ay", "$ katuyasiñ apanukuña")),
            new DixonPrefijo(Pattern.compile("^Ir a diapositiva (.+)$"), Map.of("en", "Go to slide $", "qu", "$ diapositivaman riy", "ay", "$ diapositivar saraña")),
            new DixonPrefijo(Pattern.compile("^Hola, ([^\\s.,]+)([.,!]?)$"), Map.of("en", "Hi, $", "qu", "Napaykullayki, $", "ay", "Kamisaki, $")),
            new DixonPrefijo(Pattern.compile("^Seguimiento de (.+)$"), Map.of("en", "Tracking for $", "qu", "$ qatipay", "ay", "$ arktawi")),
            new DixonPrefijo(Pattern.compile("^Solicitar (.+)$"), Map.of("en", "Request $", "qu", "$ mañakuy", "ay", "$ mayiña")));

    private static DixonIdiomaServicio dixonInstancia;

    private final AndraJsonRecursos dixonRecursos;
    private final ObjectProperty<String> dixonIdioma = new SimpleObjectProperty<>("es");
    private final Map<String, Map<String, String>> dixonDiccionario = new HashMap<>();
    private final Map<String, List<String>> dixonDias = new HashMap<>();
    private final Map<String, Map<String, Optional<String>>> dixonCache = new ConcurrentHashMap<>();
    private final Set<String> dixonFaltantes = ConcurrentHashMap.newKeySet();

    public DixonIdiomaServicio(AndraJsonRecursos dixonRecursos) {
        this.dixonRecursos = dixonRecursos;
    }

    @PostConstruct
    public void dixonCargar() {
        JsonNode dixonRaiz = dixonRecursos.andraTraducciones();
        for (String dixonTexto : List.of("en", "qu", "ay")) {
            dixonDiccionario.put(dixonTexto, new HashMap<>());
            dixonCache.put(dixonTexto, new ConcurrentHashMap<>());
            List<String> dixonLista = new ArrayList<>();
            dixonRaiz.path("dias").path(dixonTexto).forEach(dixonPosicionX -> dixonLista.add(dixonPosicionX.asText()));
            dixonDias.put(dixonTexto, dixonLista);
        }
        Iterator<Map.Entry<String, JsonNode>> dixonIterador = dixonRaiz.path("textos").fields();
        while (dixonIterador.hasNext()) {
            Map.Entry<String, JsonNode> dixonEntrada = dixonIterador.next();
            for (String dixonTexto : List.of("en", "qu", "ay")) {
                JsonNode dixonNodo = dixonEntrada.getValue().get(dixonTexto);
                if (dixonNodo != null && !dixonNodo.asText().isEmpty()) {
                    dixonDiccionario.get(dixonTexto).put(dixonEntrada.getKey(), dixonNodo.asText());
                }
            }
        }
        dixonAgregar("Sede Central", "Central Campus", "Central sede", "Central sede");
        dixonAgregar("Sede Norte", "North Campus", "Norte sede", "Norte sede");
        dixonAgregar("Sede Sur", "South Campus", "Sur sede", "Sur sede");
        dixonAgregar("Pago", "Payment", "Pagay", "Payllawi");
        dixonAgregar("Espacio", "Space", "Kiti", "Chiqa");
        dixonAgregar("Dirección de Servicios Académicos", "Academic Services Office", "Yachay Yanapakuy Kamachiy", "Yatiqañ Yanapt'añ Irpawi");
        dixonAgregar("Dependencia / Escuela profesional", "Office / School", "Kamachiy / Yachay programa", "Irpawi / Yatiqañ programa");
        dixonInstancia = this;
    }

    private void dixonAgregar(String dixonEspanol, String dixonIngles, String dixonQuechua, String dixonAimara) {
        dixonDiccionario.get("en").put(dixonEspanol, dixonIngles);
        dixonDiccionario.get("qu").put(dixonEspanol, dixonQuechua);
        dixonDiccionario.get("ay").put(dixonEspanol, dixonAimara);
    }

    public static DixonIdiomaServicio dixonActual() {
        return dixonInstancia;
    }

    public ObjectProperty<String> dixonIdiomaPropiedad() {
        return dixonIdioma;
    }

    public String dixonObtenerIdioma() {
        return dixonIdioma.get();
    }

    public void dixonAsignarIdioma(String dixonCodigo) {
        dixonIdioma.set(dixonCodigo);
    }

    public int dixonTotalTextos() {
        return dixonDiccionario.get("en").size();
    }

    public Set<String> dixonClaves() {
        return Collections.unmodifiableSet(dixonDiccionario.get("en").keySet());
    }

    public String dixonDirecta(String dixonClave, String dixonLengua) {
        Map<String, String> dixonMapa = dixonDiccionario.get(dixonLengua);
        return dixonMapa == null ? null : dixonMapa.get(dixonClave);
    }

    public Set<String> dixonFaltantes() {
        return Collections.unmodifiableSet(dixonFaltantes);
    }

    public static String dixonT(String dixonTexto) {
        return dixonInstancia == null ? dixonTexto : dixonInstancia.dixonTraducir(dixonTexto);
    }

    public static String dixonT(String dixonPlantilla, Object... dixonValores) {
        return dixonT(dixonFormatear(dixonPlantilla, dixonValores));
    }

    public static String dixonFormatear(String dixonPlantilla, Object... dixonValores) {
        Matcher dixonCoincidencia = DIXON_MARCAS.matcher(dixonPlantilla);
        StringBuilder dixonTexto = new StringBuilder();
        while (dixonCoincidencia.find()) {
            int dixonIndice = Integer.parseInt(dixonCoincidencia.group(1));
            dixonCoincidencia.appendReplacement(dixonTexto, Matcher.quoteReplacement(dixonIndice < dixonValores.length ? String.valueOf(dixonValores[dixonIndice]) : ""));
        }
        dixonCoincidencia.appendTail(dixonTexto);
        return dixonTexto.toString();
    }

    public static StringBinding dixonEnlace(String dixonTexto) {
        if (dixonInstancia == null) {
            return Bindings.createStringBinding(() -> dixonTexto);
        }
        return Bindings.createStringBinding(() -> dixonInstancia.dixonTraducir(dixonTexto), dixonInstancia.dixonIdioma);
    }

    public static StringBinding dixonEnlaceMayus(String dixonTexto) {
        if (dixonInstancia == null) {
            return Bindings.createStringBinding(() -> dixonTexto.toUpperCase(Locale.ROOT));
        }
        return Bindings.createStringBinding(() -> dixonInstancia.dixonTraducir(dixonTexto).toUpperCase(Locale.ROOT), dixonInstancia.dixonIdioma);
    }

    public static StringBinding dixonEnlace(java.util.function.Supplier<String> dixonTexto) {
        if (dixonInstancia == null) {
            return Bindings.createStringBinding(dixonTexto::get);
        }
        return Bindings.createStringBinding(() -> dixonInstancia.dixonTraducir(dixonTexto.get()), dixonInstancia.dixonIdioma);
    }

    public String dixonTraducir(String dixonTexto) {
        return dixonTraducir(dixonTexto, dixonIdioma.get());
    }

    public String dixonTraducir(String dixonTexto, String dixonLengua) {
        if (dixonTexto == null || "es".equals(dixonLengua) || dixonTexto.isEmpty()) {
            return dixonTexto;
        }
        String dixonTexto2 = dixonTexto.trim();
        if (dixonTexto2.isEmpty() || !DIXON_UNA_LETRA.matcher(dixonTexto2).find()) {
            return dixonTexto;
        }
        Map<String, Optional<String>> dixonMapa = dixonCache.get(dixonLengua);
        if (dixonMapa == null) {
            return dixonTexto;
        }
        Optional<String> dixonGuardado = dixonMapa.get(dixonTexto2);
        String dixonTexto3;
        if (dixonGuardado != null) {
            dixonTexto3 = dixonGuardado.orElse(null);
        } else {
            dixonTexto3 = dixonNucleo(dixonTexto2, dixonLengua, 0);
            if (dixonMapa.size() < 20000) {
                dixonMapa.put(dixonTexto2, Optional.ofNullable(dixonTexto3));
            }
            if (dixonTexto3 == null && dixonFaltantes.add(dixonTexto2)) {
                log.info("Sin traducción ({}): {}", dixonLengua, dixonTexto2);
            }
        }
        if (dixonTexto3 != null && !"en".equals(dixonLengua)) {
            List<String> dixonLista = dixonDias.get(dixonLengua);
            Matcher dixonCoincidencia = DIXON_DIA_CAPITAL.matcher(dixonTexto3);
            StringBuilder dixonTexto4 = new StringBuilder();
            while (dixonCoincidencia.find()) {
                String dixonAncho = dixonCoincidencia.group(2).toLowerCase();
                dixonCoincidencia.appendReplacement(dixonTexto4, Matcher.quoteReplacement(dixonCoincidencia.group(1) + dixonLista.get(DIXON_DIAS_ES.indexOf(dixonAncho))));
            }
            dixonCoincidencia.appendTail(dixonTexto4);
            dixonTexto3 = dixonTexto4.toString();
        }
        return dixonTexto3 == null ? dixonTexto : dixonTexto.replace(dixonTexto2, dixonTexto3);
    }

    private String dixonBuscar(String dixonTexto, String dixonLengua) {
        Map<String, String> dixonMapa = dixonDiccionario.get(dixonLengua);
        String dixonTexto2 = dixonMapa.get(dixonTexto);
        if (dixonTexto2 != null) {
            return dixonTexto2;
        }
        List<String> dixonValores = new ArrayList<>();
        Matcher dixonCoincidencia = DIXON_TOK.matcher(dixonTexto);
        StringBuilder dixonTexto3 = new StringBuilder();
        while (dixonCoincidencia.find()) {
            dixonValores.add(dixonCoincidencia.group());
            dixonCoincidencia.appendReplacement(dixonTexto3, Matcher.quoteReplacement("{" + (dixonValores.size() - 1) + "}"));
        }
        dixonCoincidencia.appendTail(dixonTexto3);
        if (!dixonValores.isEmpty()) {
            String dixonPlantilla = dixonMapa.get(dixonTexto3.toString());
            if (dixonPlantilla != null) {
                Matcher dixonCoincidencia2 = DIXON_MARCAS.matcher(dixonPlantilla);
                StringBuilder dixonSalida = new StringBuilder();
                while (dixonCoincidencia2.find()) {
                    int dixonIndice = Integer.parseInt(dixonCoincidencia2.group(1));
                    dixonCoincidencia2.appendReplacement(dixonSalida, Matcher.quoteReplacement(dixonIndice < dixonValores.size() ? dixonValores.get(dixonIndice) : ""));
                }
                dixonCoincidencia2.appendTail(dixonSalida);
                return dixonSalida.toString();
            }
        }
        return null;
    }

    private String dixonNucleo(String dixonTexto, String dixonLengua, int dixonProfundidad) {
        if (!DIXON_DOS_LETRAS.matcher(dixonTexto).find()) {
            return null;
        }
        String dixonTexto2 = dixonBuscar(dixonTexto, dixonLengua);
        if (dixonTexto2 != null) {
            return dixonTexto2;
        }
        if (dixonProfundidad > 4) {
            return null;
        }
        String dixonFecha = dixonFechas(dixonTexto, dixonLengua);
        if (dixonFecha != null) {
            String dixonResultado = dixonNucleo(dixonFecha, dixonLengua, dixonProfundidad + 1);
            return dixonResultado != null ? dixonResultado : dixonFecha;
        }
        for (String dixonSep : DIXON_SEPARADORES) {
            int dixonIndice = dixonTexto.indexOf(dixonSep);
            if (dixonIndice > 0) {
                String dixonTextoA = dixonTexto.substring(0, dixonIndice);
                String dixonTextoB = dixonTexto.substring(dixonIndice + dixonSep.length());
                String dixonTraduccionA = dixonNucleo(dixonTextoA, dixonLengua, dixonProfundidad + 1);
                String dixonTraduccionB = " (".equals(dixonSep) ? dixonNucleo(dixonTextoB.replaceAll("\\)$", ""), dixonLengua, dixonProfundidad + 1)
                        : dixonNucleo(dixonTextoB, dixonLengua, dixonProfundidad + 1);
                if (dixonTraduccionA != null || dixonTraduccionB != null) {
                    String dixonIzquierda = dixonTraduccionA != null ? dixonTraduccionA : dixonTextoA;
                    String dixonDerecha = " (".equals(dixonSep) && dixonTraduccionB != null ? dixonTraduccionB + ")" : (dixonTraduccionB != null ? dixonTraduccionB : dixonTextoB);
                    return dixonIzquierda + dixonSep + dixonDerecha;
                }
            }
        }
        for (DixonPrefijo dixonPrefijo : DIXON_PREFIJOS) {
            Matcher dixonPosicionX = dixonPrefijo.dixonPatron().matcher(dixonTexto);
            if (dixonPosicionX.matches()) {
                String dixonCola = dixonNucleo(dixonPosicionX.group(1), dixonLengua, dixonProfundidad + 1);
                String dixonFin = dixonPosicionX.groupCount() >= 2 && dixonPosicionX.group(2) != null ? dixonPosicionX.group(2) : "";
                return dixonPrefijo.dixonPlantillas().get(dixonLengua).replace("$", dixonCola != null ? dixonCola : dixonPosicionX.group(1)) + dixonFin;
            }
        }
        return null;
    }

    private String dixonFechas(String dixonTexto, String dixonLengua) {
        List<String> dixonLista = dixonDias.get(dixonLengua);
        boolean dixonCambio = false;
        Matcher dixonCoincidencia = DIXON_FECHA_CON_DIA.matcher(dixonTexto);
        StringBuilder dixonTexto2 = new StringBuilder();
        while (dixonCoincidencia.find()) {
            dixonCambio = true;
            int dixonIndiceDia = DIXON_DIAS_ES.indexOf(dixonCoincidencia.group(1).toLowerCase());
            int dixonIndiceMes = DIXON_MESES_ES.indexOf(dixonCoincidencia.group(3).toLowerCase());
            String dixonAnio = dixonCoincidencia.group(4) != null ? ", " + dixonCoincidencia.group(4) : "";
            String dixonResultado = "en".equals(dixonLengua) ? dixonLista.get(dixonIndiceDia) + ", " + DIXON_MESES_EN[dixonIndiceMes] + " " + dixonCoincidencia.group(2) + dixonAnio
                    : dixonLista.get(dixonIndiceDia) + ", " + dixonCoincidencia.group(2) + " " + DIXON_MESES_ES.get(dixonIndiceMes) + ("qu".equals(dixonLengua) ? " killa" : " phaxsi") + dixonAnio;
            dixonCoincidencia.appendReplacement(dixonTexto2, Matcher.quoteReplacement(dixonResultado));
        }
        dixonCoincidencia.appendTail(dixonTexto2);
        String dixonResultado1 = dixonTexto2.toString();
        Matcher dixonCoincidencia2 = DIXON_FECHA_SIN_DIA.matcher(dixonResultado1);
        StringBuilder dixonTexto22 = new StringBuilder();
        while (dixonCoincidencia2.find()) {
            dixonCambio = true;
            int dixonIndiceMes = DIXON_MESES_ES.indexOf(dixonCoincidencia2.group(2).toLowerCase());
            String dixonAnio = dixonCoincidencia2.group(3) != null ? ", " + dixonCoincidencia2.group(3) : "";
            String dixonResultado = "en".equals(dixonLengua) ? DIXON_MESES_EN[dixonIndiceMes] + " " + dixonCoincidencia2.group(1) + dixonAnio
                    : dixonCoincidencia2.group(1) + " " + DIXON_MESES_ES.get(dixonIndiceMes) + ("qu".equals(dixonLengua) ? " killa" : " phaxsi") + dixonAnio;
            dixonCoincidencia2.appendReplacement(dixonTexto22, Matcher.quoteReplacement(dixonResultado));
        }
        dixonCoincidencia2.appendTail(dixonTexto22);
        String dixonResultado2 = dixonTexto22.toString();
        if ("en".equals(dixonLengua)) {
            Matcher dixonCoincidencia3 = DIXON_DIA_CORTO.matcher(dixonResultado2);
            StringBuilder dixonTexto3 = new StringBuilder();
            while (dixonCoincidencia3.find()) {
                dixonCambio = true;
                dixonCoincidencia3.appendReplacement(dixonTexto3, Matcher.quoteReplacement(dixonLista.get(DIXON_DIAS_ES_CORTOS.indexOf(dixonCoincidencia3.group(1).toLowerCase())).substring(0, 3)));
            }
            dixonCoincidencia3.appendTail(dixonTexto3);
            Matcher dixonCoincidencia4 = DIXON_MES_CORTO.matcher(dixonTexto3.toString());
            StringBuilder dixonTexto4 = new StringBuilder();
            while (dixonCoincidencia4.find()) {
                dixonCambio = true;
                dixonCoincidencia4.appendReplacement(dixonTexto4, Matcher.quoteReplacement(DIXON_MESES_EN[DIXON_MESES_ES_CORTOS.indexOf(dixonCoincidencia4.group(2))].substring(0, 3) + " " + dixonCoincidencia4.group(1)));
            }
            dixonCoincidencia4.appendTail(dixonTexto4);
            dixonResultado2 = dixonTexto4.toString();
        }
        return dixonCambio ? dixonResultado2 : null;
    }
}
