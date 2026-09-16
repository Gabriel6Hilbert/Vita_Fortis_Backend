package VitaFortis.demo.v1.dto;

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
    private String categoria;
    private java.util.Map<@jakarta.validation.constraints.Size(max=40) String, @jakarta.validation.constraints.Size(max=255) String> atributos;
    private boolean ativo;
    private String imagemUrl;
    private Set<String> objetivos;
    private Set<String> esportes;
    private boolean vegano;
    private boolean vegetariano;
    private boolean linhaClinica;
    private boolean lancamento;
    private boolean destaque;
    private boolean oferta;
    private boolean kit;
    private String subcategoria;
    private BigDecimal avaliacaoMedia;
    private long totalVendido;
}
