package VitaFortis.demo.v1.dto.Carrinho;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarrinhoItemResponseDto {

    private Long id;
    private Long produtoId;
    private String produtoNome;
    private int quantidade;
}
