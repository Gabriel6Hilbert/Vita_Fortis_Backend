package VitaFortis.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
class SecurityCsrfIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void rejeitaMutacaoSemCsrfEAceitaComTokenValido() throws Exception {
        var csrf = csrf();
        var login = mvc.perform(post("/api/v1/auth/login")
                        .session(csrf.session())
                        .header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente@vitafortis.test\",\"senha\":\"Teste@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoUsuario").value("CLIENTE"))
                .andReturn();
        var authenticatedSession = (MockHttpSession) login.getRequest().getSession(false);

        mvc.perform(post("/api/v1/auth/logout").session(authenticatedSession))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/auth/logout")
                        .session(authenticatedSession)
                        .header(csrf.headerName(), csrf.token()))
                .andExpect(status().isNoContent());
    }

    @Test
    void loginERecuperacaoPublicosExigemTokenEContinuamFuncionando() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente@vitafortis.test\",\"senha\":\"Teste@123\"}"))
                .andExpect(status().isForbidden());

        var csrf = csrf();
        mvc.perform(post("/api/v1/auth/recuperacao-senha")
                        .session(csrf.session())
                        .header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-cadastrado@vitafortis.test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void paginasJuridicasSaoPublicasEServidasPeloReact() throws Exception {
        for (String rota : new String[]{
                "/politicas",
                "/politica-de-privacidade",
                "/politica-de-cookies",
                "/termos-de-uso-e-compra",
                "/entrega-e-retirada",
                "/trocas-devolucoes-e-arrependimento"}) {
            mvc.perform(get(rota))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/index.html"));
        }
    }

    private CsrfContext csrf() throws Exception {
        var result = mvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        var body = json.readTree(result.getResponse().getContentAsString());
        return new CsrfContext(
                (MockHttpSession) result.getRequest().getSession(false),
                body.get("headerName").asText(),
                body.get("token").asText());
    }

    private record CsrfContext(MockHttpSession session, String headerName, String token) { }
}
