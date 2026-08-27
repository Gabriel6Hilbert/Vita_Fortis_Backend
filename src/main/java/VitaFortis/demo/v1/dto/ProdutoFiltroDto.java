package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.enums.ProdutoOrdenacao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter @Setter
public class ProdutoFiltroDto {

    private String nome;
    private String busca;
    private String marca;
    private String descricao;
    private BigDecimal precoMin;
    private BigDecimal precoMax;
    private CategoriaProduto categoria;
    private Boolean resgatavel;
    private Set<String> objetivos;
    private Set<String> esportes;
    private Boolean vegano;
    private Boolean vegetariano;
    private Boolean linhaClinica;
    private Boolean lancamento;
    private String subcategoria;
    private Boolean oferta;
    private Boolean emEstoque;
    private BigDecimal descontoMin;
    private Boolean maisVendidos;
    private ProdutoOrdenacao ordenacao;
    private Integer pagina;
    private Integer tamanho;

}
