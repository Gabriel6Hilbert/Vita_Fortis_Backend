package VitaFortis.demo.v1.dto;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime; import java.util.List; import java.util.Map;
public record MetricasAdminDto(BigDecimal faturamento, long pedidos, BigDecimal ticketMedio, long produtosVendidos,
 long clientes, BigDecimal comparacaoFaturamentoPercentual, List<PontoSerieDto> serieDiaria, List<PontoSerieDto> serieMensal,
 Map<String,Long> categorias, Map<String,Long> status, List<ProdutoVendidoDto> maisVendidos, List<VendaRecenteDto> vendasRecentes) {
 public record PontoSerieDto(String periodo, BigDecimal faturamento, long pedidos){}
 public record ProdutoVendidoDto(Long produtoId,String nome,long quantidade,BigDecimal faturamento){}
 public record VendaRecenteDto(Long pedidoId,LocalDateTime data,String cliente,BigDecimal total,String status){}
}
