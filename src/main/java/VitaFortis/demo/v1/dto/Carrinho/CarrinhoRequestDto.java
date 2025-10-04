package VitaFortis.demo.v1.dto.Carrinho;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CarrinhoRequestDto {

    private Long usuarioId;
    private List<CarrinhoItemRequestDto> carrinhoItens;
}
