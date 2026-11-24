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
public class AndraIdiomaServicio {

    public static final List<String[]> ANDRA_IDIOMAS = List.of(
            new String[]{"es", "Español", "Castellano"},
            new String[]{"en", "English", "Inglés"},
            new String[]{"qu", "Runasimi", "Quechua"},
            new String[]{"ay", "Aymar aru", "Aimara"});

    private static final Pattern ANDRA_TOK = Pattern.compile(
            "S/\\s?\\d[\\d.,]*|\\b[A-Z][A-Z0-9]*(?:-[A-Z0-9]+)+\\b|\\d[\\d.,:/]*(?:\\s?%)?|“[^”]*”");
    private static final Pattern ANDRA_DOS_LETRAS = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúñÑ]{2}");
    private static final Pattern ANDRA_UNA_LETRA = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúñÑ]");
    private static final Pattern ANDRA_MARCAS = Pattern.compile("\\{(\\d+)}");
    private static final String ANDRA_MESES_RX = "(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)";
    private static final Pattern ANDRA_FECHA_CON_DIA = Pattern.compile(
            "\\b(lunes|martes|miércoles|jueves|viernes|sábado|domingo),?\\s+(\\d{1,2}) de " + ANDRA_MESES_RX
                    + "(?:,? (?:de )?(\\d{4}))?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ANDRA_FECHA_SIN_DIA = Pattern.compile(
            "\\b(\\d{1,2}) de " + ANDRA_MESES_RX + "(?:,? (?:de )?(\\d{4}))?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ANDRA_DIA_CORTO = Pattern.compile("\\b(Lun|Mar|Mié|Jue|Vie|Sáb|Dom)\\b(\\.?)");
    private static final Pattern ANDRA_MES_CORTO = Pattern.compile("\\b(\\d{1,2}) (ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)\\b\\.?");
    private static final Pattern ANDRA_DIA_CAPITAL = Pattern.compile(
            "(^|[^A-Za-zÁ-ú])(Lunes|Martes|Miércoles|Jueves|Viernes|Sábado|Domingo)(?![A-Za-zá-ú])");
    private static final String[] ANDRA_SEPARADORES = {" · ", " • ", " — ", " – ", ". ", ": ", " | ", ", ", " ("};
    private static final List<String> ANDRA_DIAS_ES = List.of("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo");
    private static final List<String> ANDRA_MESES_ES = List.of("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre");
    private static final List<String> ANDRA_DIAS_ES_CORTOS = List.of("lun", "mar", "mié", "jue", "vie", "sáb", "dom");
    private static final List<String> ANDRA_MESES_ES_CORTOS = List.of("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago",
            "sep", "oct", "nov", "dic");
    private static final String[] ANDRA_MESES_EN = {"January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"};

    private record AndraPrefijo(Pattern andraPatron, Map<String, String> andraPlantillas) {
    }

    private static final List<AndraPrefijo> ANDRA_PREFIJOS = List.of(
            new AndraPrefijo(Pattern.compile("^Ver detalles de (.+)$"), Map.of("en", "View details of $", "qu", "$ sut'ichaykunata qhaway", "ay", "$ qhanancht'awinak uñjaña")),
            new AndraPrefijo(Pattern.compile("^Reservar (.+)$"), Map.of("en", "Book $", "qu", "$ waqaychay", "ay", "$ katuyasiña")),
            new AndraPrefijo(Pattern.compile("^Cancelar reserva (.+)$"), Map.of("en", "Cancel booking $", "qu", "$ waqaychayta saqiy", "ay", "$ katuyasiñ apanukuña")),
            new AndraPrefijo(Pattern.compile("^Ir a diapositiva (.+)$"), Map.of("en", "Go to slide $", "qu", "$ diapositivaman riy", "ay", "$ diapositivar saraña")),
            new AndraPrefijo(Pattern.compile("^Hola, ([^\\s.,]+)([.,!]?)$"), Map.of("en", "Hi, $", "qu", "Napaykullayki, $", "ay", "Kamisaki, $")),
            new AndraPrefijo(Pattern.compile("^Seguimiento de (.+)$"), Map.of("en", "Tracking for $", "qu", "$ qatipay", "ay", "$ arktawi")),
            new AndraPrefijo(Pattern.compile("^Solicitar (.+)$"), Map.of("en", "Request $", "qu", "$ mañakuy", "ay", "$ mayiña")));

    private static AndraIdiomaServicio andraInstancia;

    private final AndraJsonRecursos andraRecursos;
    private final ObjectProperty<String> andraIdioma = new SimpleObjectProperty<>("es");
    private final Map<String, Map<String, String>> andraDiccionario = new HashMap<>();
    private final Map<String, List<String>> andraDias = new HashMap<>();
    private final Map<String, Map<String, Optional<String>>> andraCache = new ConcurrentHashMap<>();
    private final Set<String> andraFaltantes = ConcurrentHashMap.newKeySet();

    public AndraIdiomaServicio(AndraJsonRecursos andraRecursos) {
        this.andraRecursos = andraRecursos;
    }

    @PostConstruct
    public void andraCargar() {
        JsonNode andraRaiz = andraRecursos.andraTraducciones();
        for (String andraTexto : List.of("en", "qu", "ay")) {
            andraDiccionario.put(andraTexto, new HashMap<>());
            andraCache.put(andraTexto, new ConcurrentHashMap<>());
            List<String> andraLista = new ArrayList<>();
            andraRaiz.path("dias").path(andraTexto).forEach(andraPosicionX -> andraLista.add(andraPosicionX.asText()));
            andraDias.put(andraTexto, andraLista);
        }
        Iterator<Map.Entry<String, JsonNode>> andraIterador = andraRaiz.path("textos").fields();
        while (andraIterador.hasNext()) {
            Map.Entry<String, JsonNode> andraEntrada = andraIterador.next();
            for (String andraTexto : List.of("en", "qu", "ay")) {
                JsonNode andraNodo = andraEntrada.getValue().get(andraTexto);
                if (andraNodo != null && !andraNodo.asText().isEmpty()) {
                    andraDiccionario.get(andraTexto).put(andraEntrada.getKey(), andraNodo.asText());
                }
            }
        }
        andraAgregar("Sede Central", "Central Campus", "Central sede", "Central sede");
        andraAgregar("Sede Norte", "North Campus", "Norte sede", "Norte sede");
        andraAgregar("Sede Sur", "South Campus", "Sur sede", "Sur sede");
        andraAgregar("Pago", "Payment", "Pagay", "Payllawi");
        andraAgregar("Espacio", "Space", "Kiti", "Chiqa");
        andraAgregar("Dirección de Servicios Académicos", "Academic Services Office", "Yachay Yanapakuy Kamachiy", "Yatiqañ Yanapt'añ Irpawi");
        andraAgregar("Dependencia / Escuela profesional", "Office / School", "Kamachiy / Yachay programa", "Irpawi / Yatiqañ programa");
        andraInstancia = this;
    }

    private void andraAgregar(String andraEspanol, String andraIngles, String andraQuechua, String andraAimara) {
        andraDiccionario.get("en").put(andraEspanol, andraIngles);
        andraDiccionario.get("qu").put(andraEspanol, andraQuechua);
        andraDiccionario.get("ay").put(andraEspanol, andraAimara);
    }

    public static AndraIdiomaServicio andraActual() {
        return andraInstancia;
    }

    public ObjectProperty<String> andraIdiomaPropiedad() {
        return andraIdioma;
    }

    public String andraObtenerIdioma() {
        return andraIdioma.get();
    }

    public void andraAsignarIdioma(String andraCodigo) {
        andraIdioma.set(andraCodigo);
    }

    public int andraTotalTextos() {
        return andraDiccionario.get("en").size();
    }

    public Set<String> andraClaves() {
        return Collections.unmodifiableSet(andraDiccionario.get("en").keySet());
    }

    public String andraDirecta(String andraClave, String andraLengua) {
        Map<String, String> andraMapa = andraDiccionario.get(andraLengua);
        return andraMapa == null ? null : andraMapa.get(andraClave);
    }

    public Set<String> andraFaltantes() {
        return Collections.unmodifiableSet(andraFaltantes);
    }

    public static String andraT(String andraTexto) {
        return andraInstancia == null ? andraTexto : andraInstancia.andraTraducir(andraTexto);
    }

    public static String andraT(String andraPlantilla, Object... andraValores) {
        return andraT(andraFormatear(andraPlantilla, andraValores));
    }

    public static String andraFormatear(String andraPlantilla, Object... andraValores) {
        Matcher andraCoincidencia = ANDRA_MARCAS.matcher(andraPlantilla);
        StringBuilder andraTexto = new StringBuilder();
        while (andraCoincidencia.find()) {
            int andraIndice = Integer.parseInt(andraCoincidencia.group(1));
            andraCoincidencia.appendReplacement(andraTexto, Matcher.quoteReplacement(andraIndice < andraValores.length ? String.valueOf(andraValores[andraIndice]) : ""));
        }
        andraCoincidencia.appendTail(andraTexto);
        return andraTexto.toString();
    }

    public static StringBinding andraEnlace(String andraTexto) {
        if (andraInstancia == null) {
            return Bindings.createStringBinding(() -> andraTexto);
        }
        return Bindings.createStringBinding(() -> andraInstancia.andraTraducir(andraTexto), andraInstancia.andraIdioma);
    }

    public static StringBinding andraEnlaceMayus(String andraTexto) {
        if (andraInstancia == null) {
            return Bindings.createStringBinding(() -> andraTexto.toUpperCase(Locale.ROOT));
        }
        return Bindings.createStringBinding(() -> andraInstancia.andraTraducir(andraTexto).toUpperCase(Locale.ROOT), andraInstancia.andraIdioma);
    }

    public static StringBinding andraEnlace(java.util.function.Supplier<String> andraTexto) {
        if (andraInstancia == null) {
            return Bindings.createStringBinding(andraTexto::get);
        }
        return Bindings.createStringBinding(() -> andraInstancia.andraTraducir(andraTexto.get()), andraInstancia.andraIdioma);
    }

    public String andraTraducir(String andraTexto) {
        return andraTraducir(andraTexto, andraIdioma.get());
    }

    public String andraTraducir(String andraTexto, String andraLengua) {
        if (andraTexto == null || "es".equals(andraLengua) || andraTexto.isEmpty()) {
            return andraTexto;
        }
        String andraTexto2 = andraTexto.trim();
        if (andraTexto2.isEmpty() || !ANDRA_UNA_LETRA.matcher(andraTexto2).find()) {
            return andraTexto;
        }
        Map<String, Optional<String>> andraMapa = andraCache.get(andraLengua);
        if (andraMapa == null) {
            return andraTexto;
        }
        Optional<String> andraGuardado = andraMapa.get(andraTexto2);
        String andraTexto3;
        if (andraGuardado != null) {
            andraTexto3 = andraGuardado.orElse(null);
        } else {
            andraTexto3 = andraNucleo(andraTexto2, andraLengua, 0);
            if (andraMapa.size() < 20000) {
                andraMapa.put(andraTexto2, Optional.ofNullable(andraTexto3));
            }
            if (andraTexto3 == null && andraFaltantes.add(andraTexto2)) {
                log.info("Sin traducción ({}): {}", andraLengua, andraTexto2);
            }
        }
        if (andraTexto3 != null && !"en".equals(andraLengua)) {
            List<String> andraLista = andraDias.get(andraLengua);
            Matcher andraCoincidencia = ANDRA_DIA_CAPITAL.matcher(andraTexto3);
            StringBuilder andraTexto4 = new StringBuilder();
            while (andraCoincidencia.find()) {
                String andraAncho = andraCoincidencia.group(2).toLowerCase();
                andraCoincidencia.appendReplacement(andraTexto4, Matcher.quoteReplacement(andraCoincidencia.group(1) + andraLista.get(ANDRA_DIAS_ES.indexOf(andraAncho))));
            }
            andraCoincidencia.appendTail(andraTexto4);
            andraTexto3 = andraTexto4.toString();
        }
        return andraTexto3 == null ? andraTexto : andraTexto.replace(andraTexto2, andraTexto3);
    }

    private String andraBuscar(String andraTexto, String andraLengua) {
        Map<String, String> andraMapa = andraDiccionario.get(andraLengua);
        String andraTexto2 = andraMapa.get(andraTexto);
        if (andraTexto2 != null) {
            return andraTexto2;
        }
        List<String> andraValores = new ArrayList<>();
        Matcher andraCoincidencia = ANDRA_TOK.matcher(andraTexto);
        StringBuilder andraTexto3 = new StringBuilder();
        while (andraCoincidencia.find()) {
            andraValores.add(andraCoincidencia.group());
            andraCoincidencia.appendReplacement(andraTexto3, Matcher.quoteReplacement("{" + (andraValores.size() - 1) + "}"));
        }
        andraCoincidencia.appendTail(andraTexto3);
        if (!andraValores.isEmpty()) {
            String andraPlantilla = andraMapa.get(andraTexto3.toString());
            if (andraPlantilla != null) {
                Matcher andraCoincidencia2 = ANDRA_MARCAS.matcher(andraPlantilla);
                StringBuilder andraSalida = new StringBuilder();
                while (andraCoincidencia2.find()) {
                    int andraIndice = Integer.parseInt(andraCoincidencia2.group(1));
                    andraCoincidencia2.appendReplacement(andraSalida, Matcher.quoteReplacement(andraIndice < andraValores.size() ? andraValores.get(andraIndice) : ""));
                }
                andraCoincidencia2.appendTail(andraSalida);
                return andraSalida.toString();
            }
        }
        return null;
    }

    private String andraNucleo(String andraTexto, String andraLengua, int andraProfundidad) {
        if (!ANDRA_DOS_LETRAS.matcher(andraTexto).find()) {
            return null;
        }
        String andraTexto2 = andraBuscar(andraTexto, andraLengua);
        if (andraTexto2 != null) {
            return andraTexto2;
        }
        if (andraProfundidad > 4) {
            return null;
        }
        String andraFecha = andraFechas(andraTexto, andraLengua);
        if (andraFecha != null) {
            String andraResultado = andraNucleo(andraFecha, andraLengua, andraProfundidad + 1);
            return andraResultado != null ? andraResultado : andraFecha;
        }
        for (String andraSep : ANDRA_SEPARADORES) {
            int andraIndice = andraTexto.indexOf(andraSep);
            if (andraIndice > 0) {
                String andraTextoA = andraTexto.substring(0, andraIndice);
                String andraTextoB = andraTexto.substring(andraIndice + andraSep.length());
                String andraTraduccionA = andraNucleo(andraTextoA, andraLengua, andraProfundidad + 1);
                String andraTraduccionB = " (".equals(andraSep) ? andraNucleo(andraTextoB.replaceAll("\\)$", ""), andraLengua, andraProfundidad + 1)
                        : andraNucleo(andraTextoB, andraLengua, andraProfundidad + 1);
                if (andraTraduccionA != null || andraTraduccionB != null) {
                    String andraIzquierda = andraTraduccionA != null ? andraTraduccionA : andraTextoA;
                    String andraDerecha = " (".equals(andraSep) && andraTraduccionB != null ? andraTraduccionB + ")" : (andraTraduccionB != null ? andraTraduccionB : andraTextoB);
                    return andraIzquierda + andraSep + andraDerecha;
                }
            }
        }
        for (AndraPrefijo andraPrefijo : ANDRA_PREFIJOS) {
            Matcher andraPosicionX = andraPrefijo.andraPatron().matcher(andraTexto);
            if (andraPosicionX.matches()) {
                String andraCola = andraNucleo(andraPosicionX.group(1), andraLengua, andraProfundidad + 1);
                String andraFin = andraPosicionX.groupCount() >= 2 && andraPosicionX.group(2) != null ? andraPosicionX.group(2) : "";
                return andraPrefijo.andraPlantillas().get(andraLengua).replace("$", andraCola != null ? andraCola : andraPosicionX.group(1)) + andraFin;
            }
        }
        return null;
    }

    private String andraFechas(String andraTexto, String andraLengua) {
        List<String> andraLista = andraDias.get(andraLengua);
        boolean andraCambio = false;
        Matcher andraCoincidencia = ANDRA_FECHA_CON_DIA.matcher(andraTexto);
        StringBuilder andraTexto2 = new StringBuilder();
        while (andraCoincidencia.find()) {
            andraCambio = true;
            int andraIndiceDia = ANDRA_DIAS_ES.indexOf(andraCoincidencia.group(1).toLowerCase());
            int andraIndiceMes = ANDRA_MESES_ES.indexOf(andraCoincidencia.group(3).toLowerCase());
            String andraAnio = andraCoincidencia.group(4) != null ? ", " + andraCoincidencia.group(4) : "";
            String andraResultado = "en".equals(andraLengua) ? andraLista.get(andraIndiceDia) + ", " + ANDRA_MESES_EN[andraIndiceMes] + " " + andraCoincidencia.group(2) + andraAnio
                    : andraLista.get(andraIndiceDia) + ", " + andraCoincidencia.group(2) + " " + ANDRA_MESES_ES.get(andraIndiceMes) + ("qu".equals(andraLengua) ? " killa" : " phaxsi") + andraAnio;
            andraCoincidencia.appendReplacement(andraTexto2, Matcher.quoteReplacement(andraResultado));
        }
        andraCoincidencia.appendTail(andraTexto2);
        String andraResultado1 = andraTexto2.toString();
        Matcher andraCoincidencia2 = ANDRA_FECHA_SIN_DIA.matcher(andraResultado1);
        StringBuilder andraTexto22 = new StringBuilder();
        while (andraCoincidencia2.find()) {
            andraCambio = true;
            int andraIndiceMes = ANDRA_MESES_ES.indexOf(andraCoincidencia2.group(2).toLowerCase());
            String andraAnio = andraCoincidencia2.group(3) != null ? ", " + andraCoincidencia2.group(3) : "";
            String andraResultado = "en".equals(andraLengua) ? ANDRA_MESES_EN[andraIndiceMes] + " " + andraCoincidencia2.group(1) + andraAnio
                    : andraCoincidencia2.group(1) + " " + ANDRA_MESES_ES.get(andraIndiceMes) + ("qu".equals(andraLengua) ? " killa" : " phaxsi") + andraAnio;
            andraCoincidencia2.appendReplacement(andraTexto22, Matcher.quoteReplacement(andraResultado));
        }
        andraCoincidencia2.appendTail(andraTexto22);
        String andraResultado2 = andraTexto22.toString();
        if ("en".equals(andraLengua)) {
            Matcher andraCoincidencia3 = ANDRA_DIA_CORTO.matcher(andraResultado2);
            StringBuilder andraTexto3 = new StringBuilder();
            while (andraCoincidencia3.find()) {
                andraCambio = true;
                andraCoincidencia3.appendReplacement(andraTexto3, Matcher.quoteReplacement(andraLista.get(ANDRA_DIAS_ES_CORTOS.indexOf(andraCoincidencia3.group(1).toLowerCase())).substring(0, 3)));
            }
            andraCoincidencia3.appendTail(andraTexto3);
            Matcher andraCoincidencia4 = ANDRA_MES_CORTO.matcher(andraTexto3.toString());
            StringBuilder andraTexto4 = new StringBuilder();
            while (andraCoincidencia4.find()) {
                andraCambio = true;
                andraCoincidencia4.appendReplacement(andraTexto4, Matcher.quoteReplacement(ANDRA_MESES_EN[ANDRA_MESES_ES_CORTOS.indexOf(andraCoincidencia4.group(2))].substring(0, 3) + " " + andraCoincidencia4.group(1)));
            }
            andraCoincidencia4.appendTail(andraTexto4);
            andraResultado2 = andraTexto4.toString();
        }
        return andraCambio ? andraResultado2 : null;
    }
}
