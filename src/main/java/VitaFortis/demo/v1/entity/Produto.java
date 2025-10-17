package VitaFortis.demo.v1.entity;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "PRODUTO", indexes = {
        @Index(name = "IX_PRODUTO_NOME", columnList = "NOME")
})
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUTO_ID")
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    @Size(max = 1000)
    @Column(name = "DESCRICAO", length = 1000)
    private String descricao;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @Column(name = "PRECO", nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

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

    @Column(name = "RESGATAVEL", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean resgatavel = false;

    @Min(0)
    @Column(name = "PONTOS_NECESSARIOS")
    private Integer pontosNecessarios;

    /* Regras de consistência */
    @PrePersist
    @PreUpdate
    private void validarResgate() {
        if (!resgatavel) {
            pontosNecessarios = null;
        }
    }

}