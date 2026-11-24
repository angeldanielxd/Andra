package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje_enviado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraMensajeEnviado {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expediente_id", nullable = false)
    private AndraExpediente andraExpediente;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 10)
    private AndraCanalAviso andraCanal;

    @Column(name = "texto", nullable = false, length = 400)
    private String andraTexto;

    @Column(name = "enviado", nullable = false)
    private LocalDateTime andraEnviado;
}
