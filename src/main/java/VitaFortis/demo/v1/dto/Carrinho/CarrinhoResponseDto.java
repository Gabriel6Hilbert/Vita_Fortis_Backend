package VitaFortis.demo.v1.dto.Carrinho;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CarrinhoResponseDto {

    private Long id;
    private Long usuarioId;
    private List<CarrinhoItemResponseDto> carrinhoItens;
}
