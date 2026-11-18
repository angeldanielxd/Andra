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
@EqualsAndHashCode(of = "dixonId")
public class DixonMensajeEnviado {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expediente_id", nullable = false)
    private DixonExpediente dixonExpediente;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 10)
    private DixonCanalAviso dixonCanal;

    @Column(name = "texto", nullable = false, length = 400)
    private String dixonTexto;

    @Column(name = "enviado", nullable = false)
    private LocalDateTime dixonEnviado;
}
