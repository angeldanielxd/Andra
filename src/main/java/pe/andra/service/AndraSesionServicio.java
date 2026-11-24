package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.AndraAdministrador;
import pe.andra.model.AndraPersona;
import pe.andra.model.AndraRol;
import pe.andra.repository.AndraAdministradorRepositorio;
import pe.andra.repository.AndraPersonaRepositorio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraHash;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AndraSesionServicio {

    public record AndraRuta(String andraClave, String andraEtiqueta) {
    }

    public static final List<AndraRuta> ANDRA_RUTAS_ESTUDIANTE = List.of(new AndraRuta("inicio", "Inicio"),
            new AndraRuta("documentos", "Documentos"), new AndraRuta("reservas", "Reservas"), new AndraRuta("equipos", "Equipos"),
            new AndraRuta("solicitudes", "Mis Solicitudes"), new AndraRuta("pay", "Andra Pay"), new AndraRuta("ayuda", "Ayuda"),
            new AndraRuta("favoritos", "Favoritos"));
    public static final List<AndraRuta> ANDRA_RUTAS_ADMINISTRADOR = List.of(new AndraRuta("admin", "Panel"), new AndraRuta("a-datos", "Análisis"),
            new AndraRuta("a-tramites", "Trámites"), new AndraRuta("a-pagos", "Transacciones"), new AndraRuta("a-reservas", "Reservas"),
            new AndraRuta("a-prestamos", "Préstamos"));

    private static final String ANDRA_HUELLA_ESTUDIANTE_DASM = "8f011ca2ec2b1943dbb9a3438decb59aa13f953c49878e63be361f3845d7e89f";
    private static final String ANDRA_HUELLA_ADMINISTRADOR_DASM = "6051fc84a7a0d74c225fb18a496b09952da5642e60723ecae543298edd7d82d6";

    private final AndraAjustesServicio andraAjustes;
    private final AndraPersonaRepositorio andraPersonas;
    private final AndraAdministradorRepositorio andraAdministradores;
    private final AndraContextoServicio andraContexto;

    public AndraRol andraRol() {
        return andraAjustes.andraObtener().getAndraSesion();
    }

    public boolean andraConectado() {
        return andraRol() != null;
    }

    public boolean andraEsAdministrador() {
        return andraRol() == AndraRol.ADMINISTRADOR;
    }

    public AndraPersona andraEstudiante() {
        return andraPersonas.findFirstByAndraPrincipalTrue().orElseThrow(() -> new AndraNegocioExcepcion("No hay estudiante registrado."));
    }

    public AndraAdministrador andraAdministrador() {
        return andraAdministradores.findFirstByOrderByAndraIdAsc().orElseThrow(() -> new AndraNegocioExcepcion("No hay administrador."));
    }

    public AndraRol andraIngresar(AndraRol andraRol, String andraUsuario, String andraContrasena) {
        String andraTexto = andraUsuario == null ? "" : andraUsuario.trim().toLowerCase();
        String andraHuella = AndraHash.andraSha256Hex(andraContrasena == null ? "" : andraContrasena);
        if (andraRol == AndraRol.ESTUDIANTE) {
            AndraPersona andraPersona = andraEstudiante();
            boolean andraCoincide = (andraTexto.equals(andraPersona.getAndraCodigo()) || andraTexto.equals(andraPersona.getAndraDni())) && ANDRA_HUELLA_ESTUDIANTE_DASM.equals(andraHuella);
            if (!andraCoincide) {
                throw new AndraNegocioExcepcion("El código, DNI o contraseña no coinciden. Revisa los datos o usa la cuenta de demostración.");
            }
            andraContexto.andraAsegurarSedeParaEstudiante(andraPersona.getAndraSede().getAndraCodigo());
        } else {
            AndraAdministrador andraAdministrador = andraAdministrador();
            boolean andraCoincide = andraTexto.equals(andraAdministrador.getAndraCorreo().toLowerCase()) && ANDRA_HUELLA_ADMINISTRADOR_DASM.equals(andraHuella);
            if (!andraCoincide) {
                throw new AndraNegocioExcepcion("El correo o la contraseña no coinciden. Revisa los datos o usa la cuenta de demostración.");
            }
        }
        andraAjustes.andraCambiar(andraAjustes2 -> {
            andraAjustes2.setAndraSesion(andraRol);
            andraAjustes2.setAndraSesionDesde(AndraFechas.andraAhora());
        });
        return andraRol;
    }

    public void andraCerrar() {
        andraAjustes.andraCambiar(andraAjustes2 -> {
            andraAjustes2.setAndraSesion(null);
            andraAjustes2.setAndraSesionDesde(null);
        });
    }

    public static List<AndraRuta> andraRutas(AndraRol andraRol) {
        return andraRol == AndraRol.ADMINISTRADOR ? ANDRA_RUTAS_ADMINISTRADOR : ANDRA_RUTAS_ESTUDIANTE;
    }

    public static boolean andraPuedeAcceder(AndraRol andraRol, String andraRuta) {
        if (andraRol == null) {
            return "login".equals(andraRuta);
        }
        if (andraRol == AndraRol.ESTUDIANTE && "carne".equals(andraRuta)) {
            return true;
        }
        return andraRutas(andraRol).stream().anyMatch(andraRuta2 -> andraRuta2.andraClave().equals(andraRuta));
    }

    public static String andraRutaInicial(AndraRol andraRol) {
        return andraRol == null ? "login" : andraRutas(andraRol).get(0).andraClave();
    }

    public static String andraEtiqueta(String andraRuta) {
        if ("carne".equals(andraRuta)) {
            return "Mi carné";
        }
        return java.util.stream.Stream.concat(ANDRA_RUTAS_ESTUDIANTE.stream(), ANDRA_RUTAS_ADMINISTRADOR.stream())
                .filter(andraRuta2 -> andraRuta2.andraClave().equals(andraRuta)).map(AndraRuta::andraEtiqueta).findFirst().orElse("");
    }
}
