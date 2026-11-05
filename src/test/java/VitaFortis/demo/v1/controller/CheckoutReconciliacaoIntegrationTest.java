package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.PedidoRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
class CheckoutReconciliacaoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProdutoRepository produtos;
    @Autowired UsuarioRepository usuarios;
    @Autowired CupomRepository cupons;
    @Autowired PedidoRepository pedidos;

    @Test
    void reconciliaPrecoCupomCashbackBloqueiaRevisaoVencidaEConfirmaUmaUnicaVez() throws Exception {
        Login cliente = login("cliente2@vitafortis.test");
        request(delete("/api/v1/carrinhos/" + cliente.id + "/itens"), cliente, null, 200);

        Produto produto = new Produto();
        produto.setCodigo("P0-" + UUID.randomUUID());
        produto.setNome("Produto P0 checkout");
        produto.setCategoria("PROTEINAS");
        produto.setPreco(new BigDecimal("73.27"));
        produto.setQuantidadeEstoque(10);
        produto = produtos.saveAndFlush(produto);

        Cupom cupom = new Cupom();
        cupom.setCodigo("P0" + UUID.randomUUID().toString().replace("-", ""));
        cupom.setTipo(CupomTipo.PERCENTUAL);
        cupom.setDesconto(new BigDecimal("20.00"));
        cupom.setAtivo(true);
        cupom.setColaborador(usuarios.findByEmail("colaborador@vitafortis.test").orElseThrow());
        cupom.setPercentualCashback(new BigDecimal("5.00"));
        cupom = cupons.saveAndFlush(cupom);

        request(post("/api/v1/carrinhos/" + cliente.id + "/itens"), cliente,
                Map.of("produtoId", produto.getId(), "quantidade", 1), 200);
        request(post("/api/v1/carrinhos/" + cliente.id + "/cupom/" + cupom.getCodigo()), cliente, null, 200);

        produto.setPreco(new BigDecimal("1.00"));
        produtos.saveAndFlush(produto);

        JsonNode carrinho = request(get("/api/v1/carrinhos/" + cliente.id), cliente, null, 200);
        assertEquals(1.00, carrinho.get("carrinhoItens").get(0).get("precoUnitario").asDouble());
        assertEquals(1.00, carrinho.get("subtotal").asDouble());
        assertEquals(0.20, carrinho.get("descontos").asDouble());
        assertEquals(0.80, carrinho.get("total").asDouble());
        assertEquals(0.05, carrinho.get("cashbackColaborador").asDouble());
        assertTrue(carrinho.get("precoAlterado").asBoolean());

        Map<String, Object> body = new HashMap<>();
        body.put("usuarioId", cliente.id);
        body.put("formaRecebimento", "RETIRADA");
        body.put("formaPagamento", "PIX");
        body.put("cupomId", cupom.getId());
        body.put("itens", List.of(Map.of("produtoId", produto.getId(), "quantidade", 1)));

        long pedidosAntes = pedidos.count();
        JsonNode revisao = request(post("/api/v1/pedidos/revisao"), cliente, body, 200);
        assertEquals(1.00, revisao.get("itens").get(0).get("precoUnitario").asDouble());
        assertEquals(0.80, revisao.get("total").asDouble());
        assertEquals(0.05, revisao.get("cashbackColaborador").asDouble());
        assertEquals(pedidosAntes, pedidos.count(), "A revisão não pode criar pedido");
        assertEquals(10, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque(), "A revisão não pode baixar estoque");

        body.put("revisaoVersao", revisao.get("revisaoVersao").asLong());
        body.put("totalRevisado", revisao.get("total").decimalValue());
        produto = produtos.findById(produto.getId()).orElseThrow();
        produto.setPreco(new BigDecimal("2.00"));
        produtos.saveAndFlush(produto);
        request(post("/api/v1/pedidos"), cliente, body, 409);
        assertEquals(pedidosAntes, pedidos.count(), "Revisão vencida não pode criar pedido");

        carrinho = request(get("/api/v1/carrinhos/" + cliente.id), cliente, null, 200);
        assertEquals(2.00, carrinho.get("carrinhoItens").get(0).get("precoUnitario").asDouble());
        assertEquals(1.60, carrinho.get("total").asDouble());
        assertEquals(0.10, carrinho.get("cashbackColaborador").asDouble());

        JsonNode novaRevisao = request(post("/api/v1/pedidos/revisao"), cliente, body, 200);
        body.put("revisaoVersao", novaRevisao.get("revisaoVersao").asLong());
        body.put("totalRevisado", novaRevisao.get("total").decimalValue());
        String payload = json.writeValueAsString(body);
        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        long pedidoId;
        try {
            var a = pool.submit(() -> confirmarConcorrente(cliente, payload, prontas, iniciar));
            var b = pool.submit(() -> confirmarConcorrente(cliente, payload, prontas, iniciar));
            assertTrue(prontas.await(10, TimeUnit.SECONDS));
            iniciar.countDown();
            var primeira = a.get(20, TimeUnit.SECONDS);
            var segunda = b.get(20, TimeUnit.SECONDS);
            assertEquals(Set.of(201, 409), Set.of(primeira.getStatus(), segunda.getStatus()));
            var criada = primeira.getStatus() == 201 ? primeira : segunda;
            pedidoId = json.readTree(criada.getContentAsString()).get("id").asLong();
        } finally {
            pool.shutdownNow();
        }
        assertEquals(pedidosAntes + 1, pedidos.count());
        assertEquals(10, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque(), "Confirmar pedido não baixa estoque");
        request(post("/api/v1/pedidos"), cliente, body, 409);
        assertEquals(pedidosAntes + 1, pedidos.count());

        Login admin = login("admin@vitafortis.test");
        Login colaborador = login("colaborador@vitafortis.test");
        String saldoPath = "/api/v1/colaborador/cashback/resumo";
        BigDecimal saldoInicial = request(get(saldoPath), colaborador, null, 200).get("saldo").decimalValue();
        String adminPedido = "/api/v1/admin/pedidos/" + pedidoId;
        assertEquals("SIMULADO", request(get("/api/v1/loja/versao"), cliente, null, 200).get("pagamento").asText());
        request(patch(adminPedido + "/pagamento/aprovar"), admin, null, 200);
        assertEquals(9, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        BigDecimal saldoCreditado = request(get(saldoPath), colaborador, null, 200).get("saldo").decimalValue();
        assertEquals(0, saldoInicial.add(new BigDecimal("0.10")).compareTo(saldoCreditado));
        request(patch(adminPedido + "/pagamento/aprovar"), admin, null, 400);
        assertEquals(9, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        assertEquals(0, saldoCreditado.compareTo(request(get(saldoPath), colaborador, null, 200).get("saldo").decimalValue()));
        request(patch(adminPedido + "/status").param("valor", "CANCELADO"), admin, null, 200);
        assertEquals(10, produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        assertEquals(0, saldoInicial.compareTo(request(get(saldoPath), colaborador, null, 200).get("saldo").decimalValue()));
    }

    private org.springframework.mock.web.MockHttpServletResponse confirmarConcorrente(
            Login login, String payload, CountDownLatch prontas, CountDownLatch iniciar) throws Exception {
        prontas.countDown();
        iniciar.await();
        return mvc.perform(post("/api/v1/pedidos").with(csrf()).session(login.session)
                .contentType(MediaType.APPLICATION_JSON).content(payload)).andReturn().getResponse();
    }

    private JsonNode request(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                             Login login, Object body, int esperado) throws Exception {
        builder.with(csrf()).session(login.session);
        if (body != null) builder.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(builder).andReturn().getResponse();
        assertEquals(esperado, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsString());
    }

    private Login login(String email) throws Exception {
        var result = mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "senha", "Teste@123"))))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        return new Login(json.readTree(result.getResponse().getContentAsString()).get("id").asLong(),
                (MockHttpSession) result.getRequest().getSession(false));
    }

    private record Login(long id, MockHttpSession session) {}
}
