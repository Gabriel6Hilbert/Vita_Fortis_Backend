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
    @Size(max = 120)
    private String nome;

    @NotBlank @Size(max = 1000)
    private String descricao;

    @NotNull @Digits(integer = 10, fraction = 2)
    private BigDecimal preco;

    @NotNull @Min(0)
    private Integer quantidadeEstoque;

    @NotNull
    private CategoriaProduto categoria;

    private boolean resgatavel;

    @Min(0)
    private Integer pontosNecessarios;

    @AssertTrue(message = "Quando resgatável, 'pontosNecessarios' é obrigatório")
    public boolean isPontosValidos() {
        return !resgatavel || pontosNecessarios != null;
    }

}
