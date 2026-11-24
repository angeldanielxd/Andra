package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.andra.model.AndraEspacio;
import pe.andra.util.AndraDiasHabiles;
import pe.andra.util.AndraFechas;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AndraOcupacionServicio {

    public enum AndraEstadoOcupacion {
        LIBRE("Libre", "free"),
        POR_TERMINAR("Libre hasta", "soon"),
        OCUPADO("Ocupado", "busy");

        private final String andraEtiqueta;
        private final String andraClase;

        AndraEstadoOcupacion(String andraEtiqueta, String andraClase) {
            this.andraEtiqueta = andraEtiqueta;
            this.andraClase = andraClase;
        }

        public String andraObtenerEtiqueta() {
            return andraEtiqueta;
        }

        public String andraObtenerClase() {
            return andraClase;
        }
    }

    public record AndraFranjaBnps(int andraIndice, int andraDia, boolean andraEnVivo) {
        public String andraInicio() {
            return AndraCatalogoServicio.ANDRA_FRANJAS.get(andraIndice);
        }

        public String andraFin() {
            return String.format("%02d:00", 8 + andraIndice * 2 + 2);
        }
    }

    private final AndraReservaServicio andraReservas;

    public static AndraFranjaBnps andraFranjaPara(LocalDateTime andraMomento) {
        double andraAlto = andraMomento.getHour() + andraMomento.getMinute() / 60.0;
        if (andraAlto >= 8 && andraAlto < 18 && AndraDiasHabiles.andraEsHabil(andraMomento.toLocalDate())) {
            return new AndraFranjaBnps((int) Math.floor((andraAlto - 8) / 2), 0, true);
        }
        return new AndraFranjaBnps(0, andraAlto >= 18 ? 1 : 0, false);
    }

    public AndraFranjaBnps andraFranjaActual() {
        return andraFranjaPara(LocalDateTime.now(AndraFechas.andraReloj()));
    }

    public AndraEstadoOcupacion andraEstado(AndraEspacio andraEspacio, int andraIndice, int andraDia) {
        if (andraOcupado(andraEspacio, andraIndice, andraDia)) {
            return AndraEstadoOcupacion.OCUPADO;
        }
        if (andraIndice < AndraCatalogoServicio.ANDRA_FRANJAS.size() - 1 && andraOcupado(andraEspacio, andraIndice + 1, andraDia)) {
            return AndraEstadoOcupacion.POR_TERMINAR;
        }
        return AndraEstadoOcupacion.LIBRE;
    }

    private boolean andraOcupado(AndraEspacio andraEspacio, int andraIndice, int andraDia) {
        String andraFranja = AndraCatalogoServicio.ANDRA_FRANJAS.get(andraIndice);
        return andraEspacio.isAndraFueraDeServicio() || !AndraReservaServicio.andraDisponibleSimulado(andraEspacio.getAndraNombre(), andraDia, andraFranja)
                || andraReservas.andraOcupadoPorReserva(andraEspacio, AndraFechas.andraHoy().plusDays(andraDia), andraFranja);
    }

    public Map<String, List<AndraEspacio>> andraPorPabellon(List<AndraEspacio> andraEspacios) {
        Map<String, List<AndraEspacio>> andraGrupos = new LinkedHashMap<>();
        for (AndraEspacio andraEspacio : andraEspacios) {
            andraGrupos.computeIfAbsent(andraEspacio.andraObtenerPabellon(), andraTexto -> new ArrayList<>()).add(andraEspacio);
        }
        return andraGrupos;
    }

    public long andraLibres(List<AndraEspacio> andraEspacios, AndraFranjaBnps andraFranja) {
        return andraEspacios.stream().filter(andraEspacio -> andraEstado(andraEspacio, andraFranja.andraIndice(), andraFranja.andraDia()) == AndraEstadoOcupacion.LIBRE).count();
    }

    public static String andraNombreCorto(String andraNombre) {
        return andraNombre.replaceFirst("^(Laboratorio|Auditorio|Taller|Sala|Aula) (de |del )?", "");
    }
}
