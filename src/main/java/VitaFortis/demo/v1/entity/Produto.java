package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "Produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private BigDecimal preco;
    private int quantidadeEstoque;

    @Enumerated(EnumType.STRING)
    private CategoriaProduto categoria;

    private boolean ativo = true;
    private boolean resgatavel;
    private Integer pontosNecessarios;
}
