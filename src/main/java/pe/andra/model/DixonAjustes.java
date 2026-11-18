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
@EqualsAndHashCode(of = "dixonId")
public class DixonAjustes {
    public static final String DIXON_TODAS = "all";

    @Column(name = "id")
    @Id
    private Long dixonId;

    @Column(name = "idioma", nullable = false, length = 2)
    private String dixonIdioma;

    @Column(name = "sede", nullable = false, length = 20)
    private String dixonSede;

    @Column(name = "escuela", length = 10)
    private String dixonEscuela;

    @Column(name = "aviso_whatsapp", nullable = false)
    private boolean dixonAvisoWhatsapp;

    @Column(name = "aviso_correo", nullable = false)
    private boolean dixonAvisoCorreo;

    @Column(name = "aviso_app", nullable = false)
    private boolean dixonAvisoAplicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "sesion", length = 14)
    private DixonRol dixonSesion;

    @Column(name = "sesion_desde")
    private LocalDateTime dixonSesionDesde;

    @Column(name = "reducir_movimiento", nullable = false)
    private boolean dixonReducirMovimiento;

    @Column(name = "ventanax")
    private Double dixonVentanaX;

    @Column(name = "ventanay")
    private Double dixonVentanaY;

    @Column(name = "ventana_ancho")
    private Double dixonVentanaAncho;

    @Column(name = "ventana_alto")
    private Double dixonVentanaAlto;

    @Column(name = "maximizada", nullable = false)
    private boolean dixonMaximizada;

    @Column(name = "version", nullable = false)
    private int dixonVersion;
}
