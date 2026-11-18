package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.DixonDocumento;
import pe.andra.model.DixonDocumentoPersonal;
import pe.andra.model.DixonEscuelaProfesional;
import pe.andra.model.DixonTipoFavorito;
import pe.andra.repository.DixonDocumentoPersonalRepositorio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonTextoPlano;

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
public class DixonDocumentoServicio {

    public record DixonCriterio(String dixonTexto, String dixonTipo, String dixonDependencia, String dixonPublico, String dixonRango, LocalDate dixonDesde,
                           LocalDate dixonHasta) {
        public static DixonCriterio dixonVacio() {
            return new DixonCriterio("", "", "", "", "any", null, null);
        }

        public DixonCriterio dixonConTipo(String dixonNuevoTipo) {
            return new DixonCriterio(dixonTexto, dixonNuevoTipo, dixonDependencia, dixonPublico, dixonRango, dixonDesde, dixonHasta);
        }
    }

    public record DixonRango(LocalDate dixonDesde, LocalDate dixonHasta) {
    }

    public static final List<String[]> DIXON_RANGOS = List.of(new String[]{"any", "Cualquier fecha"},
            new String[]{"30d", "Últimos 30 días"}, new String[]{"6m", "Últimos 6 meses"},
            new String[]{"year", "Este año (2026)"}, new String[]{"2025", "Año 2025"}, new String[]{"2024", "Año 2024"},
            new String[]{"custom", "Rango personalizado…"});
    public static final List<String[]> DIXON_ORDENES = List.of(new String[]{"rel", "Relevancia"},
            new String[]{"az", "Nombre (A–Z)"}, new String[]{"new", "Más recientes"}, new String[]{"old", "Más antiguos"},
            new String[]{"fav", "Favoritos primero"});
    public static final List<String[]> DIXON_PREDEFINIDOS = List.of(new String[]{"fav", "Mis favoritos"},
            new String[]{"norm", "Normativa"}, new String[]{"form", "Formatos y solicitudes"},
            new String[]{"y26", "Publicados en 2026"});
    public static final List<String> DIXON_PUBLICOS = List.of("Estudiantes", "Docentes", "Administrativos", "Público en General");
    public static final List<String> DIXON_TIPOS_RAPIDOS = List.of("Malla curricular", "Sílabos", "Certificados", "Constancias",
            "Récord académico", "Reglamentos");

    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonFavoritoServicio dixonFavoritos;
    private final DixonDocumentoPersonalRepositorio dixonPersonales;

