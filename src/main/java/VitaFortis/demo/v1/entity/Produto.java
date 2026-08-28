package VitaFortis.demo.v1.entity;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "PRODUTO", indexes = {
        @Index(name = "IX_PRODUTO_NOME", columnList = "NOME"),
        @Index(name = "IX_PRODUTO_CODIGO", columnList = "CODIGO", unique = true)
})
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUTO_ID")
    private Long id;

    @Size(max = 60)
    @Column(name = "CODIGO", length = 60, unique = true)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    @Size(max = 1000)
    @Column(name = "DESCRICAO", length = 1000)
    private String descricao;

    @Size(max = 120)
    @Column(name = "MARCA", length = 120)
    private String marca;

    @Size(max = 20)
    @Column(name = "UNIDADE", length = 20)
    private String unidade = "UN";

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @Column(name = "PRECO", nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Digits(integer = 3, fraction = 2)
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(name = "DESC_PERCENT", precision = 5, scale = 2)
    private BigDecimal descontoPercentual;

    @Digits(integer = 10, fraction = 2)
    @DecimalMin(value = "0.00")
    @Column(name = "DESC_VALOR", precision = 12, scale = 2)
    private BigDecimal descontoValor;

    @Min(0)
    @Column(name = "QTD_ESTOQUE", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIA", nullable = false, length = 40)
    private CategoriaProduto categoria;

    @Column(name = "ATIVO", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean ativo = true;

    @Version
    @Column(name = "VERSAO")
    private Long versao;

    @Size(max = 500)
    @Column(name = "IMAGEM_URL", length = 500)
    private String imagemUrl;

    @Size(max = 250)
    @Column(name = "IMAGEM_CONTENT_TYPE")
    private String imagemContentType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUTO_OBJETIVO", joinColumns = @JoinColumn(name = "PRODUTO_ID"))
    @Column(name = "OBJETIVO", length = 50)
    private Set<String> objetivos = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUTO_ESPORTE", joinColumns = @JoinColumn(name = "PRODUTO_ID"))
    @Column(name = "ESPORTE", length = 50)
    private Set<String> esportes = new HashSet<>();

    @Column(name = "VEGANO", nullable = false) private boolean vegano;
    @Column(name = "VEGETARIANO", nullable = false) private boolean vegetariano;
    @Column(name = "LINHA_CLINICA", nullable = false) private boolean linhaClinica;
    @Column(name = "LANCAMENTO", nullable = false) private boolean lancamento;
    @Size(max = 60) @Column(name = "SUBCATEGORIA", length = 60) private String subcategoria;
    @Digits(integer = 1, fraction = 2) @DecimalMin("0.0") @DecimalMax("5.0")
    @Column(name = "AVALIACAO_MEDIA", precision = 3, scale = 2) private BigDecimal avaliacaoMedia;
    @Min(0) @Column(name = "TOTAL_VENDIDO", nullable = false) private long totalVendido;



    /* Regras de consistência */
    @PrePersist
    private void beforeInsert() {
       normalize();
    }

    @PreUpdate
    private void beforeUpdate() {
        normalize();
    }

    @Transient
    public BigDecimal getValorDesconto() {
        if (preco == null) return null;
        BigDecimal base = money(preco);

        if (descontoPercentual != null) {
            BigDecimal perc = descontoPercentual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_EVEN);
            return money(base.multiply(perc));
        }
        if (descontoValor != null) {
            BigDecimal desc = money(descontoValor);
            if (desc.compareTo(base) > 0) desc = base;
            if (desc.signum() < 0) desc = BigDecimal.ZERO;
            return desc;
        }
        return BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getPrecoFinal() {
        if (preco == null) return null;
        return money(preco).subtract(getValorDesconto()).max(BigDecimal.ZERO);
    }

    private void normalize () {
        if (codigo != null) codigo = codigo.trim().toUpperCase();
        if (marca != null) marca = marca.trim();
        if (unidade == null || unidade.isBlank()) unidade = "UN";
        else unidade = unidade.trim().toUpperCase();
        if (preco != null) preco = money(preco);
        if (descontoPercentual != null) {
            BigDecimal dp = descontoPercentual;
            if (dp.signum() < 0) dp = BigDecimal.ZERO;
            if (dp.compareTo(new BigDecimal("100.00")) > 0) dp = new BigDecimal("100");
            descontoPercentual = dp.setScale(2, RoundingMode.HALF_UP);
        }
        if (descontoValor != null) {
            BigDecimal dv = money(descontoValor);
            if (dv.signum() < 0) dv = BigDecimal.ZERO;
            descontoValor = dv;
        }
    }

    private static BigDecimal money(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

}
