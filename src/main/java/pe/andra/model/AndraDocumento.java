package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "documento", uniqueConstraints = @UniqueConstraint(name = "uk_documento_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraDocumento {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String andraNombre;

    @Column(name = "descripcion", length = 400)
    private String andraDescripcion;

    @Column(name = "tipo", nullable = false, length = 60)
    private String andraTipo;

    @Column(name = "dependencia", nullable = false, length = 120)
    private String andraDependencia;

    @Column(name = "publico", nullable = false, length = 40)
    private String andraPublico;

    @Column(name = "fecha", nullable = false)
    private LocalDate andraFecha;

    @Column(name = "frecuente", nullable = false)
    private boolean andraFrecuente;

    @Column(name = "paginas", nullable = false)
    private int andraPaginas;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private AndraEscuelaProfesional andraEscuela;
}
