package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "matricula")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraMatricula {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "periodo", nullable = false, length = 10)
    private String andraPeriodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private AndraEstadoMatricula andraEstado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private AndraPago andraPago;

    public boolean andraEsConfirmada() {
        return andraEstado == AndraEstadoMatricula.CONFIRMADA;
    }
}
