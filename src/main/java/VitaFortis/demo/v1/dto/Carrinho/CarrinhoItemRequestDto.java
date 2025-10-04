package VitaFortis.demo.v1.dto.Carrinho;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarrinhoItemRequestDto {

    private Long produtoId;
    private int quantidade;
}
