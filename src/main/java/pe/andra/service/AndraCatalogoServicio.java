package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AndraCatalogoServicio {

    public static final List<String> ANDRA_FRANJAS = List.of("08:00", "10:00", "12:00", "14:00", "16:00");
    public static final List<String> ANDRA_HORARIOS = List.of("08:00 - 10:00", "10:00 - 12:00", "12:00 - 14:00",
            "14:00 - 16:00", "16:00 - 18:00");
    public static final List<String> ANDRA_SOLICITANTES = List.of("Estudiante", "Egresado", "Docente", "Administrativo",
            "Público en general");

    private final AndraSedeRepositorio andraSedes;
    private final AndraEscuelaProfesionalRepositorio andraEscuelas;
    private final AndraEspacioRepositorio andraEspacios;
    private final AndraDocumentoRepositorio andraDocumentos;
    private final AndraCategoriaEquipoRepositorio andraCategorias;
    private final AndraEquipoRepositorio andraEquipos;
    private final AndraTramiteRepositorio andraTramites;
    private final AndraPersonaRepositorio andraPersonas;
    private final AndraJsonRecursos andraJson;

    public List<AndraSede> andraSedes() {
        return andraSedes.findAllByOrderByAndraIdAsc();
    }

    public Optional<AndraSede> andraSede(String andraCodigo) {
        return andraSedes.findByAndraCodigo(andraCodigo);
    }

    public List<AndraEscuelaProfesional> andraEscuelas() {
        return andraEscuelas.findAllByOrderByAndraIdAsc();
    }

    public List<AndraEscuelaProfesional> andraEscuelasDe(AndraSede andraSede) {
        return andraEscuelas().stream().filter(andraEscuelaProfesional -> andraEscuelaProfesional.getAndraSede().getAndraId().equals(andraSede.getAndraId())).toList();
    }

    public Optional<AndraEscuelaProfesional> andraEscuela(String andraCodigo) {
        return andraCodigo == null ? Optional.empty() : andraEscuelas.findByAndraCodigo(andraCodigo);
    }

    public String andraNombreEscuela(String andraCodigo) {
        return andraEscuela(andraCodigo).map(AndraEscuelaProfesional::getAndraNombre).orElse("Uso general");
    }

    public List<AndraEspacio> andraEspacios() {
        return andraEspacios.findAllByOrderByAndraIdAsc();
    }

    public Optional<AndraEspacio> andraEspacio(String andraCodigo) {
        return andraEspacios.findByAndraCodigo(andraCodigo);
    }

    public List<AndraDocumento> andraDocumentos() {
        return andraDocumentos.findAllByOrderByAndraIdAsc();
    }

    public List<AndraCategoriaEquipo> andraCategorias() {
        return andraCategorias.findAllByOrderByAndraIdAsc();
    }

    public List<AndraEquipo> andraEquipos() {
        return andraEquipos.findAllByOrderByAndraIdAsc();
    }

    public Optional<AndraEquipo> andraEquipo(String andraCodigo) {
        return andraEquipos.findByAndraCodigo(andraCodigo);
    }

    public List<AndraTramite> andraTramites() {
        return andraTramites.findAllByOrderByAndraIdAsc();
    }

    public Optional<AndraTramite> andraTramite(String andraCodigo) {
        return andraTramites.findByAndraCodigo(andraCodigo);
    }

    public List<AndraPersona> andraPersonas() {
        return andraPersonas.findAllByOrderByAndraIdAsc();
    }

    public Optional<AndraPersona> andraPersona(String andraCodigo) {
        return andraPersonas.findByAndraCodigo(andraCodigo);
    }

    public Optional<AndraPersona> andraPersonaPorCodigoODni(String andraValor) {
        String andraValor2 = andraValor == null ? "" : andraValor.trim();
        return andraPersonas.findByAndraCodigo(andraValor2).or(() -> andraPersonas.findFirstByAndraDni(andraValor2));
    }

    public List<String> andraTiposDocumento() {
        List<String> andraLista = new ArrayList<>();
        andraJson.andraCatalogo().path("tiposDocumento").forEach(andraNodo -> andraLista.add(andraNodo.asText()));
        return andraLista;
    }

    public List<String> andraDependencias() {
        List<String> andraLista = new ArrayList<>();
        andraJson.andraCatalogo().path("dependenciasAdministrativas").forEach(andraNodo -> andraLista.add(andraNodo.asText()));
        return andraLista;
    }

    public String andraEtiquetaDependencia(String andraDependencia) {
        return andraEscuelas().stream().anyMatch(andraEscuelaProfesional -> andraEscuelaProfesional.getAndraNombre().equals(andraDependencia))
                ? "Escuela Profesional de " + andraDependencia : andraDependencia;
    }
}
