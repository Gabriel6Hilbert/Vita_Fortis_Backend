package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CupomRegrasService {
    private final PedidoRepository pedidos;
    public CupomRegrasService(PedidoRepository pedidos) { this.pedidos = pedidos; }
    public void validar(Cupom cupom, BigDecimal subtotal) {
        var agora = LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"));
        if (!cupom.isAtivo()) throw new IllegalArgumentException("Cupom inativo");
        if (cupom.getDataInicio() != null && agora.isBefore(cupom.getDataInicio())) throw new IllegalArgumentException("Cupom ainda nao esta valido");
        if (cupom.getDataVencimento() != null && !agora.isBefore(cupom.getDataVencimento())) throw new IllegalArgumentException("Cupom expirado");
        if (cupom.getMinSubtotal() != null && subtotal.compareTo(cupom.getMinSubtotal()) < 0) throw new IllegalArgumentException("Subtotal minimo do cupom nao atingido");
        // Cada pedido confirmado no checkout consome uma utilizacao, mesmo se depois cancelado.
        if (cupom.getLimiteUso() != null && pedidos.countByCupomUtilizadoId(cupom.getId()) >= cupom.getLimiteUso())
            throw new IllegalArgumentException("Limite de uso do cupom atingido");
    }
}
