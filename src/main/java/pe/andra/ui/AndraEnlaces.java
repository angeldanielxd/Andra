package pe.andra.ui;

import javafx.application.HostServices;
import org.springframework.stereotype.Component;

@Component
public class AndraEnlaces {

    private HostServices andraServicios;

    public void andraAsignarServiciosAnfitrion(HostServices andraServicios) {
        this.andraServicios = andraServicios;
    }

    public void andraAbrir(String andraUrl) {
        if (andraServicios != null && andraUrl != null && !andraUrl.isBlank()) {
            andraServicios.showDocument(andraUrl);
        }
    }
}
