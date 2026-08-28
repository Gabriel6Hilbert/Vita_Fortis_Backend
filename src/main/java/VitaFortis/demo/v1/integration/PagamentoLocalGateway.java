package VitaFortis.demo.v1.integration;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PagamentoLocalGateway implements PagamentoGateway {

    @Override
    public PagamentoPendente iniciar(Long pedidoId, BigDecimal total, String formaPagamento) {
        return new PagamentoPendente("LOCAL-" + UUID.randomUUID());
    }
}
