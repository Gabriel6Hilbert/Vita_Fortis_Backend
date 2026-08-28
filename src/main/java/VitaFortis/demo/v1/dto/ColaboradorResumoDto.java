package VitaFortis.demo.v1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ColaboradorResumoDto(
        Long colaboradorId,
        BigDecimal saldo,
        BigDecimal cashbackConfirmado,
        BigDecimal cashbackEstornado,
        BigDecimal vendasGeradas,
        long pedidosGerados,
        List<CupomDesempenhoDto> cupons,
        List<MovimentoDto> movimentos,
        List<PedidoResponseDto> pedidos) {
    public record CupomDesempenhoDto(Long id, String codigo, boolean ativo, BigDecimal percentualCashback,
                                      long pedidos, BigDecimal vendas, BigDecimal cashback) {}
    public record MovimentoDto(Long id, String tipo, BigDecimal valor, BigDecimal saldoAnterior,
                                BigDecimal saldoNovo, String justificativa, Long pedidoId,
                                LocalDateTime criadoEm) {}
}
