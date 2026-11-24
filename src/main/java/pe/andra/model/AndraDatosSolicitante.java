package pe.andra.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AndraDatosSolicitante {
    @Column(name = "sol_tipo", length = 30)
    private String andraTipo;

    @Column(name = "sol_nombre", length = 100)
    private String andraNombre;

    @Column(name = "sol_dni", length = 8)
    private String andraDni;

    @Column(name = "sol_codigo", length = 60)
    private String andraCodigo;

    @Column(name = "sol_escuela", length = 120)
    private String andraEscuela;

    @Column(name = "sol_correo", length = 80)
    private String andraCorreo;
}
