package VitaFortis.demo.v1.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidoRequestDto {
    private Long usuarioId;
    private List<ItemCompraRequestDto> itens;
    private Long cupomId;
}