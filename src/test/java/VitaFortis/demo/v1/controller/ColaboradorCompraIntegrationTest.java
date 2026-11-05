package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import VitaFortis.demo.v1.service.CashbackService;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
class ColaboradorCompraIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProdutoRepository produtos;
    @Autowired UsuarioRepository usuarios;
    @Autowired CupomRepository cupons;
    @Autowired CashbackService cashback;
    @Autowired PasswordEncoder encoder;

    @Test
    void relatorioExigePermissaoAtualEConsultaSomenteOTitularDaSessao() throws Exception {
        var a = colaboradorFicticio("a", "28287953011", "QA-REL-A", "1.25");
        var b = colaboradorFicticio("b", "97727375025", "QA-REL-B", "3.75");
        var admin = login("admin@vitafortis.test", "ADMIN");
        var sessaoSemPermissao = login(a.getEmail(), "COLABORADOR");
        String relatorio = "/api/v1/colaborador/cashback/relatorio";
        mvc.perform(get(relatorio).session(sessaoSemPermissao.session())).andExpect(status().isForbidden());
        alterar(admin, a.getId(), "permissao-relatorios", true);
        alterar(admin, b.getId(), "permissao-relatorios", true);
        var sessaoA = login(a.getEmail(), "COLABORADOR");
        var sessaoB = login(b.getEmail(), "COLABORADOR");
        mvc.perform(get(relatorio).param("colaboradorId", b.getId().toString()).session(sessaoA.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cupons", hasSize(1)))
                .andExpect(jsonPath("$.cupons[0].codigo").value("QA-REL-A"))
                .andExpect(jsonPath("$.movimentos", hasSize(1)))
                .andExpect(jsonPath("$.movimentos[0].valor").value(1.25))
                .andExpect(jsonPath("$.movimentos[0].responsavel").doesNotExist())
                .andExpect(jsonPath("$.movimentos[0].justificativa").doesNotExist());
        mvc.perform(get(relatorio).session(sessaoB.session())).andExpect(status().isOk())
                .andExpect(jsonPath("$.cupons", hasSize(1)))
                .andExpect(jsonPath("$.cupons[0].codigo").value("QA-REL-B"))
                .andExpect(jsonPath("$.movimentos", hasSize(1)))
                .andExpect(jsonPath("$.movimentos[0].valor").value(3.75));
        mvc.perform(get("/api/v1/admin/usuarios").session(sessaoA.session())).andExpect(status().isForbidden());
        alterar(admin, a.getId(), "permissao-relatorios", false);
        mvc.perform(get(relatorio).session(sessaoA.session())).andExpect(status().isForbidden());
        var sessaoRenovada = login(a.getEmail(), "COLABORADOR");
        mvc.perform(get(relatorio).session(sessaoRenovada.session())).andExpect(status().isForbidden());
        alterar(admin, a.getId(), "ativo", false);
        mvc.perform(get("/api/v1/colaborador/cashback/resumo").session(sessaoRenovada.session()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", a.getEmail(), "senha", "Teste@123"))))
                .andExpect(status().isForbidden());
    }

    private void alterar(Login admin, Long id, String acao, boolean valor) throws Exception {
        mvc.perform(patch("/api/v1/admin/usuarios/{id}/{acao}", id, acao).with(csrf())
                        .session(admin.session()).param("valor", String.valueOf(valor)))
                .andExpect(status().isOk());
    }

    private Usuario colaboradorFicticio(String sufixo, String cpf, String codigo, String saldo) {
        var usuario = new Usuario();
        usuario.setNome("Colaborador QA " + sufixo);
        usuario.setEmail("relatorio-" + sufixo + "@vitafortis.test");
        usuario.setCpf(cpf);
        usuario.setAtivo(true);
        usuario.setTipo(TipoUsuario.COLABORADOR);
        usuario.setSenha(encoder.encode("Teste@123"));
        usuario = usuarios.saveAndFlush(usuario);
        var cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setTipo(CupomTipo.PERCENTUAL);
        cupom.setDesconto(BigDecimal.TEN);
        cupom.setColaborador(usuario);
        cupons.saveAndFlush(cupom);
        cashback.ajustar(usuario.getId(), new BigDecimal(saldo), "Movimento fictício", "QA");
        return usuario;
    }

    @Test
    void colaboradorCompraEAcompanhaPedidoComImagemSemAcessarDadosDeOutroUsuario() throws Exception {
        var colaborador = login("colaborador@vitafortis.test", "COLABORADOR");
        var cliente = login("cliente2@vitafortis.test", "CLIENTE");
        var produto = produtoComImagem();
        String itemCarrinho = "$.carrinhoItens[?(@.produtoId == " + produto.getId() + ")]";
        String itens = json.writeValueAsString(Map.of("produtoId", produto.getId(), "quantidade", 1));

        // Each request completes its own transaction, so subsequent reads also exercise persistence.
        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", colaborador.id()).with(csrf())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(itens))
                .andExpect(status().isOk())
                .andExpect(jsonPath(itemCarrinho + ".produtoImagemUrl", hasItem(produto.getImagemUrl())))
                .andExpect(jsonPath(itemCarrinho + ".quantidade", hasItem(1)));

        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", colaborador.id()).with(csrf())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(itens))
                .andExpect(status().isOk())
                .andExpect(jsonPath(itemCarrinho, hasSize(1)))
                .andExpect(jsonPath(itemCarrinho + ".quantidade", hasItem(2)));

        mvc.perform(get("/api/v1/carrinhos/{usuarioId}", colaborador.id()).session(colaborador.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(itemCarrinho + ".produtoImagemUrl", hasItem(produto.getImagemUrl())))
                .andExpect(jsonPath(itemCarrinho + ".quantidade", hasItem(2)));

        var pedidoBody = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(pedido(colaborador.id(), produto.getId()));
        var revisao = mvc.perform(post("/api/v1/pedidos/revisao").with(csrf())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(pedidoBody.toString()))
                .andExpect(status().isOk()).andReturn();
        var revisaoJson = json.readTree(revisao.getResponse().getContentAsString());
        pedidoBody.put("revisaoVersao", revisaoJson.get("revisaoVersao").asLong());
        pedidoBody.put("totalRevisado", revisaoJson.get("total").decimalValue());
        var criacao = mvc.perform(post("/api/v1/pedidos").with(csrf())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoBody.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(colaborador.id()))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"))
                .andExpect(jsonPath("$.formaRecebimento").value("RETIRADA"))
                .andExpect(jsonPath("$.frete").value(0))
                .andExpect(jsonPath("$.total").value(49.80))
                .andExpect(jsonPath("$.itens[0].produtoImagemUrl").value(produto.getImagemUrl()))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andReturn();
        long pedidoId = json.readTree(criacao.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/v1/pedidos/{id}", pedidoId).session(colaborador.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.itens[0].produtoId").value(produto.getId()))
                .andExpect(jsonPath("$.itens[0].produtoImagemUrl").value(produto.getImagemUrl()))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(24.90))
                .andExpect(jsonPath("$.itens[0].subtotal").value(49.80));

        mvc.perform(get("/api/v1/pedidos/usuario/{usuarioId}", colaborador.id()).session(colaborador.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + pedidoId + ")].itens[0].produtoImagemUrl",
                        hasItem(produto.getImagemUrl())));

        mvc.perform(get("/api/v1/carrinhos/{usuarioId}", colaborador.id()).session(cliente.session()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", cliente.id()).with(csrf())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(itens))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos/{id}", pedidoId).session(cliente.session()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos/usuario/{usuarioId}", colaborador.id()).session(cliente.session()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos/usuario/{usuarioId}", cliente.id()).session(colaborador.session()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/pedidos").with(csrf())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(cliente.id(), produto.getId())))
                .andExpect(status().isForbidden());

        assertEquals(10, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque(),
                "Pedido pendente nao deve debitar o estoque");
    }

    private Login login(String email, String tipoUsuario) throws Exception {
        var resposta = mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", "Teste@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoUsuario").value(tipoUsuario))
                .andReturn();
        var session = (MockHttpSession) resposta.getRequest().getSession(false);
        assertNotNull(session, "O login real deve estabelecer a sessao autenticada");
        return new Login(json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong(), session);
    }

    private String pedido(long usuarioId, long produtoId) throws Exception {
        return json.writeValueAsString(Map.of("usuarioId", usuarioId, "formaRecebimento", "RETIRADA",
                "formaPagamento", "PIX", "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 2))));
    }

    private Produto produtoComImagem() {
        var produto = new Produto();
        produto.setCodigo("TEST-COMPRA-" + UUID.randomUUID());
        produto.setNome("Produto para compra do colaborador");
        produto.setCategoria("PROTEINAS");
        produto.setPreco(new BigDecimal("24.90"));
        produto.setQuantidadeEstoque(10);
        produto.setImagemUrl("/assets/imagens/produto-teste-colaborador.webp");
        return produtos.saveAndFlush(produto);
    }

    private record Login(long id, MockHttpSession session) { }
}
