package pe.andra.service;

import pe.andra.util.DixonFechas;
import pe.andra.util.DixonTextoPlano;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public final class DixonFiltroServicio {

    public record DixonAccesores<T>(Function<T, LocalDate> dixonFecha, Function<T, String> dixonCodigo, Map<String, Function<T, String>> dixonCampos) {
    }

    private DixonFiltroServicio() {
    }

    public static <T> List<T> dixonAplicar(List<T> dixonLista, DixonEstadoFiltro dixonEstado, Map<String, Predicate<T>> dixonPredicados,
                                      Function<T, String> dixonTextoDe) {
        List<T> dixonLista2 = dixonLista;
        for (String dixonClave : dixonEstado.dixonObtenerActivos()) {
            Predicate<T> dixonCondicion = dixonPredicados.get(dixonClave);
            if (dixonCondicion != null) {
                dixonLista2 = dixonLista2.stream().filter(dixonCondicion).toList();
            }
        }
        if (!dixonEstado.dixonObtenerTexto().isBlank()) {
            dixonLista2 = dixonLista2.stream().filter(dixonPosicionX -> DixonTextoPlano.dixonContiene(dixonTextoDe.apply(dixonPosicionX), dixonEstado.dixonObtenerTexto())).toList();
        }
        return dixonLista2;
    }

    public static <T> List<T> dixonAplicarAvanzado(List<T> dixonLista, DixonEstadoFiltro dixonEstado, DixonAccesores<T> dixonAcc) {
        List<T> dixonLista2 = dixonLista;
        String dixonDesde = dixonEstado.dixonAvanzado("from");
        String dixonHasta = dixonEstado.dixonAvanzado("to");
        if (dixonDesde != null) {
            LocalDate dixonFecha = LocalDate.parse(dixonDesde);
            dixonLista2 = dixonLista2.stream().filter(dixonPosicionX -> {
                LocalDate dixonFecha2 = dixonAcc.dixonFecha().apply(dixonPosicionX);
                return dixonFecha2 != null && !dixonFecha2.isBefore(dixonFecha);
            }).toList();
        }
        if (dixonHasta != null) {
            LocalDate dixonAlto = LocalDate.parse(dixonHasta);
            dixonLista2 = dixonLista2.stream().filter(dixonPosicionX -> {
                LocalDate dixonFecha3 = dixonAcc.dixonFecha().apply(dixonPosicionX);
                return dixonFecha3 != null && !dixonFecha3.isAfter(dixonAlto);
            }).toList();
        }
        String dixonCodigo = dixonEstado.dixonAvanzado("code");
        if (dixonCodigo != null) {
            dixonLista2 = dixonLista2.stream().filter(dixonPosicionX -> DixonTextoPlano.dixonContiene(dixonAcc.dixonCodigo().apply(dixonPosicionX), dixonCodigo)).toList();
        }
        for (Map.Entry<String, Function<T, String>> dixonCampo : dixonAcc.dixonCampos().entrySet()) {
            String dixonValor = dixonEstado.dixonAvanzado(dixonCampo.getKey());
            if (dixonValor != null) {
                dixonLista2 = dixonLista2.stream().filter(dixonPosicionX -> dixonValor.equals(dixonCampo.getValue().apply(dixonPosicionX))).toList();
            }
        }
        return dixonLista2;
    }

    public static <T> Map<String, Predicate<T>> dixonPredicadosFecha(Function<T, LocalDate> dixonFecha) {
        LocalDate dixonHoy = DixonFechas.dixonHoy();
        Map<String, Predicate<T>> dixonMapa = new HashMap<>();
        dixonMapa.put("today", dixonPosicionX -> dixonHoy.equals(dixonFecha.apply(dixonPosicionX)));
        dixonMapa.put("d7", dixonPosicionX -> {
            LocalDate dixonFecha2 = dixonFecha.apply(dixonPosicionX);
            return dixonFecha2 != null && !dixonFecha2.isBefore(dixonHoy.minusDays(6));
        });
        dixonMapa.put("month", dixonPosicionX -> {
            LocalDate dixonFecha3 = dixonFecha.apply(dixonPosicionX);
            return dixonFecha3 != null && dixonFecha3.getYear() == dixonHoy.getYear() && dixonFecha3.getMonth() == dixonHoy.getMonth();
        });
        return dixonMapa;
    }

    public static String dixonValidarRango(String dixonDesde, String dixonHasta) {
        if (dixonDesde != null && !dixonDesde.isBlank() && dixonHasta != null && !dixonHasta.isBlank()
                && LocalDate.parse(dixonDesde).isAfter(LocalDate.parse(dixonHasta))) {
            return "La fecha “Hasta” debe ser posterior a “Desde”.";
        }
        return null;
    }
}
