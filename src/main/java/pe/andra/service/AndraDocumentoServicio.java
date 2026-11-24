package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.AndraDocumento;
import pe.andra.model.AndraDocumentoPersonal;
import pe.andra.model.AndraEscuelaProfesional;
import pe.andra.model.AndraTipoFavorito;
import pe.andra.repository.AndraDocumentoPersonalRepositorio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraTextoPlano;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class AndraDocumentoServicio {

    public record AndraCriterio(String andraTexto, String andraTipo, String andraDependencia, String andraPublico, String andraRango, LocalDate andraDesde,
                           LocalDate andraHasta) {
        public static AndraCriterio andraVacio() {
            return new AndraCriterio("", "", "", "", "any", null, null);
        }

        public AndraCriterio andraConTipo(String andraNuevoTipo) {
            return new AndraCriterio(andraTexto, andraNuevoTipo, andraDependencia, andraPublico, andraRango, andraDesde, andraHasta);
        }
    }

    public record AndraRango(LocalDate andraDesde, LocalDate andraHasta) {
    }

    public static final List<String[]> ANDRA_RANGOS = List.of(new String[]{"any", "Cualquier fecha"},
            new String[]{"30d", "Últimos 30 días"}, new String[]{"6m", "Últimos 6 meses"},
            new String[]{"year", "Este año (2026)"}, new String[]{"2025", "Año 2025"}, new String[]{"2024", "Año 2024"},
            new String[]{"custom", "Rango personalizado…"});
    public static final List<String[]> ANDRA_ORDENES = List.of(new String[]{"rel", "Relevancia"},
            new String[]{"az", "Nombre (A–Z)"}, new String[]{"new", "Más recientes"}, new String[]{"old", "Más antiguos"},
            new String[]{"fav", "Favoritos primero"});
    public static final List<String[]> ANDRA_PREDEFINIDOS = List.of(new String[]{"fav", "Mis favoritos"},
            new String[]{"norm", "Normativa"}, new String[]{"form", "Formatos y solicitudes"},
            new String[]{"y26", "Publicados en 2026"});
    public static final List<String> ANDRA_PUBLICOS = List.of("Estudiantes", "Docentes", "Administrativos", "Público en General");
    public static final List<String> ANDRA_TIPOS_RAPIDOS = List.of("Malla curricular", "Sílabos", "Certificados", "Constancias",
            "Récord académico", "Reglamentos");

    private final AndraCatalogoServicio andraCatalogo;
    private final AndraFavoritoServicio andraFavoritos;
    private final AndraDocumentoPersonalRepositorio andraPersonales;

    public static AndraRango andraLimites(AndraCriterio andraCriterio, LocalDate andraHoy) {
        String andraAnio = String.valueOf(andraHoy.getYear());
        return switch (andraCriterio.andraRango() == null ? "any" : andraCriterio.andraRango()) {
            case "30d" -> new AndraRango(andraHoy.minusDays(30), andraHoy);
            case "6m" -> new AndraRango(andraHoy.minusDays(182), andraHoy);
            case "year" -> new AndraRango(LocalDate.parse(andraAnio + "-01-01"), LocalDate.parse(andraAnio + "-12-31"));
            case "2025" -> new AndraRango(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"));
            case "2024" -> new AndraRango(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-12-31"));
            case "custom" -> new AndraRango(andraCriterio.andraDesde() != null ? andraCriterio.andraDesde() : LocalDate.MIN, andraCriterio.andraHasta() != null ? andraCriterio.andraHasta() : LocalDate.MAX);
            default -> null;
        };
    }

    public static String andraValidarRangoPersonalizado(AndraCriterio andraCriterio) {
        if ("custom".equals(andraCriterio.andraRango()) && andraCriterio.andraDesde() != null && andraCriterio.andraHasta() != null && andraCriterio.andraDesde().isAfter(andraCriterio.andraHasta())) {
            return "La fecha final debe ser posterior a la inicial.";
        }
        return null;
    }

    public static List<AndraDocumento> andraFiltrar(List<AndraDocumento> andraTodos, AndraCriterio andraCriterio, AndraEscuelaProfesional andraEscuela, LocalDate andraHoy) {
        AndraRango andraRango = andraLimites(andraCriterio, andraHoy);
        return andraTodos.stream().filter(andraDocumento -> AndraTextoPlano.andraContiene(andraDocumento.getAndraNombre() + " " + andraDocumento.getAndraDescripcion() + " " + andraDocumento.getAndraTipo()
                        + " " + andraDocumento.getAndraDependencia(), andraCriterio.andraTexto()))
                .filter(andraDocumento -> andraCriterio.andraTipo() == null || andraCriterio.andraTipo().isBlank() || andraDocumento.getAndraTipo().equals(andraCriterio.andraTipo()))
                .filter(andraDocumento -> andraCriterio.andraDependencia() != null && !andraCriterio.andraDependencia().isBlank() ? andraDocumento.getAndraDependencia().equals(andraCriterio.andraDependencia())
                        : andraEscuela == null || andraDocumento.getAndraEscuela() == null || andraDocumento.getAndraEscuela().getAndraCodigo().equals(andraEscuela.getAndraCodigo()))
                .filter(andraDocumento -> andraCriterio.andraPublico() == null || andraCriterio.andraPublico().isBlank() || andraDocumento.getAndraPublico().equals(andraCriterio.andraPublico()))
                .filter(andraDocumento -> andraRango == null || (!andraDocumento.getAndraFecha().isBefore(andraRango.andraDesde()) && !andraDocumento.getAndraFecha().isAfter(andraRango.andraHasta())))
                .toList();
    }

    public Map<String, Predicate<AndraDocumento>> andraPredicados() {
        Set<String> andraFav = andraFavoritos.andraCodigos(AndraTipoFavorito.DOCUMENTO);
        return Map.of("fav", andraDocumento -> andraFav.contains(andraDocumento.getAndraCodigo()),
                "norm", andraDocumento -> List.of("Reglamentos", "Directivas", "Resoluciones").contains(andraDocumento.getAndraTipo()),
                "form", andraDocumento -> List.of("Formatos y plantillas", "Solicitudes y formularios").contains(andraDocumento.getAndraTipo()),
                "y26", andraDocumento -> andraDocumento.getAndraFecha().getYear() == 2026);
    }

    public static List<AndraDocumento> andraOrdenar(List<AndraDocumento> andraLista, String andraOrden, boolean andraConRango, Set<String> andraFav) {
        List<AndraDocumento> andraLista2 = new ArrayList<>(andraLista);
        Collator andraEspanol = Collator.getInstance(Locale.forLanguageTag("es"));
        switch (andraOrden) {
            case "az" -> andraLista2.sort(Comparator.comparing(AndraDocumento::getAndraNombre, andraEspanol));
            case "new" -> andraLista2.sort(Comparator.comparing(AndraDocumento::getAndraFecha).reversed());
            case "old" -> andraLista2.sort(Comparator.comparing(AndraDocumento::getAndraFecha));
            case "fav" -> andraLista2.sort(Comparator.comparing((AndraDocumento andraDocumento) -> andraFav.contains(andraDocumento.getAndraCodigo()) ? 0 : 1));
            default -> {
                if (andraConRango) {
                    andraLista2.sort(Comparator.comparing(AndraDocumento::getAndraFecha).reversed());
                }
            }
        }
        return andraLista2;
    }

    @Transactional(readOnly = true)
    public List<AndraDocumento> andraBuscar(AndraCriterio andraCriterio, AndraEscuelaProfesional andraEscuela, AndraEstadoFiltro andraFiltros, String andraOrden) {
        List<AndraDocumento> andraLista = andraFiltrar(andraCatalogo.andraDocumentos(), andraCriterio, andraEscuela, AndraFechas.andraHoy());
        andraLista = AndraFiltroServicio.andraAplicar(andraLista, andraFiltros, andraPredicados(),
                andraDocumento -> andraDocumento.getAndraNombre() + " " + andraDocumento.getAndraDescripcion() + " " + andraDocumento.getAndraTipo() + " " + andraDocumento.getAndraDependencia());
        return andraOrdenar(andraLista, andraOrden, andraLimites(andraCriterio, AndraFechas.andraHoy()) != null, andraFavoritos.andraCodigos(AndraTipoFavorito.DOCUMENTO));
    }

    @Transactional(readOnly = true)
    public List<AndraDocumentoPersonal> andraMisDocumentos() {
        return andraPersonales.findAllByOrderByAndraIdAsc();
    }

    @Transactional(readOnly = true)
    public AndraDocumentoPersonal andraMiDocumento(String andraCodigo) {
        return andraPersonales.findByAndraCodigo(andraCodigo).orElseThrow(() -> new AndraNegocioExcepcion("El documento no existe."));
    }

    public static String andraEtiquetaRango(String andraClave) {
        return ANDRA_RANGOS.stream().filter(andraTextos -> andraTextos[0].equals(andraClave)).map(andraTextos -> andraTextos[1]).findFirst().orElse("Cualquier fecha");
    }

    public static String andraEtiquetaOrden(String andraClave) {
        return ANDRA_ORDENES.stream().filter(andraTextos -> andraTextos[0].equals(andraClave)).map(andraTextos -> andraTextos[1]).findFirst().orElse("Relevancia");
    }
}
