package VitaFortis.demo.v1.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemCompraResponseDto {
    private Long id;
    private Long produtoId;
    private String produtoNome;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
