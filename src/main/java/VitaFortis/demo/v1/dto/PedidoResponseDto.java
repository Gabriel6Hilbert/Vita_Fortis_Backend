package VitaFortis.demo.v1.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PedidoResponseDto {
    private Long id;
    private Long usuarioId;
    private LocalDateTime dataPedido;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal frete;
    private BigDecimal total;
    private String status;
    private List<ItemCompraResponseDto> itens;
    private String cupomCodigo;
    private String formaRecebimento;
    private String enderecoMascarado;
    private Integer prazoEntregaDias;
    private String formaPagamento;
    private String statusPagamento;
    private String referenciaPagamentoMascarada;
    private List<HistoricoStatusPedidoDto> historicoStatus;
}
