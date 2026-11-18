package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamo_activo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonPrestamoActivo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private DixonPersona dixonPersona;

    @Column(name = "equipo", nullable = false, length = 120)
    private String dixonEquipo;

    @Column(name = "retiro", nullable = false, length = 60)
    private String dixonRetiro;

    @Column(name = "limite", nullable = false)
    private LocalDateTime dixonLimite;

    @Column(name = "renovado", nullable = false)
    private boolean dixonRenovado;

    @Column(name = "incidencia", length = 60)
    private String dixonIncidencia;

    @Column(name = "lugar", nullable = false, length = 160)
    private String dixonLugar;

    @Column(name = "devuelto", nullable = false)
    private boolean dixonDevuelto;
}
