package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.AndraAjustes;
import pe.andra.model.AndraEscuelaProfesional;
import pe.andra.model.AndraSede;

@Service
@RequiredArgsConstructor
public class AndraContextoServicio {

    private final AndraAjustesServicio andraAjustes;
    private final AndraCatalogoServicio andraCatalogo;

    public boolean andraTodasLasSedes() {
        return AndraAjustes.ANDRA_TODAS.equals(andraAjustes.andraObtener().getAndraSede());
    }

    public String andraCodigoSede() {
        return andraAjustes.andraObtener().getAndraSede();
    }

    public AndraSede andraSede() {
        String andraTexto = andraCodigoSede();
        return AndraAjustes.ANDRA_TODAS.equals(andraTexto) ? null : andraCatalogo.andraSede(andraTexto).orElse(null);
    }

    public AndraSede andraSedeOPrincipal() {
        AndraSede andraSede = andraSede();
        return andraSede != null ? andraSede : andraCatalogo.andraSede("central").orElseThrow();
    }

    public String andraNombreSede() {
        AndraSede andraSede = andraSede();
        return andraSede == null ? "Todas" : andraSede.getAndraNombre();
    }

    public String andraTextoSede() {
        AndraSede andraSede = andraSede();
        return andraSede == null ? "Todas las sedes" : "Sede " + andraSede.getAndraNombre();
    }

    public AndraEscuelaProfesional andraEscuela() {
        return andraCatalogo.andraEscuela(andraAjustes.andraObtener().getAndraEscuela()).orElse(null);
    }

    public String andraTextoEscuela() {
        AndraEscuelaProfesional andraEscuelaProfesional = andraEscuela();
        return andraEscuelaProfesional == null ? "Todas las escuelas" : "EP " + andraEscuelaProfesional.getAndraNombre();
    }

    public boolean andraIncluyeSede(AndraSede andraSede) {
        String andraTexto = andraCodigoSede();
        return AndraAjustes.ANDRA_TODAS.equals(andraTexto) || (andraSede != null && andraSede.getAndraCodigo().equals(andraTexto));
    }

    public boolean andraIncluyeEscuela(AndraEscuelaProfesional andraEscuelaRecurso) {
        String andraElegida = andraAjustes.andraObtener().getAndraEscuela();
        return andraElegida == null || andraEscuelaRecurso == null || andraEscuelaRecurso.getAndraCodigo().equals(andraElegida);
    }

    public boolean andraIncluye(AndraSede andraSede, AndraEscuelaProfesional andraEscuelaRecurso) {
        return andraIncluyeSede(andraSede) && andraIncluyeEscuela(andraEscuelaRecurso);
    }

    public static boolean andraPertenece(String andraSedeContexto, String andraEscuelaContexto, AndraSede andraSede, AndraEscuelaProfesional andraEscuela) {
        boolean andraEnSede = AndraAjustes.ANDRA_TODAS.equals(andraSedeContexto) || (andraSede != null && andraSede.getAndraCodigo().equals(andraSedeContexto));
        boolean andraEnEscuela = andraEscuelaContexto == null || andraEscuela == null || andraEscuela.getAndraCodigo().equals(andraEscuelaContexto);
        return andraEnSede && andraEnEscuela;
    }

    public String andraCambiarSede(String andraCodigo) {
        if (AndraAjustes.ANDRA_TODAS.equals(andraCodigo)) {
            andraAjustes.andraCambiar(andraAjustes2 -> {
                andraAjustes2.setAndraSede(AndraAjustes.ANDRA_TODAS);
                andraAjustes2.setAndraEscuela(null);
            });
            return "Mostrando información de todas las sedes.";
        }
        AndraSede andraSede = andraCatalogo.andraSede(andraCodigo).orElseThrow(() -> new AndraNegocioExcepcion("La sede no existe."));
        AndraEscuelaProfesional andraActual = andraEscuela();
        boolean andraLimpia = andraActual != null && !andraActual.getAndraSede().getAndraCodigo().equals(andraCodigo);
        andraAjustes.andraCambiar(andraAjustes3 -> {
            andraAjustes3.setAndraSede(andraCodigo);
            if (andraLimpia) {
                andraAjustes3.setAndraEscuela(null);
            }
        });
        return "Mostrando información de la Sede " + andraSede.getAndraNombre() + (andraLimpia ? " · todas las escuelas" : "") + ".";
    }

    public String andraCambiarEscuela(String andraCodigo) {
        if (andraCodigo == null) {
            andraAjustes.andraCambiar(andraAjustes2 -> andraAjustes2.setAndraEscuela(null));
            return "Mostrando todas las escuelas de la sede.";
        }
        AndraEscuelaProfesional andraEscuelaProfesional = andraCatalogo.andraEscuela(andraCodigo).orElseThrow(() -> new AndraNegocioExcepcion("La escuela no existe."));
        andraAjustes.andraCambiar(andraAjustes3 -> {
            andraAjustes3.setAndraSede(andraEscuelaProfesional.getAndraSede().getAndraCodigo());
            andraAjustes3.setAndraEscuela(andraEscuelaProfesional.getAndraCodigo());
        });
        return "EP " + andraEscuelaProfesional.getAndraNombre() + " · Sede " + andraEscuelaProfesional.getAndraSede().getAndraNombre() + ".";
    }

    public void andraAsegurarSedeParaEstudiante(String andraSedePropia) {
        if (andraTodasLasSedes()) {
            andraAjustes.andraCambiar(andraAjustes2 -> {
                andraAjustes2.setAndraSede(andraSedePropia);
                andraAjustes2.setAndraEscuela(null);
            });
        }
    }
}
