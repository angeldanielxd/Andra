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
@EqualsAndHashCode(of = "dixonId")
public class DixonReserva {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private DixonPersona dixonPersona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "espacio_id", nullable = false)
    private DixonEspacio dixonEspacio;

    @Column(name = "fecha", nullable = false)
    private LocalDate dixonFecha;

    @Column(name = "horario", nullable = false, length = 15)
    private String dixonHorario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private DixonEstadoReserva dixonEstado;

    @Column(name = "motivo", nullable = false, length = 300)
    private String dixonMotivo;

    @Column(name = "asistentes", nullable = false)
    private int dixonAsistentes;

    @Column(name = "nota", length = 300)
    private String dixonNota;

    public String dixonObtenerInicio() {
        return dixonHorario.substring(0, 5);
    }

    public String dixonObtenerFin() {
        return dixonHorario.substring(dixonHorario.length() - 5);
    }
}
