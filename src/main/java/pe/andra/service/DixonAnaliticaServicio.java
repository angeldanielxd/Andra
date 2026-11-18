package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.*;
import pe.andra.repository.DixonExpedienteRepositorio;
import pe.andra.repository.DixonPagoRepositorio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonHash;
import pe.andra.util.DixonTextoPlano;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DixonAnaliticaServicio {

    public static final String[] DIXON_DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie"};
    public static final String[] DIXON_DIAS_LARGOS = {"lunes", "martes", "miércoles", "jueves", "viernes"};
    public static final String[] DIXON_RAMPA = {"#EEF5FD", "#CFE3FA", "#9CC6F4", "#5AA0EA", "#0071E3"};
    private static final double[] DIXON_PESO_DIA = {1.6, 1.1, 1, 1.2, 0.8};
    private static final double[] DIXON_PESO_FRANJA = {1.3, 1.7, 0.9, 1.1, 0.7};

    public record DixonBarra(String dixonEtiqueta, double dixonValor) {
    }

    public record DixonHallazgo(String dixonAntes, String dixonNegrita, String dixonDespues) {
    }

    public record DixonAnalisis(int dixonTramitesYOrdenes, BigDecimal dixonRecaudacion, int dixonUsoPromedio, List<DixonBarra> dixonSuperior,
                           List<DixonBarra> dixonPorSede, int[][] dixonCalor, int dixonCalorMaximo, int[] dixonPico, List<DixonBarra> dixonSubutilizados,
                           List<DixonBarra> dixonDemandados, List<DixonHallazgo> dixonHallazgos) {
    }

    private final DixonPagoRepositorio dixonPagos;
    private final DixonExpedienteRepositorio dixonExpedientes;
    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonContextoServicio dixonContexto;

    public static int dixonDias(String dixonPeriodo) {
        return "sem".equals(dixonPeriodo) ? 120 : Integer.parseInt(dixonPeriodo);
    }

    public static int[][] dixonMapaCalor(List<DixonPago> dixonEnPeriodo) {
        int[][] dixonCalor = new int[5][5];
        for (int dixonIndiceDia = 0; dixonIndiceDia < 5; dixonIndiceDia++) {
            for (int dixonIndiceFranja = 0; dixonIndiceFranja < 5; dixonIndiceFranja++) {
                int dixonDia = dixonIndiceDia + 1;
                int dixonDesde = 8 + dixonIndiceFranja * 2;
                long dixonReal = dixonEnPeriodo.stream().filter(dixonPago -> dixonPago.getDixonCreado().getDayOfWeek().getValue() == dixonDia
                        && dixonPago.getDixonCreado().getHour() >= dixonDesde && dixonPago.getDixonCreado().getHour() < dixonDesde + 2).count();
                dixonCalor[dixonIndiceDia][dixonIndiceFranja] = (int) Math.round(DIXON_PESO_DIA[dixonIndiceDia] * DIXON_PESO_FRANJA[dixonIndiceFranja] * 8
                        + DixonHash.dixonNumero(DIXON_DIAS[dixonIndiceDia] + DixonCatalogoServicio.DIXON_FRANJAS.get(dixonIndiceFranja)) % 5 + dixonReal * 3);
            }
        }
        return dixonCalor;
    }

    public static int[] dixonPico(int[][] dixonCalor) {
        int[] dixonNumeros = {0, 0};
        for (int dixonIndice = 0; dixonIndice < dixonCalor.length; dixonIndice++) {
            for (int dixonIndice2 = 0; dixonIndice2 < dixonCalor[dixonIndice].length; dixonIndice2++) {
                if (dixonCalor[dixonIndice][dixonIndice2] > dixonCalor[dixonNumeros[0]][dixonNumeros[1]]) {
                    dixonNumeros = new int[]{dixonIndice, dixonIndice2};
                }
            }
        }
        return dixonNumeros;
    }

    public static int dixonNivel(int dixonValor, int dixonMaximo) {
        return Math.min(4, (int) Math.floor(dixonValor / (double) dixonMaximo * 4.999));
    }

    public static int dixonUso(DixonEspacio dixonEspacio) {
        int dixonOcupadas = 0;
        for (int dixonDias = 0; dixonDias < 5; dixonDias++) {
            for (String dixonTexto : DixonCatalogoServicio.DIXON_FRANJAS) {
                if (dixonEspacio.isDixonFueraDeServicio() || !DixonReservaServicio.dixonDisponibleSimulado(dixonEspacio.getDixonNombre(), dixonDias, dixonTexto)) {
                    dixonOcupadas++;
                }
            }
        }
        return (int) Math.round(dixonOcupadas / 25.0 * 100);
    }

    @Transactional(readOnly = true)
    public DixonAnalisis dixonAnalizar(String dixonPeriodo) {
        LocalDate dixonDesde = DixonFechas.dixonHoy().minusDays(dixonDias(dixonPeriodo) - 1L);
        List<DixonPago> dixonTodos = dixonPagos.findAllByOrderByDixonCreadoDesc();
        List<DixonPago> dixonPagos2 = dixonTodos.stream().filter(dixonPago -> dixonContexto.dixonIncluyeSede(dixonPago.getDixonSede()) && !dixonPago.getDixonCreado().toLocalDate().isBefore(dixonDesde)).toList();
        List<DixonPago> dixonPagados = dixonPagos2.stream().filter(DixonPago::dixonEsPagado).toList();
        Map<String, Integer> dixonPorConcepto = new LinkedHashMap<>();
        dixonPagos2.stream().filter(dixonPago -> dixonPago.getDixonTipo() != DixonTipoPago.MATRICULA).forEach(dixonPago -> dixonPorConcepto.merge(dixonPago.getDixonConcepto(), 1, Integer::sum));
        dixonExpedientes.findAllByOrderByDixonIdDesc().stream().filter(dixonExpediente -> dixonContexto.dixonIncluyeSede(dixonExpediente.getDixonSede()) && dixonExpediente.getDixonPago() == null)
                .forEach(dixonExpediente -> dixonPorConcepto.merge(DixonTextoPlano.dixonRecortar(dixonExpediente.getDixonTitulo(), 40), 1, Integer::sum));
        List<Map.Entry<String, Integer>> dixonOrden = new ArrayList<>(dixonPorConcepto.entrySet());
        dixonOrden.sort((dixonEntradaA, dixonEntradaB) -> dixonEntradaB.getValue() - dixonEntradaA.getValue());
        List<DixonBarra> dixonSuperior = dixonOrden.stream().limit(6).map(dixonEntrada -> new DixonBarra(dixonEntrada.getKey(), dixonEntrada.getValue())).toList();
        int dixonTotalConceptos = Math.max(1, dixonPorConcepto.values().stream().mapToInt(Integer::intValue).sum());

        int[][] dixonCalor = dixonMapaCalor(dixonPagos2);
        int dixonMaximo = Arrays.stream(dixonCalor).flatMapToInt(Arrays::stream).max().orElse(1);
        int[] dixonPico = dixonPico(dixonCalor);

        List<DixonEspacio> dixonEnContexto = dixonCatalogo.dixonEspacios().stream().filter(dixonEspacio -> dixonContexto.dixonIncluye(dixonEspacio.getDixonSede(), dixonEspacio.getDixonEscuela())).toList();
        List<DixonBarra> dixonUsos = new ArrayList<>(dixonEnContexto.stream().map(dixonEspacio -> new DixonBarra(dixonEspacio.getDixonNombre(), dixonUso(dixonEspacio))).toList());
        dixonUsos.sort(Comparator.comparingDouble(DixonBarra::dixonValor));
        int dixonPromedio = (int) Math.round(dixonUsos.stream().mapToDouble(DixonBarra::dixonValor).average().orElse(0));
        List<DixonBarra> dixonDemandados = new ArrayList<>(dixonUsos.subList(Math.max(0, dixonUsos.size() - 3), dixonUsos.size()));
        Collections.reverse(dixonDemandados);

        List<DixonBarra> dixonPorSede = dixonCatalogo.dixonSedes().stream().map(dixonSede -> new DixonBarra(dixonSede.getDixonNombre(), DixonPagoServicio.dixonTotal(dixonTodos.stream()
                .filter(dixonPago -> dixonPago.dixonEsPagado() && dixonPago.getDixonPagado() != null && dixonPago.getDixonSede().getDixonId().equals(dixonSede.getDixonId())
                        && !dixonPago.getDixonPagado().toLocalDate().isBefore(dixonDesde)).toList()).doubleValue())).toList();

        List<DixonHallazgo> dixonHallazgos = new ArrayList<>();
        if (!dixonSuperior.isEmpty()) {
            dixonHallazgos.add(new DixonHallazgo("", dixonSuperior.get(0).dixonEtiqueta(), " es el trámite más pedido ("
                    + Math.round(dixonSuperior.get(0).dixonValor() / dixonTotalConceptos * 100) + " % del periodo). Conviene emitirlo de forma automática."));
        }
        String dixonFranja = DixonCatalogoServicio.DIXON_FRANJAS.get(dixonPico[1]);
        dixonHallazgos.add(new DixonHallazgo("Las ventanillas se saturan los ", DIXON_DIAS_LARGOS[dixonPico[0]] + " de " + dixonFranja + " a "
                + String.format("%02d:00", Integer.parseInt(dixonFranja.substring(0, 2)) + 2),
                ". Sugerimos abrir una ventanilla adicional en esa franja."));
        if (!dixonUsos.isEmpty()) {
            dixonHallazgos.add(new DixonHallazgo("", dixonUsos.get(0).dixonEtiqueta(), " se usa solo el " + (int) dixonUsos.get(0).dixonValor()
                    + " % del tiempo. Podría abrirse a otras escuelas o a talleres."));
        }
        dixonHallazgos.add(new DixonHallazgo(Math.round(dixonPagados.size() / (double) Math.max(1, dixonPagos2.size()) * 100)
                + " % de las órdenes del periodo ya están pagadas con Andra Pay, sin colas en el banco.", "", ""));
        return new DixonAnalisis(dixonPagos2.size(), DixonPagoServicio.dixonTotal(dixonPagados), dixonPromedio, dixonSuperior, dixonPorSede, dixonCalor, dixonMaximo, dixonPico,
                dixonUsos.subList(0, Math.min(4, dixonUsos.size())), dixonDemandados, dixonHallazgos);
    }
}
