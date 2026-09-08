package VitaFortis;

import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static VitaFortis.demo.v1.enums.TipoUsuario.ADMIN;
import static VitaFortis.demo.v1.enums.TipoUsuario.COLABORADOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertEquals(ADMIN, usuarios.findByEmail("admin@vitafortis.test").orElseThrow().getTipo());
        assertEquals(COLABORADOR, usuarios.findByEmail("colaborador@vitafortis.test").orElseThrow().getTipo());
        assertFalse(produtos.findByCodigoIgnoreCase("HML-INATIVO").orElseThrow().isAtivo());
    }
}
