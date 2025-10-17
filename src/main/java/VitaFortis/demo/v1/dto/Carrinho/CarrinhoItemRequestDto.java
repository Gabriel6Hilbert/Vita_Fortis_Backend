package VitaFortis.demo.v1.dto.Carrinho;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CarrinhoItemRequestDto {
    @NotNull
    private Long produtoId;

    @Min(1)
    private int quantidade;
}
