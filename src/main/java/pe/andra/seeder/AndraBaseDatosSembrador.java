package pe.andra.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true")
public class AndraBaseDatosSembrador implements ApplicationRunner {

    private final AndraSembradorDemo andraSembrador;

    @Override
    public void run(ApplicationArguments andraArgs) {
        AndraSembradorDemo.AndraResumen andraResumen = andraSembrador.andraSembrar();
        log.info("Seeder: {} registros de catálogo nuevos; estado de demostración {}.", andraResumen.andraCatalogo(),
                andraResumen.andraEstadoNuevo() ? "creado" : "existente");
    }
}
