package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expediente", uniqueConstraints = @UniqueConstraint(name = "uk_expediente_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonExpediente {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 30)
    private String dixonCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private DixonPersona dixonPersona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;

    @Enumerated(EnumType.STRING)
    @Column(name = "clase", nullable = false, length = 6)
    private DixonClaseExpediente dixonClase;

    @ManyToOne
    @JoinColumn(name = "tramite_id")
    private DixonTramite dixonTramite;

    @ManyToOne
    @JoinColumn(name = "pago_id")
    private DixonPago dixonPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "grupo", nullable = false, length = 12)
    private DixonGrupoExpediente dixonGrupo;

    @Column(name = "icono", nullable = false, length = 20)
    private String dixonIcono;

    @Column(name = "rotulo_icono", nullable = false, length = 20)
    private String dixonRotuloIcono;

    @Column(name = "tono", nullable = false, length = 10)
    private String dixonTono;

    @Column(name = "estado", nullable = false, length = 100)
    private String dixonEstado;

    @Column(name = "titulo", nullable = false, length = 200)
    private String dixonTitulo;

    @Column(name = "descripcion", length = 400)
    private String dixonDescripcion;

    @Column(name = "paso", nullable = false)
    private int dixonPaso;

    @Column(name = "observacion", length = 500)
    private String dixonObservacion;

    @Column(name = "destacado", nullable = false)
    private boolean dixonDestacado;

    @Column(name = "nota", length = 200)
    private String dixonNota;

    @Embedded
    private DixonDatosSolicitante dixonSolicitante;

    @Column(name = "fecha")
    private LocalDateTime dixonFecha;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente_dato", joinColumns = @JoinColumn(name = "expediente_id"))
    @OrderColumn(name = "orden")
    private List<DixonDatoExpediente> dixonDatos = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente_etiqueta", joinColumns = @JoinColumn(name = "expediente_id"))
    @OrderColumn(name = "orden")
    @Column(name = "etiqueta", length = 120)
    private List<String> dixonEtiquetas = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente_accion", joinColumns = @JoinColumn(name = "expediente_id"))
    @OrderColumn(name = "orden")
    private List<DixonAccionExpediente> dixonAcciones = new ArrayList<>();

    public boolean dixonEsEntregado() {
        return dixonEstado != null && dixonEstado.contains("Entregado");
    }

    public boolean dixonEsConPagoPendiente() {
        return dixonPago != null && dixonPago.dixonEsPendiente();
    }

    public void dixonPonerAcciones(String... dixonTernas) {
        dixonAcciones.clear();
        for (int dixonIndice = 0; dixonIndice + 2 < dixonTernas.length; dixonIndice += 3) {
            dixonAcciones.add(new DixonAccionExpediente(dixonTernas[dixonIndice], dixonTernas[dixonIndice + 1], dixonTernas[dixonIndice + 2]));
        }
    }

    public void dixonPonerDato(int dixonIndice, String dixonEtiqueta, String dixonValor) {
        while (dixonDatos.size() <= dixonIndice) {
            dixonDatos.add(new DixonDatoExpediente("", ""));
        }
        dixonDatos.set(dixonIndice, new DixonDatoExpediente(dixonEtiqueta, dixonValor));
    }
}
