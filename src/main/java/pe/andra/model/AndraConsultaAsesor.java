package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consulta_asesor", uniqueConstraints = @UniqueConstraint(name = "uk_consulta_expediente", columnNames = "expediente_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraConsultaAsesor {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @OneToOne(optional = false)
    @JoinColumn(name = "expediente_id", nullable = false)
    private AndraExpediente andraExpediente;

    @Column(name = "usadas", nullable = false)
    private int andraUsadas;

    @Column(name = "cupo", nullable = false)
    private int andraCupo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "ampliacion", nullable = false, length = 10)
    private AndraEstadoAmpliacion andraAmpliacion = AndraEstadoAmpliacion.NINGUNA;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consulta_asesor_mensaje", joinColumns = @JoinColumn(name = "consulta_id"))
    @OrderColumn(name = "orden")
    private List<AndraMensajeAsesor> andraMensajes = new ArrayList<>();

    public int andraRestantes() {
        return Math.max(0, andraCupo - andraUsadas);
    }
}
