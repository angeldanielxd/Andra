package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipo", uniqueConstraints = @UniqueConstraint(name = "uk_equipo_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonEquipo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 10)
    private String dixonCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private DixonCategoriaEquipo dixonCategoria;

    @Column(name = "rotulo", nullable = false, length = 40)
    private String dixonRotulo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String dixonNombre;

    @Column(name = "especificaciones", length = 240)
    private String dixonEspecificaciones;

    @Column(name = "ubicacion", length = 120)
    private String dixonUbicacion;

    @Column(name = "punto_entrega", length = 80)
    private String dixonPuntoEntrega;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private DixonEscuelaProfesional dixonEscuela;

    @Column(name = "unidades_iniciales", nullable = false)
    private int dixonUnidadesIniciales;

    @Column(name = "stock", nullable = false)
    private int dixonSaldo;

    @Column(name = "unidad", nullable = false, length = 10)
    private String dixonUnidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo", nullable = false, length = 10)
    private DixonModoEquipo dixonModo;

    @Column(name = "proximo_turno", length = 10)
    private String dixonProximoTurno;

    public boolean dixonEsDisponibleAhora() {
        return dixonSaldo > 0 && dixonModo != DixonModoEquipo.TURNO;
    }

    public boolean dixonEsPorTurno() {
        return dixonModo == DixonModoEquipo.TURNO;
    }
}
