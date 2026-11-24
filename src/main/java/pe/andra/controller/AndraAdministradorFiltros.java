package pe.andra.controller;

import pe.andra.model.AndraPersona;
import pe.andra.service.AndraCatalogoServicio;
import pe.andra.ui.AndraFiltros;
import pe.andra.ui.AndraOpcion;

import java.util.ArrayList;
import java.util.List;

final class AndraAdministradorFiltros {

    private AndraAdministradorFiltros() {
    }

    static List<AndraOpcion> andraEscuelas(AndraCatalogoServicio andraCatalogo) {
        List<AndraOpcion> andraLista = new ArrayList<>();
        andraCatalogo.andraEscuelas().forEach(andraEscuelaProfesional -> andraLista.add(AndraOpcion.andraDe(andraEscuelaProfesional.getAndraCodigo(), andraEscuelaProfesional.getAndraNombre())));
        return andraLista;
    }

    static List<AndraFiltros.AndraAvanzado> andraPersona(AndraCatalogoServicio andraCatalogo, String andraEtiquetaCodigo, String andraEjemplo) {
        return new ArrayList<>(List.of(AndraFiltros.AndraAvanzado.andraTexto("code", andraEtiquetaCodigo, andraEjemplo),
                AndraFiltros.AndraAvanzado.andraSelector("fac", "Escuela profesional", "Todas las escuelas", andraEscuelas(andraCatalogo)),
                AndraFiltros.AndraAvanzado.andraSelector("tipo", "Tipo de solicitante", "Todos", AndraOpcion.andraLista(AndraCatalogoServicio.ANDRA_SOLICITANTES))));
    }

    static List<AndraFiltros.AndraAvanzado> andraFechas(String andraDesde, String andraHasta) {
        return new ArrayList<>(List.of(AndraFiltros.AndraAvanzado.andraFecha("from", andraDesde), AndraFiltros.AndraAvanzado.andraFecha("to", andraHasta)));
    }

    static String andraClave(AndraPersona andraPersona) {
        return andraPersona.getAndraCodigo() + " " + (andraPersona.getAndraDni() == null ? "" : andraPersona.getAndraDni()) + " " + andraPersona.getAndraNombre();
    }

    static String andraEscuela(AndraPersona andraPersona) {
        return andraPersona.getAndraEscuela() == null ? null : andraPersona.getAndraEscuela().getAndraCodigo();
    }
}
