package pe.andra.controller;

import pe.andra.model.DixonPersona;
import pe.andra.service.DixonCatalogoServicio;
import pe.andra.ui.AndraFiltros;
import pe.andra.ui.AndraOpcion;

import java.util.ArrayList;
import java.util.List;

final class AndraAdministradorFiltros {

    private AndraAdministradorFiltros() {
    }

    static List<AndraOpcion> andraEscuelas(DixonCatalogoServicio andraCatalogo) {
        List<AndraOpcion> andraLista = new ArrayList<>();
        andraCatalogo.dixonEscuelas().forEach(andraEscuelaProfesional -> andraLista.add(AndraOpcion.andraDe(andraEscuelaProfesional.getDixonCodigo(), andraEscuelaProfesional.getDixonNombre())));
        return andraLista;
    }

    static List<AndraFiltros.AndraAvanzado> andraPersona(DixonCatalogoServicio andraCatalogo, String andraEtiquetaCodigo, String andraEjemplo) {
        return new ArrayList<>(List.of(AndraFiltros.AndraAvanzado.andraTexto("code", andraEtiquetaCodigo, andraEjemplo),
                AndraFiltros.AndraAvanzado.andraSelector("fac", "Escuela profesional", "Todas las escuelas", andraEscuelas(andraCatalogo)),
                AndraFiltros.AndraAvanzado.andraSelector("tipo", "Tipo de solicitante", "Todos", AndraOpcion.andraLista(DixonCatalogoServicio.DIXON_SOLICITANTES))));
    }

    static List<AndraFiltros.AndraAvanzado> andraFechas(String andraDesde, String andraHasta) {
        return new ArrayList<>(List.of(AndraFiltros.AndraAvanzado.andraFecha("from", andraDesde), AndraFiltros.AndraAvanzado.andraFecha("to", andraHasta)));
    }

    static String andraClave(DixonPersona andraPersona) {
        return andraPersona.getDixonCodigo() + " " + (andraPersona.getDixonDni() == null ? "" : andraPersona.getDixonDni()) + " " + andraPersona.getDixonNombre();
    }

    static String andraEscuela(DixonPersona andraPersona) {
        return andraPersona.getDixonEscuela() == null ? null : andraPersona.getDixonEscuela().getDixonCodigo();
    }
}
