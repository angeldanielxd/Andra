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
@EqualsAndHashCode(of = "dixonId")
public class DixonDocumento {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String dixonNombre;

    @Column(name = "descripcion", length = 400)
    private String dixonDescripcion;

    @Column(name = "tipo", nullable = false, length = 60)
    private String dixonTipo;

    @Column(name = "dependencia", nullable = false, length = 120)
    private String dixonDependencia;

    @Column(name = "publico", nullable = false, length = 40)
    private String dixonPublico;

    @Column(name = "fecha", nullable = false)
    private LocalDate dixonFecha;

    @Column(name = "frecuente", nullable = false)
    private boolean dixonFrecuente;

    @Column(name = "paginas", nullable = false)
    private int dixonPaginas;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private DixonEscuelaProfesional dixonEscuela;
}
