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
@EqualsAndHashCode(of = "dixonId")
public class DixonMatricula {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "periodo", nullable = false, length = 10)
    private String dixonPeriodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private DixonEstadoMatricula dixonEstado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private DixonPago dixonPago;

    public boolean dixonEsConfirmada() {
        return dixonEstado == DixonEstadoMatricula.CONFIRMADA;
    }
}
