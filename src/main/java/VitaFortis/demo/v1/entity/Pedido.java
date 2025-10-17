package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.entity.ItemCompra;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.StatusCompra;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "PEDIDO")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PEDIDO_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="USUARIO_ID", nullable=false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "DATA_PEDIDO", nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @NotNull @Digits(integer=10, fraction=2)
    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 30)
    private StatusCompra status;

    @Min(0)
    @Column(name = "PONTOS_GERADOS", nullable = false)
    private int pontosGerados = 0;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ItemCompra> items;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUPOM_ID")
    private Cupom cupomUtilizado;

    @PrePersist @PreUpdate
    private void calcularTotais() {
        if (items != null) {
            this.total = items.stream()
                    .map(ItemCompra::getSubtotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (this.pontosGerados < 0) this.pontosGerados = 0;
    }
}
