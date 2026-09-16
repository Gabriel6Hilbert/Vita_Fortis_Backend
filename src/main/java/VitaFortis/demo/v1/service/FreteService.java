package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Endereco;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FreteService {

    private final BigDecimal valorFixo;
    private final int prazoPadraoDias;
    private final Set<String> ufsAtendidas;

    public FreteService(
            @Value("${vita-fortis.checkout.frete-fixo:15.00}") BigDecimal valorFixo,
            @Value("${vita-fortis.checkout.prazo-padrao-dias:5}") int prazoPadraoDias,
            @Value("${vita-fortis.checkout.ufs-atendidas:SP}") String ufsAtendidas) {
        this.valorFixo = valorFixo.setScale(2, RoundingMode.HALF_UP);
        this.prazoPadraoDias = prazoPadraoDias;
        this.ufsAtendidas = Arrays.stream(ufsAtendidas.split(","))
                .map(String::trim).filter(v -> !v.isEmpty()).map(v -> v.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public CotacaoFrete calcular(Endereco endereco) {
        if (endereco == null || endereco.getCep() == null) {
            throw new IllegalArgumentException("Endereco obrigatorio para entrega");
        }
        String uf = endereco.getUf() == null ? "" : endereco.getUf().trim().toUpperCase(Locale.ROOT);
        if (!ufsAtendidas.contains("*") && !ufsAtendidas.contains(uf)) {
            throw new IllegalArgumentException("Regiao ainda nao atendida para entrega: " + uf);
        }
        return new CotacaoFrete(valorFixo, prazoPadraoDias);
    }

    public record CotacaoFrete(BigDecimal valor, int prazoDias) {
    }
}
