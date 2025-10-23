package VitaFortis.demo.v1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "CARRINHO_ITEM",
        uniqueConstraints = @UniqueConstraint(name = "UK_ITEM_CARRINHO_PRODUTO", columnNames = {"CARRINHO_ID","PRODUTO_ID"}),
        indexes = {
                @Index(name="IX_ITEM_CARRINHO", columnList="CARRINHO_ID"),
                @Index(name="IX_ITEM_PRODUTO", columnList="PRODUTO_ID")
        }
)
public class CarrinhoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARRINHO_ITEM_ID")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CARRINHO_ID", nullable = false)
    private Carrinho carrinho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRODUTO_ID", nullable = false)
    private Produto produto;

    @Min(1)
    @Column(name = "QUANTIDADE", nullable = false)
    private int quantidade;

    @Column(name = "PRECO_UNITARIO", precision = 19, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    @Version
    @Column(name = "VERSAO")
    private Long versao;

    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM", nullable = false)
    private LocalDateTime atualizadoEm;

    @JsonIgnore
    public BigDecimal getTotalItem() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public void incrementar(int delta) {
        setQuantidade(Math.max(1, this.quantidade + delta));
    }

    public void setQuantidade(int q) {
        if (q < 1) throw new IllegalArgumentException("Quantidade deve ser >= 1");
        this.quantidade = q;
    }

    @PrePersist
    void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    @PreUpdate
    void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
