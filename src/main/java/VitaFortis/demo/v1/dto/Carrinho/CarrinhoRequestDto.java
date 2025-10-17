package VitaFortis.demo.v1.dto.Carrinho;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CarrinhoRequestDto {
    @NotNull
    private Long usuarioId;
    @Valid
    private List<CarrinhoItemRequestDto> carrinhoItens;
}
