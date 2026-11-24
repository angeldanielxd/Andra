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
@EqualsAndHashCode(of = "andraId")
public class AndraNotificacion {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destinatario", nullable = false, length = 14)
    private AndraRol andraDestinatario;

    @Column(name = "texto", nullable = false, length = 300)
    private String andraTexto;

    @Column(name = "ruta", nullable = false, length = 20)
    private String andraRuta;

    @Column(name = "leida", nullable = false)
    private boolean andraLeida;

    @Column(name = "creada", nullable = false)
    private LocalDateTime andraCreada;
}
