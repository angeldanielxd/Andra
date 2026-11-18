package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonNotificacion {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destinatario", nullable = false, length = 14)
    private DixonRol dixonDestinatario;

    @Column(name = "texto", nullable = false, length = 300)
    private String dixonTexto;

    @Column(name = "ruta", nullable = false, length = 20)
    private String dixonRuta;

    @Column(name = "leida", nullable = false)
    private boolean dixonLeida;

    @Column(name = "creada", nullable = false)
    private LocalDateTime dixonCreada;
}
