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
public class DixonBaseDatosSembrador implements ApplicationRunner {

    private final DixonSembradorDemo dixonSembrador;

    @Override
    public void run(ApplicationArguments dixonArgs) {
        DixonSembradorDemo.DixonResumen dixonResumen = dixonSembrador.dixonSembrar();
        log.info("Seeder: {} registros de catálogo nuevos; estado de demostración {}.", dixonResumen.dixonCatalogo(),
                dixonResumen.dixonEstadoNuevo() ? "creado" : "existente");
    }
}
