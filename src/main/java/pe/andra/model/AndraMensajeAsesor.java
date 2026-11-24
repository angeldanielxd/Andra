package pe.andra.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AndraMensajeAsesor {
    @Column(name = "de_asesor", nullable = false)
    private boolean andraDeAsesor;

    @Column(name = "texto", nullable = false, length = 1000)
    private String andraTexto;

    @Column(name = "enviado", nullable = false)
    private LocalDateTime andraEnviado;
}
