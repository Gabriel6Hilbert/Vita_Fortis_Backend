package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void colaboradorCompraEAcompanhaPedidoComImagemSemAcessarDadosDeOutroUsuario() throws Exception {
        var colaborador = login("colaborador@vitafortis.test", "COLABORADOR");
        var cliente = login("cliente2@vitafortis.test", "CLIENTE");
        var produto = produtoComImagem();
        String itemCarrinho = "$.carrinhoItens[?(@.produtoId == " + produto.getId() + ")]";
        String itens = json.writeValueAsString(Map.of("produtoId", produto.getId(), "quantidade", 1));

        // Each request completes its own transaction, so subsequent reads also exercise persistence.
        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", colaborador.id())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(itens))
                .andExpect(status().isOk())
                .andExpect(jsonPath(itemCarrinho + ".produtoImagemUrl", hasItem(produto.getImagemUrl())))
                .andExpect(jsonPath(itemCarrinho + ".quantidade", hasItem(1)));

        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", colaborador.id())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(itens))
                .andExpect(status().isOk())
                .andExpect(jsonPath(itemCarrinho, hasSize(1)))
                .andExpect(jsonPath(itemCarrinho + ".quantidade", hasItem(2)));

        mvc.perform(get("/api/v1/carrinhos/{usuarioId}", colaborador.id()).session(colaborador.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(itemCarrinho + ".produtoImagemUrl", hasItem(produto.getImagemUrl())))
                .andExpect(jsonPath(itemCarrinho + ".quantidade", hasItem(2)));

        var criacao = mvc.perform(post("/api/v1/pedidos")
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(colaborador.id(), produto.getId())))
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
        mvc.perform(post("/api/v1/carrinhos/{usuarioId}/itens", cliente.id())
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON).content(itens))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos/{id}", pedidoId).session(cliente.session()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos/usuario/{usuarioId}", colaborador.id()).session(cliente.session()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos/usuario/{usuarioId}", cliente.id()).session(colaborador.session()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/pedidos")
                        .session(colaborador.session()).contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(cliente.id(), produto.getId())))
                .andExpect(status().isForbidden());

        assertEquals(8, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque(),
                "Somente o pedido autorizado deve debitar o estoque");
    }

    private Login login(String email, String tipoUsuario) throws Exception {
        var resposta = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
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
        produto.setCategoria(CategoriaProduto.PROTEINAS);
        produto.setPreco(new BigDecimal("24.90"));
        produto.setQuantidadeEstoque(10);
        produto.setImagemUrl("/assets/imagens/produto-teste-colaborador.webp");
        return produtos.saveAndFlush(produto);
    }

    private record Login(long id, MockHttpSession session) { }
}
