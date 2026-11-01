package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
@Transactional
class CatalogoFiltrosPaginacaoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProdutoRepository produtos;

    @Test
    void marcaFtwObjetivoPerformanceMantemConteudoContagemEPaginasCoerentes() throws Exception {
        String lote = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        for (int i = 0; i < 25; i++) {
            Produto produto = new Produto();
            produto.setCodigo("PAGE-" + lote + "-" + i);
            produto.setNome("Produto paginação " + lote + " " + i);
            produto.setDescricao("Fixture de filtros combinados");
            produto.setMarca("FTW");
            produto.setCategoria("PROTEINAS");
            produto.setPreco(new BigDecimal("10.00"));
            produto.setQuantidadeEstoque(10);
            produto.setObjetivos(Set.of("PERFORMANCE"));
            produtos.save(produto);
        }
        produtos.flush();

        JsonNode primeira = pagina(0, "FTW", "PERFORMANCE");
        JsonNode segunda = pagina(1, "FTW", "PERFORMANCE");
        assertEquals(primeira.get("totalElements").asLong(), segunda.get("totalElements").asLong());
        assertEquals(primeira.get("totalPages").asInt(), segunda.get("totalPages").asInt());
        assertTrue(primeira.get("totalElements").asLong() >= 25);
        assertTrue(primeira.get("totalPages").asInt() >= 3);
        assertEquals(12, primeira.get("content").size());
        assertEquals(12, segunda.get("content").size());

        Set<Long> ids = new HashSet<>();
        primeira.get("content").forEach(item -> assertTrue(ids.add(item.get("id").asLong())));
        segunda.get("content").forEach(item -> assertTrue(ids.add(item.get("id").asLong())));

        var respostaNormalizada = mvc.perform(get("/api/v1/produtos")
                        .param("marca", "  ftw  ")
                        .param("objetivos", " performance ", "PERFORMANCE")
                        .param("tamanho", "12").param("pagina", "0"))
                .andExpect(status().isOk()).andReturn().getResponse();
        JsonNode normalizada = json.readTree(respostaNormalizada.getContentAsString());
        assertEquals(primeira.get("totalElements").asLong(), normalizada.get("totalElements").asLong());
        JsonNode fora = pagina(999, "FTW", "PERFORMANCE");
        assertEquals(primeira.get("totalElements").asLong(), fora.get("totalElements").asLong());
        assertEquals(0, fora.get("content").size());
    }

    private JsonNode pagina(int pagina, String marca, String objetivos) throws Exception {
        var response = mvc.perform(get("/api/v1/produtos")
                        .param("marca", marca).param("objetivos", objetivos)
                        .param("tamanho", "12").param("pagina", String.valueOf(pagina)))
                .andExpect(status().isOk()).andReturn().getResponse();
        return json.readTree(response.getContentAsString());
    }
}
