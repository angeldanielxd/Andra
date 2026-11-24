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
@EqualsAndHashCode(of = "andraId")
public class AndraEquipo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 10)
    private String andraCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private AndraCategoriaEquipo andraCategoria;

    @Column(name = "rotulo", nullable = false, length = 40)
    private String andraRotulo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String andraNombre;

    @Column(name = "especificaciones", length = 240)
    private String andraEspecificaciones;

    @Column(name = "ubicacion", length = 120)
    private String andraUbicacion;

    @Column(name = "punto_entrega", length = 80)
    private String andraPuntoEntrega;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private AndraSede andraSede;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private AndraEscuelaProfesional andraEscuela;

    @Column(name = "unidades_iniciales", nullable = false)
    private int andraUnidadesIniciales;

    @Column(name = "stock", nullable = false)
    private int andraSaldo;

    @Column(name = "unidad", nullable = false, length = 10)
    private String andraUnidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo", nullable = false, length = 10)
    private AndraModoEquipo andraModo;

    @Column(name = "proximo_turno", length = 10)
    private String andraProximoTurno;

    public boolean andraEsDisponibleAhora() {
        return andraSaldo > 0 && andraModo != AndraModoEquipo.TURNO;
    }

    public boolean andraEsPorTurno() {
        return andraModo == AndraModoEquipo.TURNO;
    }
}
