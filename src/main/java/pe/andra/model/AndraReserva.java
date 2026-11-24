package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "reserva", uniqueConstraints = @UniqueConstraint(name = "uk_reserva_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraReserva {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private AndraPersona andraPersona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "espacio_id", nullable = false)
    private AndraEspacio andraEspacio;

    @Column(name = "fecha", nullable = false)
    private LocalDate andraFecha;

    @Column(name = "horario", nullable = false, length = 15)
    private String andraHorario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private AndraEstadoReserva andraEstado;

    @Column(name = "motivo", nullable = false, length = 300)
    private String andraMotivo;

    @Column(name = "asistentes", nullable = false)
    private int andraAsistentes;

    @Column(name = "nota", length = 300)
    private String andraNota;

    public String andraObtenerInicio() {
        return andraHorario.substring(0, 5);
    }

    public String andraObtenerFin() {
        return andraHorario.substring(andraHorario.length() - 5);
    }
}
