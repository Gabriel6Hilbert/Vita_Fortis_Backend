package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import VitaFortis.demo.v1.enums.StatusPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @NotNull
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @NotNull
    @Column(name = "DESCONTO", nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @NotNull
    @Column(name = "FRETE", nullable = false, precision = 12, scale = 2)
    private BigDecimal frete = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ItemCompra> items;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUPOM_ID")
    private Cupom cupomUtilizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "FORMA_RECEBIMENTO", nullable = false, length = 20)
    private FormaRecebimento formaRecebimento;

    @Size(max = 300)
    @Column(name = "ENDERECO_ENTREGA", length = 300)
    private String enderecoEntrega;

    @Column(name = "PRAZO_ENTREGA_DIAS")
    private Integer prazoEntregaDias;

    @Size(max = 40)
    @Column(name = "FORMA_PAGAMENTO", nullable = false, length = 40)
    private String formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_PAGAMENTO", nullable = false, length = 20)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    @Size(max = 100)
    @Column(name = "REFERENCIA_PAGAMENTO", length = 100)
    private String referenciaPagamento;

    // Compatibilidade com bancos criados antes da remoção do programa de fidelidade.
    // O campo não participa das regras atuais de venda e permanece sempre zerado.
    @Column(name = "PONTOS_GERADOS", nullable = false)
    private int legadoPontosGerados;

    @OneToMany(mappedBy="pedido", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    @OrderBy("data ASC") private Set<HistoricoStatusPedido> historicoStatus = new LinkedHashSet<>();

    @PrePersist @PreUpdate
    private void calcularTotais() {
        if (total == null && items != null) {
            this.total = items.stream()
                    .map(ItemCompra::getSubtotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (desconto == null) desconto = BigDecimal.ZERO;
        if (frete == null) frete = BigDecimal.ZERO;
    }
}
