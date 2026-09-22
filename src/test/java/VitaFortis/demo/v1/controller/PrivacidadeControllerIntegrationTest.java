package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.repository.AuditoriaSolicitacaoPrivacidadeRepository;
import VitaFortis.demo.v1.repository.SolicitacaoPrivacidadeRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrivacidadeControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired SolicitacaoPrivacidadeRepository solicitacoes;
    @Autowired AuditoriaSolicitacaoPrivacidadeRepository auditorias;
    @Autowired PasswordEncoder passwordEncoder;

    private MockHttpSession sessaoUm;
    private MockHttpSession sessaoDois;

    @BeforeEach
    void preparar() throws Exception {
        sessaoUm = login(criarCliente("cliente.rcp02.1@teste.local", "11111111111"));
        sessaoDois = login(criarCliente("cliente.rcp02.2@teste.local", "22222222222"));
    }

    @Test
    void persisteConsentimentoESolicitacaoComCamposObrigatorios() throws Exception {
        mvc.perform(put("/api/v1/privacidade/consentimento-comunicacoes").session(sessaoUm)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aceitaComunicacoes\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.aceitaComunicacoes").value(true));

        mvc.perform(post("/api/v1/privacidade/solicitacoes").session(sessaoUm)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"CORRECAO\",\"descricao\":\"Corrigir meu telefone cadastrado.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocolo").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("CORRECAO"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mvc.perform(get("/api/v1/privacidade/solicitacoes").session(sessaoUm))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao").value("Corrigir meu telefone cadastrado."));

        Usuario persistido = usuarios.findByEmail("cliente.rcp02.1@teste.local").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(persistido.isAceitaComunicacoes());
        org.junit.jupiter.api.Assertions.assertEquals(1, solicitacoes.findAllByUsuarioIdOrderByCriadaEmDesc(persistido.getId()).size());
    }

    @Test
    void isolaSolicitacoesEntreClientesERecusaAcessoDireto() throws Exception {
        long id = abrir(sessaoUm, "ACESSO", "Quero uma cópia dos meus dados.");

        mvc.perform(get("/api/v1/privacidade/solicitacoes").session(sessaoDois))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/v1/privacidade/solicitacoes/{id}", id).session(sessaoDois))
                .andExpect(status().isForbidden());
    }

    @Test
    void validaEntradaEProtegeRotasAdministrativas() throws Exception {
        mvc.perform(post("/api/v1/privacidade/solicitacoes").session(sessaoUm)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"EXCLUSAO\",\"descricao\":\" \"}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/v1/admin/privacidade/solicitacoes").session(sessaoUm))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/privacidade/solicitacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorProcessaEGravaAuditoriaSemExcluirDadosDoCliente() throws Exception {
        long id = abrir(sessaoUm, "EXCLUSAO", "Solicito exclusão dos dados que puderem ser eliminados.");
        long quantidadeUsuarios = usuarios.count();

        mvc.perform(patch("/api/v1/admin/privacidade/solicitacoes/{id}", id)
                        .with(user("admin.rcp02@teste.local").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NEGADA\",\"decisao\":\"Retenção parcial obrigatória\",\"justificativa\":\"Pedidos e pagamentos devem ser preservados durante o prazo legal aplicável.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEGADA"))
                .andExpect(jsonPath("$.responsavel").value("admin.rcp02@teste.local"))
                .andExpect(jsonPath("$.processadaEm").isNotEmpty());

        var trilha = auditorias.findAllBySolicitacaoIdOrderByCriadaEmAsc(id);
        org.junit.jupiter.api.Assertions.assertEquals(1, trilha.size());
        org.junit.jupiter.api.Assertions.assertEquals("admin.rcp02@teste.local", trilha.get(0).getResponsavel());
        org.junit.jupiter.api.Assertions.assertEquals(quantidadeUsuarios, usuarios.count());
    }

    private Usuario criarCliente(String email, String cpf) {
        Usuario usuario = new Usuario();
        usuario.setNome("Cliente RCP 02");
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setSenha(passwordEncoder.encode("Senha123!"));
        usuario.setTipo(TipoUsuario.CLIENTE);
        usuario.setAtivo(true);
        return usuarios.save(usuario);
    }

    private MockHttpSession login(Usuario usuario) throws Exception {
        var result = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("email", usuario.getEmail(), "senha", "Senha123!"))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private long abrir(MockHttpSession sessao, String tipo, String descricao) throws Exception {
        var result = mvc.perform(post("/api/v1/privacidade/solicitacoes").session(sessao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("tipo", tipo, "descricao", descricao))))
                .andExpect(status().isCreated()).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }
}
