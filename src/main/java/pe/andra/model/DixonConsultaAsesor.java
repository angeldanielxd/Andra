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
@EqualsAndHashCode(of = "dixonId")
public class DixonConsultaAsesor {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @OneToOne(optional = false)
    @JoinColumn(name = "expediente_id", nullable = false)
    private DixonExpediente dixonExpediente;

    @Column(name = "usadas", nullable = false)
    private int dixonUsadas;

    @Column(name = "cupo", nullable = false)
    private int dixonCupo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "ampliacion", nullable = false, length = 10)
    private DixonEstadoAmpliacion dixonAmpliacion = DixonEstadoAmpliacion.NINGUNA;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consulta_asesor_mensaje", joinColumns = @JoinColumn(name = "consulta_id"))
    @OrderColumn(name = "orden")
    private List<DixonMensajeAsesor> dixonMensajes = new ArrayList<>();

    public int dixonRestantes() {
        return Math.max(0, dixonCupo - dixonUsadas);
    }
}
