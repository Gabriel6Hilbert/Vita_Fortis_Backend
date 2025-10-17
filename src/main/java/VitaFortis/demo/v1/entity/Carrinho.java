package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.print.attribute.standard.MediaSize;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(
        name="CARRINHO",
        uniqueConstraints = @UniqueConstraint(name = "UK_CARRINHO_USUARIO", columnNames = "USUARIO_ID")
)
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARRINHO_ID")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @OneToMany(
            mappedBy = "carrinho",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CarrinhoItem> itens = new ArrayList<>();

    @Column(name = "ATIVO", nullable = false)
    private boolean ativo = true;

    @Column(name = "SUBTOTAL", precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "DESCONTOS", precision = 19, scale = 2, nullable = false)
    private BigDecimal descontos = BigDecimal.ZERO;

    @Column(name = "TOTAL", precision = 19, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Version
    @Column(name = "VERSAO")
    private Long versao;

    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM", nullable = false)
    private LocalDateTime atualizadoEm;

    public void addItem(CarrinhoItem item) {
        Objects.requireNonNull(item, "Item não pode ser nulo");
        item.setCarrinho(this);
        this.itens.add(item);
    }

    public void removeItem(CarrinhoItem item) {
        if (item != null) {
            this.itens.remove(item);
            item.setCarrinho(null);
        }
    }

    public void recalcularTotais() {
        BigDecimal novoSubtotal = itens.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotal = novoSubtotal;
        this.descontos = this.descontos == null ? BigDecimal.ZERO : this.descontos.max(BigDecimal.ZERO);
        this.total = novoSubtotal.subtract(this.descontos).max(BigDecimal.ZERO);
    }

    @PrePersist
    void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
        if (this.descontos == null) this.descontos = BigDecimal.ZERO;
        if (this.subtotal == null)  this.subtotal  = BigDecimal.ZERO;
        if (this.total == null)     this.total     = BigDecimal.ZERO;
    }

    @PreUpdate
    void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

}