    public static DixonRango dixonLimites(DixonCriterio dixonCriterio, LocalDate dixonHoy) {
        String dixonAnio = String.valueOf(dixonHoy.getYear());
        return switch (dixonCriterio.dixonRango() == null ? "any" : dixonCriterio.dixonRango()) {
            case "30d" -> new DixonRango(dixonHoy.minusDays(30), dixonHoy);
            case "6m" -> new DixonRango(dixonHoy.minusDays(182), dixonHoy);
            case "year" -> new DixonRango(LocalDate.parse(dixonAnio + "-01-01"), LocalDate.parse(dixonAnio + "-12-31"));
            case "2025" -> new DixonRango(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"));
            case "2024" -> new DixonRango(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-12-31"));
            case "custom" -> new DixonRango(dixonCriterio.dixonDesde() != null ? dixonCriterio.dixonDesde() : LocalDate.MIN, dixonCriterio.dixonHasta() != null ? dixonCriterio.dixonHasta() : LocalDate.MAX);
            default -> null;
        };
    }

    public static String dixonValidarRangoPersonalizado(DixonCriterio dixonCriterio) {
        if ("custom".equals(dixonCriterio.dixonRango()) && dixonCriterio.dixonDesde() != null && dixonCriterio.dixonHasta() != null && dixonCriterio.dixonDesde().isAfter(dixonCriterio.dixonHasta())) {
            return "La fecha final debe ser posterior a la inicial.";
        }
        return null;
    }

    public static List<DixonDocumento> dixonFiltrar(List<DixonDocumento> dixonTodos, DixonCriterio dixonCriterio, DixonEscuelaProfesional dixonEscuela, LocalDate dixonHoy) {
        DixonRango dixonRango = dixonLimites(dixonCriterio, dixonHoy);
        return dixonTodos.stream().filter(dixonDocumento -> DixonTextoPlano.dixonContiene(dixonDocumento.getDixonNombre() + " " + dixonDocumento.getDixonDescripcion() + " " + dixonDocumento.getDixonTipo()
                        + " " + dixonDocumento.getDixonDependencia(), dixonCriterio.dixonTexto()))
                .filter(dixonDocumento -> dixonCriterio.dixonTipo() == null || dixonCriterio.dixonTipo().isBlank() || dixonDocumento.getDixonTipo().equals(dixonCriterio.dixonTipo()))
                .filter(dixonDocumento -> dixonCriterio.dixonDependencia() != null && !dixonCriterio.dixonDependencia().isBlank() ? dixonDocumento.getDixonDependencia().equals(dixonCriterio.dixonDependencia())
                        : dixonEscuela == null || dixonDocumento.getDixonEscuela() == null || dixonDocumento.getDixonEscuela().getDixonCodigo().equals(dixonEscuela.getDixonCodigo()))
                .filter(dixonDocumento -> dixonCriterio.dixonPublico() == null || dixonCriterio.dixonPublico().isBlank() || dixonDocumento.getDixonPublico().equals(dixonCriterio.dixonPublico()))
                .filter(dixonDocumento -> dixonRango == null || (!dixonDocumento.getDixonFecha().isBefore(dixonRango.dixonDesde()) && !dixonDocumento.getDixonFecha().isAfter(dixonRango.dixonHasta())))
                .toList();
    }

    public Map<String, Predicate<DixonDocumento>> dixonPredicados() {
        Set<String> dixonFav = dixonFavoritos.dixonCodigos(DixonTipoFavorito.DOCUMENTO);
        return Map.of("fav", dixonDocumento -> dixonFav.contains(dixonDocumento.getDixonCodigo()),
                "norm", dixonDocumento -> List.of("Reglamentos", "Directivas", "Resoluciones").contains(dixonDocumento.getDixonTipo()),
                "form", dixonDocumento -> List.of("Formatos y plantillas", "Solicitudes y formularios").contains(dixonDocumento.getDixonTipo()),
                "y26", dixonDocumento -> dixonDocumento.getDixonFecha().getYear() == 2026);
    }

    public static List<DixonDocumento> dixonOrdenar(List<DixonDocumento> dixonLista, String dixonOrden, boolean dixonConRango, Set<String> dixonFav) {
        List<DixonDocumento> dixonLista2 = new ArrayList<>(dixonLista);
        Collator dixonEspanol = Collator.getInstance(Locale.forLanguageTag("es"));
        switch (dixonOrden) {
            case "az" -> dixonLista2.sort(Comparator.comparing(DixonDocumento::getDixonNombre, dixonEspanol));
            case "new" -> dixonLista2.sort(Comparator.comparing(DixonDocumento::getDixonFecha).reversed());
            case "old" -> dixonLista2.sort(Comparator.comparing(DixonDocumento::getDixonFecha));
            case "fav" -> dixonLista2.sort(Comparator.comparing((DixonDocumento dixonDocumento) -> dixonFav.contains(dixonDocumento.getDixonCodigo()) ? 0 : 1));
            default -> {
                if (dixonConRango) {
                    dixonLista2.sort(Comparator.comparing(DixonDocumento::getDixonFecha).reversed());
                }
            }
        }
        return dixonLista2;
    }

    @Transactional(readOnly = true)
    public List<DixonDocumento> dixonBuscar(DixonCriterio dixonCriterio, DixonEscuelaProfesional dixonEscuela, DixonEstadoFiltro dixonFiltros, String dixonOrden) {
        List<DixonDocumento> dixonLista = dixonFiltrar(dixonCatalogo.dixonDocumentos(), dixonCriterio, dixonEscuela, DixonFechas.dixonHoy());
        dixonLista = DixonFiltroServicio.dixonAplicar(dixonLista, dixonFiltros, dixonPredicados(),
                dixonDocumento -> dixonDocumento.getDixonNombre() + " " + dixonDocumento.getDixonDescripcion() + " " + dixonDocumento.getDixonTipo() + " " + dixonDocumento.getDixonDependencia());
        return dixonOrdenar(dixonLista, dixonOrden, dixonLimites(dixonCriterio, DixonFechas.dixonHoy()) != null, dixonFavoritos.dixonCodigos(DixonTipoFavorito.DOCUMENTO));
    }

    @Transactional(readOnly = true)
    public List<DixonDocumentoPersonal> dixonMisDocumentos() {
        return dixonPersonales.findAllByOrderByDixonIdAsc();
    }

    @Transactional(readOnly = true)
    public DixonDocumentoPersonal dixonMiDocumento(String dixonCodigo) {
        return dixonPersonales.findByDixonCodigo(dixonCodigo).orElseThrow(() -> new DixonNegocioExcepcion("El documento no existe."));
    }

    public static String dixonEtiquetaRango(String dixonClave) {
        return DIXON_RANGOS.stream().filter(dixonTextos -> dixonTextos[0].equals(dixonClave)).map(dixonTextos -> dixonTextos[1]).findFirst().orElse("Cualquier fecha");
    }

    public static String dixonEtiquetaOrden(String dixonClave) {
        return DIXON_ORDENES.stream().filter(dixonTextos -> dixonTextos[0].equals(dixonClave)).map(dixonTextos -> dixonTextos[1]).findFirst().orElse("Relevancia");
    }
}
