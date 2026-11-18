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
@EqualsAndHashCode(of = "dixonId")
public class DixonPersona {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String dixonNombre;

    @Column(name = "dni", length = 8)
    private String dixonDni;

    @Column(name = "tipo", nullable = false, length = 30)
    private String dixonTipo;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private DixonEscuelaProfesional dixonEscuela;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;

    @Column(name = "correo", length = 80)
    private String dixonCorreo;

    @Column(name = "celular", length = 15)
    private String dixonCelular;

    @Column(name = "principal", nullable = false)
    private boolean dixonPrincipal;

    public String dixonObtenerPrimerNombre() {
        return dixonNombre.split(" ")[0];
    }

    public String dixonObtenerIniciales() {
        StringBuilder dixonTexto = new StringBuilder();
        for (String dixonTexto2 : dixonNombre.split(" ")) {
            if (!dixonTexto2.isEmpty() && Character.isUpperCase(dixonTexto2.charAt(0)) && dixonTexto.length() < 2) {
                dixonTexto.append(dixonTexto2.charAt(0));
            }
        }
        return dixonTexto.toString();
    }
}
