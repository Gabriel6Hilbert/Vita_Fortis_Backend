package VitaFortis.demo.v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidoRequestDto {

    @NotNull
    private Long usuarioId;

    @NotEmpty
    @Valid
    private List<ItemCompraRequestDto> itens;

    private Long cupomId;
}