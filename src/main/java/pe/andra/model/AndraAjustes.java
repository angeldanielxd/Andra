package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ajustes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraAjustes {
    public static final String ANDRA_TODAS = "all";

    @Column(name = "id")
    @Id
    private Long andraId;

    @Column(name = "idioma", nullable = false, length = 2)
    private String andraIdioma;

    @Column(name = "sede", nullable = false, length = 20)
    private String andraSede;

    @Column(name = "escuela", length = 10)
    private String andraEscuela;

    @Column(name = "aviso_whatsapp", nullable = false)
    private boolean andraAvisoWhatsapp;

    @Column(name = "aviso_correo", nullable = false)
    private boolean andraAvisoCorreo;

    @Column(name = "aviso_app", nullable = false)
    private boolean andraAvisoAplicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "sesion", length = 14)
    private AndraRol andraSesion;

    @Column(name = "sesion_desde")
    private LocalDateTime andraSesionDesde;

    @Column(name = "reducir_movimiento", nullable = false)
    private boolean andraReducirMovimiento;

    @Column(name = "ventanax")
    private Double andraVentanaX;

    @Column(name = "ventanay")
    private Double andraVentanaY;

    @Column(name = "ventana_ancho")
    private Double andraVentanaAncho;

    @Column(name = "ventana_alto")
    private Double andraVentanaAlto;

    @Column(name = "maximizada", nullable = false)
    private boolean andraMaximizada;

    @Column(name = "version", nullable = false)
    private int andraVersion;
}
