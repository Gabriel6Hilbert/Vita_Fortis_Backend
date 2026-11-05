package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.TipoMovimentoCashback;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ColaboradorRelatorioDto(List<Lancamento> movimentos, List<CupomVinculado> cupons) {
    public record Lancamento(Long id, LocalDateTime criadoEm, TipoMovimentoCashback tipo, BigDecimal valor) {}
    public record CupomVinculado(String codigo, boolean ativo, BigDecimal percentualCashback) {}
}
