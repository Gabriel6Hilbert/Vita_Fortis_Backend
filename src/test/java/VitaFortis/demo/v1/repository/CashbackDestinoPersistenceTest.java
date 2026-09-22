package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.DestinoCashback;
import VitaFortis.demo.v1.enums.TipoUsuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CashbackDestinoPersistenceTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired EntityManager entityManager;

    @Test void persisteEscolhaEntrePixEDesconto() {
        Usuario colaborador = new Usuario();
        colaborador.setNome("Colaborador Cashback"); colaborador.setEmail("destino@teste.com");
        colaborador.setSenha("123456789012345678901234567890123456789012345678901234567890");
        colaborador.setCpf("12345678901"); colaborador.setTipo(TipoUsuario.COLABORADOR);
        colaborador.setSaldoCashback(BigDecimal.TEN); colaborador.setDestinoCashback(DestinoCashback.PIX);
        Long id = usuarios.saveAndFlush(colaborador).getId();
        entityManager.clear();
        assertEquals(DestinoCashback.PIX, usuarios.findById(id).orElseThrow().getDestinoCashback());

        Usuario recarregado = usuarios.findById(id).orElseThrow();
        recarregado.setDestinoCashback(DestinoCashback.DESCONTO); usuarios.saveAndFlush(recarregado);
        entityManager.clear();
        assertEquals(DestinoCashback.DESCONTO, usuarios.findById(id).orElseThrow().getDestinoCashback());
    }
}
