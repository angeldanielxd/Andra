package pe.andra.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AndraAccionExpediente {
    @Column(name = "estilo", nullable = false, length = 12)
    private String andraEstilo;

    @Column(name = "accion", nullable = false, length = 20)
    private String andraAccion;

    @Column(name = "texto", nullable = false, length = 60)
    private String andraTexto;
}
