package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.enums.ProdutoOrdenacao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProdutoFiltroDto {

    private String nome;
    private String descricao;
    private BigDecimal precoMin;
    private BigDecimal precoMax;
    private CategoriaProduto categoria;
    private Boolean resgatavel;
    private ProdutoOrdenacao ordenacao;
    private Integer pagina;
    private Integer tamanho;

}
