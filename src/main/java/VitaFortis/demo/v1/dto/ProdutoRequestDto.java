package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoRequestDto {

    @NotBlank
    @Size(max = 60)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    private String nome;

    @NotBlank @Size(max = 1000)
    private String descricao;

    @Size(max = 120)
    private String marca;

    @Size(max = 20)
    private String unidade;

    @NotNull @Digits(integer = 10, fraction = 2)
    private BigDecimal preco;

    @NotNull @Min(0)
    private Integer quantidadeEstoque;

    @NotNull
    private CategoriaProduto categoria;

    @Size(max = 500)
    private String imagemUrl;

}
