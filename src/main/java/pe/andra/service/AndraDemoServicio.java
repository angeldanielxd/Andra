package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pe.andra.seeder.AndraSembradorDemo;

@Service
@RequiredArgsConstructor
public class AndraDemoServicio {

    private final AndraSembradorDemo andraSembrador;
    private final ApplicationEventPublisher andraEventos;

    public void andraRestablecer() {
        andraSembrador.andraRestablecer();
        andraEventos.publishEvent(new AndraCambioDatos("demo"));
    }
}
