package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pe.andra.seeder.DixonSembradorDemo;

@Service
@RequiredArgsConstructor
public class DixonDemoServicio {

    private final DixonSembradorDemo dixonSembrador;
    private final ApplicationEventPublisher dixonEventos;

    public void dixonRestablecer() {
        dixonSembrador.dixonRestablecer();
        dixonEventos.publishEvent(new DixonCambioDatos("demo"));
    }
}
