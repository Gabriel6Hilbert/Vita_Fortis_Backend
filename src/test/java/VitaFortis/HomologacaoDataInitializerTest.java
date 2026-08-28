package VitaFortis;

import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("homologacao")
class HomologacaoDataInitializerTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired ProdutoRepository produtos;
    @Autowired CupomRepository cupons;

    @Test void criaCenarioIdempotenteDeHomologacao() {
        assertTrue(usuarios.existsByEmail("admin@vitafortis.test"));
        assertTrue(usuarios.existsByEmail("colaborador@vitafortis.test"));
        assertTrue(usuarios.existsByEmail("cliente@vitafortis.test"));
        assertTrue(produtos.existsByCodigoIgnoreCase("HML-WHEY"));
        assertTrue(produtos.existsByCodigoIgnoreCase("HML-ZERO"));
        assertTrue(cupons.existsByCodigoIgnoreCase("COLAB10"));
    }
}
