package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persona", uniqueConstraints = @UniqueConstraint(name = "uk_persona_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraPersona {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String andraNombre;

    @Column(name = "dni", length = 8)
    private String andraDni;

    @Column(name = "tipo", nullable = false, length = 30)
    private String andraTipo;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private AndraEscuelaProfesional andraEscuela;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private AndraSede andraSede;

    @Column(name = "correo", length = 80)
    private String andraCorreo;

    @Column(name = "celular", length = 15)
    private String andraCelular;

    @Column(name = "principal", nullable = false)
    private boolean andraPrincipal;

    public String andraObtenerPrimerNombre() {
        return andraNombre.split(" ")[0];
    }

    public String andraObtenerIniciales() {
        StringBuilder andraTexto = new StringBuilder();
        for (String andraTexto2 : andraNombre.split(" ")) {
            if (!andraTexto2.isEmpty() && Character.isUpperCase(andraTexto2.charAt(0)) && andraTexto.length() < 2) {
                andraTexto.append(andraTexto2.charAt(0));
            }
        }
        return andraTexto.toString();
    }
}
