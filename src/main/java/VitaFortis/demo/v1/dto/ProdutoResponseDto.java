package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
public class ProdutoResponseDto {

    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private String marca;
    private String unidade;
    private BigDecimal preco;
    private BigDecimal valorDesconto;
    private BigDecimal precoFinal;
    private BigDecimal descontoValor;
    private BigDecimal descontoPercentual;
    private int quantidadeEstoque;
    private CategoriaProduto categoria;
    private boolean ativo;
    private boolean resgatavel;
    private Integer pontosNecessarios;
    private String imagemUrl;
    private Set<String> objetivos;
    private Set<String> esportes;
    private boolean vegano;
    private boolean vegetariano;
    private boolean linhaClinica;
    private boolean lancamento;
    private String subcategoria;
    private BigDecimal avaliacaoMedia;
    private long totalVendido;
}
