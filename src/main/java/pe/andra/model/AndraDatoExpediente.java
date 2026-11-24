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
public class AndraDatoExpediente {
    @Column(name = "etiqueta", nullable = false, length = 60)
    private String andraEtiqueta;

    @Column(name = "valor", nullable = false, length = 200)
    private String andraValor;
}
