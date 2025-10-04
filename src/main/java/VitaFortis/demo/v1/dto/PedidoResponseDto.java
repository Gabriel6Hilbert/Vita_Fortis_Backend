package VitaFortis.demo.v1.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PedidoResponseDto {
    private Long id;
    private Long usuarioId;
    private LocalDateTime dataPedido;
    private double total;
    private String status;
    private int pontosGerados;
    private List<ItemCompraResponseDto> itens;
    private String cupomCodigo;
}
