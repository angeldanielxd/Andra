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
@EqualsAndHashCode(of = "andraId")
public class AndraExpediente {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 30)
    private String andraCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private AndraPersona andraPersona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private AndraSede andraSede;

    @Enumerated(EnumType.STRING)
    @Column(name = "clase", nullable = false, length = 6)
    private AndraClaseExpediente andraClase;

    @ManyToOne
    @JoinColumn(name = "tramite_id")
    private AndraTramite andraTramite;

    @ManyToOne
    @JoinColumn(name = "pago_id")
    private AndraPago andraPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "grupo", nullable = false, length = 12)
    private AndraGrupoExpediente andraGrupo;

    @Column(name = "icono", nullable = false, length = 20)
    private String andraIcono;

    @Column(name = "rotulo_icono", nullable = false, length = 20)
    private String andraRotuloIcono;

    @Column(name = "tono", nullable = false, length = 10)
    private String andraTono;

    @Column(name = "estado", nullable = false, length = 100)
    private String andraEstado;

    @Column(name = "titulo", nullable = false, length = 200)
    private String andraTitulo;

    @Column(name = "descripcion", length = 400)
    private String andraDescripcion;

    @Column(name = "paso", nullable = false)
    private int andraPaso;

    @Column(name = "observacion", length = 500)
    private String andraObservacion;

    @Column(name = "destacado", nullable = false)
    private boolean andraDestacado;

    @Column(name = "nota", length = 200)
    private String andraNota;

    @Embedded
    private AndraDatosSolicitante andraSolicitante;

    @Column(name = "fecha")
    private LocalDateTime andraFecha;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente_dato", joinColumns = @JoinColumn(name = "expediente_id"))
    @OrderColumn(name = "orden")
    private List<AndraDatoExpediente> andraDatos = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente_etiqueta", joinColumns = @JoinColumn(name = "expediente_id"))
    @OrderColumn(name = "orden")
    @Column(name = "etiqueta", length = 120)
    private List<String> andraEtiquetas = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente_accion", joinColumns = @JoinColumn(name = "expediente_id"))
    @OrderColumn(name = "orden")
    private List<AndraAccionExpediente> andraAcciones = new ArrayList<>();

    public boolean andraEsEntregado() {
        return andraEstado != null && andraEstado.contains("Entregado");
    }

    public boolean andraEsConPagoPendiente() {
        return andraPago != null && andraPago.andraEsPendiente();
    }

    public void andraPonerAcciones(String... andraTernas) {
        andraAcciones.clear();
        for (int andraIndice = 0; andraIndice + 2 < andraTernas.length; andraIndice += 3) {
            andraAcciones.add(new AndraAccionExpediente(andraTernas[andraIndice], andraTernas[andraIndice + 1], andraTernas[andraIndice + 2]));
        }
    }

    public void andraPonerDato(int andraIndice, String andraEtiqueta, String andraValor) {
        while (andraDatos.size() <= andraIndice) {
            andraDatos.add(new AndraDatoExpediente("", ""));
        }
        andraDatos.set(andraIndice, new AndraDatoExpediente(andraEtiqueta, andraValor));
    }
}
