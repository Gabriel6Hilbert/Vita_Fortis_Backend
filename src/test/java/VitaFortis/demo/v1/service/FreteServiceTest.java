package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Endereco;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FreteServiceTest {

    private final FreteService fretes = new FreteService(new BigDecimal("15.00"), 5, "MG,SP,RJ,ES");

    @Test
    void atendeSomenteUfsConfirmadas() {
        for (String uf : new String[]{"MG", "SP", "RJ", "ES"}) {
            assertEquals(new BigDecimal("15.00"), fretes.calcular(endereco(uf), new BigDecimal("200.00")).valor());
        }
        assertThrows(IllegalArgumentException.class,
                () -> fretes.calcular(endereco("PR"), new BigDecimal("250.00")));
    }

    @Test
    void freteGratisExigeValorEstritamenteMaiorQueDuzentos() {
        assertEquals(new BigDecimal("15.00"), fretes.calcular(endereco("MG"), new BigDecimal("200.00")).valor());
        assertEquals(new BigDecimal("0.00"), fretes.calcular(endereco("MG"), new BigDecimal("200.01")).valor());
        assertEquals(5, fretes.calcular(endereco("MG"), new BigDecimal("200.01")).prazoDias());
    }

    private Endereco endereco(String uf) {
        Endereco endereco = new Endereco();
        endereco.setCep("30140071");
        endereco.setUf(uf);
        return endereco;
    }
}
