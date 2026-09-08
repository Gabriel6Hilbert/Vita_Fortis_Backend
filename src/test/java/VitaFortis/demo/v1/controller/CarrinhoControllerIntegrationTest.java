package VitaFortis.demo.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
class CarrinhoControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void limparCarrinhoRemoveOsItensDaResposta() throws Exception {
        var login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente@vitafortis.test\",\"senha\":\"Teste@123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        var session = (MockHttpSession) login.getRequest().getSession(false);
        long userId = json.readTree(login.getResponse().getContentAsString()).get("id").asLong();
        var product = mvc.perform(get("/api/v1/produtos").param("busca", "HML-WHEY"))
                .andExpect(status().isOk())
                .andReturn();
        long productId = json.readTree(product.getResponse().getContentAsString())
                .get("content").get(0).get("id").asLong();

        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", userId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":" + productId + ",\"quantidade\":1}"))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/v1/carrinhos/{usuarioId}/itens", userId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrinhoItens").isEmpty())
                .andExpect(jsonPath("$.subtotal").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }
}
