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
@EqualsAndHashCode(of = "andraId")
public class AndraPrestamoActivo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private AndraPersona andraPersona;

    @Column(name = "equipo", nullable = false, length = 120)
    private String andraEquipo;

    @Column(name = "retiro", nullable = false, length = 60)
    private String andraRetiro;

    @Column(name = "limite", nullable = false)
    private LocalDateTime andraLimite;

    @Column(name = "renovado", nullable = false)
    private boolean andraRenovado;

    @Column(name = "incidencia", length = 60)
    private String andraIncidencia;

    @Column(name = "lugar", nullable = false, length = 160)
    private String andraLugar;

    @Column(name = "devuelto", nullable = false)
    private boolean andraDevuelto;
}
