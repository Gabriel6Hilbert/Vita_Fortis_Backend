package VitaFortis.demo.v1.dto.Carrinho;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CarrinhoItemResponseDto {

    private Long itemId;
    private Long produtoId;
    private String produtoNome;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
