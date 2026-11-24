package pe.andra.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AndraSembradorDemo {

    public record AndraResumen(int andraCatalogo, boolean andraEstadoNuevo) {
    }

    private final AndraCatalogoSembrador andraCatalogoSembrador;
    private final AndraEstadoDemoSembrador andraEstadoSembrador;

    public AndraResumen andraSembrar() {
        int andraCatalogo = andraCatalogoSembrador.andraSembrar();
        boolean andraNuevo = andraEstadoSembrador.andraSembrar();
        return new AndraResumen(andraCatalogo, andraNuevo);
    }

    public void andraRestablecer() {
        andraCatalogoSembrador.andraSembrar();
        andraEstadoSembrador.andraRestablecer();
    }
}
