package VitaFortis.demo.v1.integration;

import java.math.BigDecimal;

public interface PagamentoGateway {
    PagamentoPendente iniciar(Long pedidoId, BigDecimal total, String formaPagamento);

    record PagamentoPendente(String referencia) {
    }
}
