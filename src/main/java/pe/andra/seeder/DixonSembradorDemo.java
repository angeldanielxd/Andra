package pe.andra.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DixonSembradorDemo {

    public record DixonResumen(int dixonCatalogo, boolean dixonEstadoNuevo) {
    }

    private final DixonCatalogoSembrador dixonCatalogoSembrador;
    private final DixonEstadoDemoSembrador dixonEstadoSembrador;

    public DixonResumen dixonSembrar() {
        int dixonCatalogo = dixonCatalogoSembrador.dixonSembrar();
        boolean dixonNuevo = dixonEstadoSembrador.dixonSembrar();
        return new DixonResumen(dixonCatalogo, dixonNuevo);
    }

    public void dixonRestablecer() {
        dixonCatalogoSembrador.dixonSembrar();
        dixonEstadoSembrador.dixonRestablecer();
    }
}
