package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.andra.model.DixonEspacio;
import pe.andra.util.DixonDiasHabiles;
import pe.andra.util.DixonFechas;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class DixonOcupacionServicio {

    public enum DixonEstadoOcupacion {
        LIBRE("Libre", "free"),
        POR_TERMINAR("Libre hasta", "soon"),
        OCUPADO("Ocupado", "busy");

        private final String dixonEtiqueta;
        private final String dixonClase;

        DixonEstadoOcupacion(String dixonEtiqueta, String dixonClase) {
            this.dixonEtiqueta = dixonEtiqueta;
            this.dixonClase = dixonClase;
        }

        public String dixonObtenerEtiqueta() {
            return dixonEtiqueta;
        }

        public String dixonObtenerClase() {
            return dixonClase;
        }
    }

    public record DixonFranjaBnps(int dixonIndice, int dixonDia, boolean dixonEnVivo) {
        public String dixonInicio() {
            return DixonCatalogoServicio.DIXON_FRANJAS.get(dixonIndice);
        }

        public String dixonFin() {
            return String.format("%02d:00", 8 + dixonIndice * 2 + 2);
        }
    }

    private final DixonReservaServicio dixonReservas;

    public static DixonFranjaBnps dixonFranjaPara(LocalDateTime dixonMomento) {
        double dixonAlto = dixonMomento.getHour() + dixonMomento.getMinute() / 60.0;
        if (dixonAlto >= 8 && dixonAlto < 18 && DixonDiasHabiles.dixonEsHabil(dixonMomento.toLocalDate())) {
            return new DixonFranjaBnps((int) Math.floor((dixonAlto - 8) / 2), 0, true);
        }
        return new DixonFranjaBnps(0, dixonAlto >= 18 ? 1 : 0, false);
    }

    public DixonFranjaBnps dixonFranjaActual() {
        return dixonFranjaPara(LocalDateTime.now(DixonFechas.dixonReloj()));
    }

    public DixonEstadoOcupacion dixonEstado(DixonEspacio dixonEspacio, int dixonIndice, int dixonDia) {
        if (dixonOcupado(dixonEspacio, dixonIndice, dixonDia)) {
            return DixonEstadoOcupacion.OCUPADO;
        }
        if (dixonIndice < DixonCatalogoServicio.DIXON_FRANJAS.size() - 1 && dixonOcupado(dixonEspacio, dixonIndice + 1, dixonDia)) {
            return DixonEstadoOcupacion.POR_TERMINAR;
        }
        return DixonEstadoOcupacion.LIBRE;
    }

    private boolean dixonOcupado(DixonEspacio dixonEspacio, int dixonIndice, int dixonDia) {
        String dixonFranja = DixonCatalogoServicio.DIXON_FRANJAS.get(dixonIndice);
        return dixonEspacio.isDixonFueraDeServicio() || !DixonReservaServicio.dixonDisponibleSimulado(dixonEspacio.getDixonNombre(), dixonDia, dixonFranja)
                || dixonReservas.dixonOcupadoPorReserva(dixonEspacio, DixonFechas.dixonHoy().plusDays(dixonDia), dixonFranja);
    }

    public Map<String, List<DixonEspacio>> dixonPorPabellon(List<DixonEspacio> dixonEspacios) {
        Map<String, List<DixonEspacio>> dixonGrupos = new LinkedHashMap<>();
        for (DixonEspacio dixonEspacio : dixonEspacios) {
            dixonGrupos.computeIfAbsent(dixonEspacio.dixonObtenerPabellon(), dixonTexto -> new ArrayList<>()).add(dixonEspacio);
        }
        return dixonGrupos;
    }

    public long dixonLibres(List<DixonEspacio> dixonEspacios, DixonFranjaBnps dixonFranja) {
        return dixonEspacios.stream().filter(dixonEspacio -> dixonEstado(dixonEspacio, dixonFranja.dixonIndice(), dixonFranja.dixonDia()) == DixonEstadoOcupacion.LIBRE).count();
    }

    public static String dixonNombreCorto(String dixonNombre) {
        return dixonNombre.replaceFirst("^(Laboratorio|Auditorio|Taller|Sala|Aula) (de |del )?", "");
    }
}
