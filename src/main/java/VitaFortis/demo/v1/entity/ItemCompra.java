package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "ITENS_COMPRA")
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long itemId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRODUTO_ID", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    private Pedido pedido;

    @Min(1)
    @Column(name = "QUANTIDADE", nullable = false)
    private int quantidade;

    @NotNull
    @Column(name = "PRECO_UNITARIO", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @PrePersist @PreUpdate
    private void calcularSubtotal() {
        if (precoUnitario == null) return;
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
