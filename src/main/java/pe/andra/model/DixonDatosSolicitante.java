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
public class DixonDatosSolicitante {
    @Column(name = "sol_tipo", length = 30)
    private String dixonTipo;

    @Column(name = "sol_nombre", length = 100)
    private String dixonNombre;

    @Column(name = "sol_dni", length = 8)
    private String dixonDni;

    @Column(name = "sol_codigo", length = 60)
    private String dixonCodigo;

    @Column(name = "sol_escuela", length = 120)
    private String dixonEscuela;

    @Column(name = "sol_correo", length = 80)
    private String dixonCorreo;
}
