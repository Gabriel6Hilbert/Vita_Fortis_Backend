package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Endereco;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FreteService {

    private final BigDecimal valorFixo;
    private final int prazoPadraoDias;

    public FreteService(
            @Value("${vita-fortis.checkout.frete-fixo:15.00}") BigDecimal valorFixo,
            @Value("${vita-fortis.checkout.prazo-padrao-dias:5}") int prazoPadraoDias) {
        this.valorFixo = valorFixo.setScale(2, RoundingMode.HALF_UP);
        this.prazoPadraoDias = prazoPadraoDias;
    }

    public CotacaoFrete calcular(Endereco endereco) {
        if (endereco == null || endereco.getCep() == null) {
            throw new IllegalArgumentException("Endereco obrigatorio para entrega");
        }
        return new CotacaoFrete(valorFixo, prazoPadraoDias);
    }

    public record CotacaoFrete(BigDecimal valor, int prazoDias) {
    }
}
