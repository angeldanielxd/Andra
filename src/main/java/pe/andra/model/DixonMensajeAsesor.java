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
public class DixonMensajeAsesor {
    @Column(name = "de_asesor", nullable = false)
    private boolean dixonDeAsesor;

    @Column(name = "texto", nullable = false, length = 1000)
    private String dixonTexto;

    @Column(name = "enviado", nullable = false)
    private LocalDateTime dixonEnviado;
}
