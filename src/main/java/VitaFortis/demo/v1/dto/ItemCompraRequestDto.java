package VitaFortis.demo.v1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCompraRequestDto {
    private Long produtoId;
    private int quantidade;
}
