package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.*;
import pe.andra.repository.AndraExpedienteRepositorio;
import pe.andra.repository.AndraPagoRepositorio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraHash;
import pe.andra.util.AndraTextoPlano;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AndraAnaliticaServicio {

    public static final String[] ANDRA_DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie"};
    public static final String[] ANDRA_DIAS_LARGOS = {"lunes", "martes", "miércoles", "jueves", "viernes"};
    public static final String[] ANDRA_RAMPA = {"#EEF5FD", "#CFE3FA", "#9CC6F4", "#5AA0EA", "#0071E3"};
    private static final double[] ANDRA_PESO_DIA = {1.6, 1.1, 1, 1.2, 0.8};
    private static final double[] ANDRA_PESO_FRANJA = {1.3, 1.7, 0.9, 1.1, 0.7};

    public record AndraBarra(String andraEtiqueta, double andraValor) {
    }

    public record AndraHallazgo(String andraAntes, String andraNegrita, String andraDespues) {
    }

    public record AndraAnalisis(int andraTramitesYOrdenes, BigDecimal andraRecaudacion, int andraUsoPromedio, List<AndraBarra> andraSuperior,
                           List<AndraBarra> andraPorSede, int[][] andraCalor, int andraCalorMaximo, int[] andraPico, List<AndraBarra> andraSubutilizados,
                           List<AndraBarra> andraDemandados, List<AndraHallazgo> andraHallazgos) {
    }

    private final AndraPagoRepositorio andraPagos;
    private final AndraExpedienteRepositorio andraExpedientes;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraContextoServicio andraContexto;

    public static int andraDias(String andraPeriodo) {
        return "sem".equals(andraPeriodo) ? 120 : Integer.parseInt(andraPeriodo);
    }

    public static int[][] andraMapaCalor(List<AndraPago> andraEnPeriodo) {
        int[][] andraCalor = new int[5][5];
        for (int andraIndiceDia = 0; andraIndiceDia < 5; andraIndiceDia++) {
            for (int andraIndiceFranja = 0; andraIndiceFranja < 5; andraIndiceFranja++) {
                int andraDia = andraIndiceDia + 1;
                int andraDesde = 8 + andraIndiceFranja * 2;
                long andraReal = andraEnPeriodo.stream().filter(andraPago -> andraPago.getAndraCreado().getDayOfWeek().getValue() == andraDia
                        && andraPago.getAndraCreado().getHour() >= andraDesde && andraPago.getAndraCreado().getHour() < andraDesde + 2).count();
                andraCalor[andraIndiceDia][andraIndiceFranja] = (int) Math.round(ANDRA_PESO_DIA[andraIndiceDia] * ANDRA_PESO_FRANJA[andraIndiceFranja] * 8
                        + AndraHash.andraNumero(ANDRA_DIAS[andraIndiceDia] + AndraCatalogoServicio.ANDRA_FRANJAS.get(andraIndiceFranja)) % 5 + andraReal * 3);
            }
        }
        return andraCalor;
    }

    public static int[] andraPico(int[][] andraCalor) {
        int[] andraNumeros = {0, 0};
        for (int andraIndice = 0; andraIndice < andraCalor.length; andraIndice++) {
            for (int andraIndice2 = 0; andraIndice2 < andraCalor[andraIndice].length; andraIndice2++) {
                if (andraCalor[andraIndice][andraIndice2] > andraCalor[andraNumeros[0]][andraNumeros[1]]) {
                    andraNumeros = new int[]{andraIndice, andraIndice2};
                }
            }
        }
        return andraNumeros;
    }

    public static int andraNivel(int andraValor, int andraMaximo) {
        return Math.min(4, (int) Math.floor(andraValor / (double) andraMaximo * 4.999));
    }

    public static int andraUso(AndraEspacio andraEspacio) {
        int andraOcupadas = 0;
        for (int andraDias = 0; andraDias < 5; andraDias++) {
            for (String andraTexto : AndraCatalogoServicio.ANDRA_FRANJAS) {
                if (andraEspacio.isAndraFueraDeServicio() || !AndraReservaServicio.andraDisponibleSimulado(andraEspacio.getAndraNombre(), andraDias, andraTexto)) {
                    andraOcupadas++;
                }
            }
        }
        return (int) Math.round(andraOcupadas / 25.0 * 100);
    }

    @Transactional(readOnly = true)
    public AndraAnalisis andraAnalizar(String andraPeriodo) {
        LocalDate andraDesde = AndraFechas.andraHoy().minusDays(andraDias(andraPeriodo) - 1L);
        List<AndraPago> andraTodos = andraPagos.findAllByOrderByAndraCreadoDesc();
        List<AndraPago> andraPagos2 = andraTodos.stream().filter(andraPago -> andraContexto.andraIncluyeSede(andraPago.getAndraSede()) && !andraPago.getAndraCreado().toLocalDate().isBefore(andraDesde)).toList();
        List<AndraPago> andraPagados = andraPagos2.stream().filter(AndraPago::andraEsPagado).toList();
        Map<String, Integer> andraPorConcepto = new LinkedHashMap<>();
        andraPagos2.stream().filter(andraPago -> andraPago.getAndraTipo() != AndraTipoPago.MATRICULA).forEach(andraPago -> andraPorConcepto.merge(andraPago.getAndraConcepto(), 1, Integer::sum));
        andraExpedientes.findAllByOrderByAndraIdDesc().stream().filter(andraExpediente -> andraContexto.andraIncluyeSede(andraExpediente.getAndraSede()) && andraExpediente.getAndraPago() == null)
                .forEach(andraExpediente -> andraPorConcepto.merge(AndraTextoPlano.andraRecortar(andraExpediente.getAndraTitulo(), 40), 1, Integer::sum));
        List<Map.Entry<String, Integer>> andraOrden = new ArrayList<>(andraPorConcepto.entrySet());
        andraOrden.sort((andraEntradaA, andraEntradaB) -> andraEntradaB.getValue() - andraEntradaA.getValue());
        List<AndraBarra> andraSuperior = andraOrden.stream().limit(6).map(andraEntrada -> new AndraBarra(andraEntrada.getKey(), andraEntrada.getValue())).toList();
        int andraTotalConceptos = Math.max(1, andraPorConcepto.values().stream().mapToInt(Integer::intValue).sum());

        int[][] andraCalor = andraMapaCalor(andraPagos2);
        int andraMaximo = Arrays.stream(andraCalor).flatMapToInt(Arrays::stream).max().orElse(1);
        int[] andraPico = andraPico(andraCalor);

        List<AndraEspacio> andraEnContexto = andraCatalogo.andraEspacios().stream().filter(andraEspacio -> andraContexto.andraIncluye(andraEspacio.getAndraSede(), andraEspacio.getAndraEscuela())).toList();
        List<AndraBarra> andraUsos = new ArrayList<>(andraEnContexto.stream().map(andraEspacio -> new AndraBarra(andraEspacio.getAndraNombre(), andraUso(andraEspacio))).toList());
        andraUsos.sort(Comparator.comparingDouble(AndraBarra::andraValor));
        int andraPromedio = (int) Math.round(andraUsos.stream().mapToDouble(AndraBarra::andraValor).average().orElse(0));
        List<AndraBarra> andraDemandados = new ArrayList<>(andraUsos.subList(Math.max(0, andraUsos.size() - 3), andraUsos.size()));
        Collections.reverse(andraDemandados);

        List<AndraBarra> andraPorSede = andraCatalogo.andraSedes().stream().map(andraSede -> new AndraBarra(andraSede.getAndraNombre(), AndraPagoServicio.andraTotal(andraTodos.stream()
                .filter(andraPago -> andraPago.andraEsPagado() && andraPago.getAndraPagado() != null && andraPago.getAndraSede().getAndraId().equals(andraSede.getAndraId())
                        && !andraPago.getAndraPagado().toLocalDate().isBefore(andraDesde)).toList()).doubleValue())).toList();

        List<AndraHallazgo> andraHallazgos = new ArrayList<>();
        if (!andraSuperior.isEmpty()) {
            andraHallazgos.add(new AndraHallazgo("", andraSuperior.get(0).andraEtiqueta(), " es el trámite más pedido ("
                    + Math.round(andraSuperior.get(0).andraValor() / andraTotalConceptos * 100) + " % del periodo). Conviene emitirlo de forma automática."));
        }
        String andraFranja = AndraCatalogoServicio.ANDRA_FRANJAS.get(andraPico[1]);
        andraHallazgos.add(new AndraHallazgo("Las ventanillas se saturan los ", ANDRA_DIAS_LARGOS[andraPico[0]] + " de " + andraFranja + " a "
                + String.format("%02d:00", Integer.parseInt(andraFranja.substring(0, 2)) + 2),
                ". Sugerimos abrir una ventanilla adicional en esa franja."));
        if (!andraUsos.isEmpty()) {
            andraHallazgos.add(new AndraHallazgo("", andraUsos.get(0).andraEtiqueta(), " se usa solo el " + (int) andraUsos.get(0).andraValor()
                    + " % del tiempo. Podría abrirse a otras escuelas o a talleres."));
        }
        andraHallazgos.add(new AndraHallazgo(Math.round(andraPagados.size() / (double) Math.max(1, andraPagos2.size()) * 100)
                + " % de las órdenes del periodo ya están pagadas con Andra Pay, sin colas en el banco.", "", ""));
        return new AndraAnalisis(andraPagos2.size(), AndraPagoServicio.andraTotal(andraPagados), andraPromedio, andraSuperior, andraPorSede, andraCalor, andraMaximo, andraPico,
                andraUsos.subList(0, Math.min(4, andraUsos.size())), andraDemandados, andraHallazgos);
    }
}
