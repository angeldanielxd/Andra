package pe.andra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.institucion")
@Getter
@Setter
public class DatosInstitucion {
    private String andraNombre = "Institución";
    private String andraSigla = "INST";
    private String andraTelefono = "(01) 000-0000";
    private String andraAnexos = "0000 / 0000";
    private String andraCorreoMesaPartes = "mesadepartes@institucion.edu";
    private String andraCorreoSoporte = "soporte@institucion.edu";
    private String andraCorreoDefensoria = "defensoria@institucion.edu";
    private String andraUrlYoutube = "https://www.youtube.com";
    private String andraUrlFacebook = "https://www.facebook.com";
    private String andraUrlDocumentos = "https://documentos.institucion.edu";
}
