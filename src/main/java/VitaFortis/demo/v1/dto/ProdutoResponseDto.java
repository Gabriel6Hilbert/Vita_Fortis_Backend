package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoResponseDto {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private int quantidadeEstoque;
    private CategoriaProduto categoria;
    private boolean ativo;
    private boolean resgatavel;
    private Integer pontosNecessarios;
}
