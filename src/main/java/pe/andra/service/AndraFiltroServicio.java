package pe.andra.service;

import pe.andra.util.AndraFechas;
import pe.andra.util.AndraTextoPlano;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public final class AndraFiltroServicio {

    public record AndraAccesores<T>(Function<T, LocalDate> andraFecha, Function<T, String> andraCodigo, Map<String, Function<T, String>> andraCampos) {
    }

    private AndraFiltroServicio() {
    }

    public static <T> List<T> andraAplicar(List<T> andraLista, AndraEstadoFiltro andraEstado, Map<String, Predicate<T>> andraPredicados,
                                      Function<T, String> andraTextoDe) {
        List<T> andraLista2 = andraLista;
        for (String andraClave : andraEstado.andraObtenerActivos()) {
            Predicate<T> andraCondicion = andraPredicados.get(andraClave);
            if (andraCondicion != null) {
                andraLista2 = andraLista2.stream().filter(andraCondicion).toList();
            }
        }
        if (!andraEstado.andraObtenerTexto().isBlank()) {
            andraLista2 = andraLista2.stream().filter(andraPosicionX -> AndraTextoPlano.andraContiene(andraTextoDe.apply(andraPosicionX), andraEstado.andraObtenerTexto())).toList();
        }
        return andraLista2;
    }

    public static <T> List<T> andraAplicarAvanzado(List<T> andraLista, AndraEstadoFiltro andraEstado, AndraAccesores<T> andraAcc) {
        List<T> andraLista2 = andraLista;
        String andraDesde = andraEstado.andraAvanzado("from");
        String andraHasta = andraEstado.andraAvanzado("to");
        if (andraDesde != null) {
            LocalDate andraFecha = LocalDate.parse(andraDesde);
            andraLista2 = andraLista2.stream().filter(andraPosicionX -> {
                LocalDate andraFecha2 = andraAcc.andraFecha().apply(andraPosicionX);
                return andraFecha2 != null && !andraFecha2.isBefore(andraFecha);
            }).toList();
        }
        if (andraHasta != null) {
            LocalDate andraAlto = LocalDate.parse(andraHasta);
            andraLista2 = andraLista2.stream().filter(andraPosicionX -> {
                LocalDate andraFecha3 = andraAcc.andraFecha().apply(andraPosicionX);
                return andraFecha3 != null && !andraFecha3.isAfter(andraAlto);
            }).toList();
        }
        String andraCodigo = andraEstado.andraAvanzado("code");
        if (andraCodigo != null) {
            andraLista2 = andraLista2.stream().filter(andraPosicionX -> AndraTextoPlano.andraContiene(andraAcc.andraCodigo().apply(andraPosicionX), andraCodigo)).toList();
        }
        for (Map.Entry<String, Function<T, String>> andraCampo : andraAcc.andraCampos().entrySet()) {
            String andraValor = andraEstado.andraAvanzado(andraCampo.getKey());
            if (andraValor != null) {
                andraLista2 = andraLista2.stream().filter(andraPosicionX -> andraValor.equals(andraCampo.getValue().apply(andraPosicionX))).toList();
            }
        }
        return andraLista2;
    }

    public static <T> Map<String, Predicate<T>> andraPredicadosFecha(Function<T, LocalDate> andraFecha) {
        LocalDate andraHoy = AndraFechas.andraHoy();
        Map<String, Predicate<T>> andraMapa = new HashMap<>();
        andraMapa.put("today", andraPosicionX -> andraHoy.equals(andraFecha.apply(andraPosicionX)));
        andraMapa.put("d7", andraPosicionX -> {
            LocalDate andraFecha2 = andraFecha.apply(andraPosicionX);
            return andraFecha2 != null && !andraFecha2.isBefore(andraHoy.minusDays(6));
        });
        andraMapa.put("month", andraPosicionX -> {
            LocalDate andraFecha3 = andraFecha.apply(andraPosicionX);
            return andraFecha3 != null && andraFecha3.getYear() == andraHoy.getYear() && andraFecha3.getMonth() == andraHoy.getMonth();
        });
        return andraMapa;
    }

    public static String andraValidarRango(String andraDesde, String andraHasta) {
        if (andraDesde != null && !andraDesde.isBlank() && andraHasta != null && !andraHasta.isBlank()
                && LocalDate.parse(andraDesde).isAfter(LocalDate.parse(andraHasta))) {
            return "La fecha “Hasta” debe ser posterior a “Desde”.";
        }
        return null;
    }
}
