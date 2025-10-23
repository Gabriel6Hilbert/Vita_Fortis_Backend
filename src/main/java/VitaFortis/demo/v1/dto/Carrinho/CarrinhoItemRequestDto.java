package VitaFortis.demo.v1.dto.Carrinho;

import VitaFortis.demo.v1.repository.OnCreate;
import VitaFortis.demo.v1.repository.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CarrinhoItemRequestDto {

    @NotNull(groups = OnCreate.class)
    private Long produtoId;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Min(value = 1, groups = OnCreate.class)
    private int quantidade;

    @NotNull(groups = OnUpdate.class)
    private Long itemId;

}
