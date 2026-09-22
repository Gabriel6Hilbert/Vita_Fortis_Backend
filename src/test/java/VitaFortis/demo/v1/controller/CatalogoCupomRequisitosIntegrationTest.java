package VitaFortis.demo.v1.controller;

import VitaFortis.demo.config.CatalogoFtwDataInitializer;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.repository.*;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("homologacao")
class CatalogoCupomRequisitosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProdutoRepository produtos;
    @Autowired CupomRepository cupons;
    @Autowired PedidoRepository pedidos;
    @Autowired CatalogoFtwDataInitializer carga;

    record Login(long id, MockHttpSession session) {}
    private Login login(String email) throws Exception {
        var r = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "senha", "Teste@123")))).andReturn();
        assertEquals(200, r.getResponse().getStatus());
        return new Login(json.readTree(r.getResponse().getContentAsString()).get("id").asLong(), (MockHttpSession) r.getRequest().getSession(false));
    }
    private JsonNode request(MockHttpServletRequestBuilder req, Login login, Object body, int expected) throws Exception {
        if (login != null) req.session(login.session());
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var r = mvc.perform(req).andReturn().getResponse();
        assertEquals(expected, r.getStatus(), r.getContentAsString());
        return r.getContentAsString().isBlank() ? json.nullNode() : json.readTree(r.getContentAsString());
    }
    private String unique() { return UUID.randomUUID().toString().replace("-", "").toUpperCase(); }
    private Produto produto() {
        var p = new Produto(); p.setCodigo("REQ-" + unique()); p.setNome("Produto teste requisitos");
        p.setCategoria("PROTEINAS"); p.setPreco(new BigDecimal("100.00")); p.setQuantidadeEstoque(20);
        return produtos.saveAndFlush(p);
    }
    private Map<String, Object> cupom() {
        var data = new HashMap<String,Object>(); data.put("codigo", "REQ" + unique()); data.put("tipo", "PERCENTUAL");
        data.put("desconto", 10); data.put("minSubtotal", 100); data.put("limiteUso", 1);
        data.put("dataInicio", LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).minusHours(1).toString());
        data.put("dataVencimento", LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).plusDays(1).toString());
        return data;
    }
    private Map<String,Object> pedido(Login cliente, Produto p, long cupomId) {
        return Map.of("usuarioId",cliente.id(),"formaRecebimento","RETIRADA","formaPagamento","PIX","cupomId",cupomId,
                "itens",List.of(Map.of("produtoId",p.getId(),"quantidade",1)));
    }

    @Test void rf28InativaPreservaBancoEOcultaCatalogoDetalheFavoritoESacola() throws Exception {
        var admin = login("admin@vitafortis.test"); var cliente = login("cliente@vitafortis.test"); var p = produto();
        request(post("/api/v1/usuarios/"+cliente.id()+"/favoritos/"+p.getId()),cliente,null,200);
        request(post("/api/v1/carrinhos/"+cliente.id()+"/itens"),cliente,Map.of("produtoId",p.getId(),"quantidade",1),200);
        request(delete("/api/v1/admin/produtos/"+p.getId()),admin,null,200);
        assertFalse(produtos.findById(p.getId()).orElseThrow().isAtivo());
        assertEquals(0,request(get("/api/v1/produtos").param("busca",p.getCodigo()),null,null,200).get("totalElements").asInt());
        request(get("/api/v1/produtos/"+p.getId()),null,null,400);
        assertFalse(request(get("/api/v1/usuarios/"+cliente.id()+"/favoritos"),cliente,null,200).toString().contains(p.getCodigo()));
        var cart=request(get("/api/v1/carrinhos/"+cliente.id()),cliente,null,200);
        for(var item:cart.get("carrinhoItens")) assertNotEquals(p.getId().longValue(),item.get("produtoId").asLong());
        request(post("/api/v1/carrinhos/"+cliente.id()+"/itens"),cliente,Map.of("produtoId",p.getId(),"quantidade",1),400);
        assertFalse(request(get("/api/v1/admin/produtos/"+p.getId()),admin,null,200).get("ativo").asBoolean());
    }

    @Test void rf28CargaNaoReativaProdutoArquivado() throws Exception {
        var p=produtos.findByCodigoIgnoreCase("PA00072610").orElseThrow();
        boolean anterior=p.isAtivo();
        try { p.setAtivo(false);produtos.saveAndFlush(p);carga.run();assertFalse(produtos.findById(p.getId()).orElseThrow().isAtivo()); }
        finally { var atual=produtos.findById(p.getId()).orElseThrow();atual.setAtivo(anterior);produtos.saveAndFlush(atual); }
    }

    @Test void rf30CadastroDinamicoEdicaoVinculoEInativacaoComAutorizacao() throws Exception {
        var admin=login("admin@vitafortis.test"); var cliente=login("cliente2@vitafortis.test");
        String cat="CAT"+unique(), attr="ATR"+unique(); String url="/api/v1/admin/catalogo/cadastros";
        var category=Map.of("tipo","CATEGORIA","codigo",cat,"nome","Categoria livre","ativo",true);
        request(post(url),cliente,category,403); request(get(url),cliente,null,403);
        long catId=request(post(url),admin,category,201).get("id").asLong();request(post(url),admin,category,400);
        long attrId=request(post(url),admin,Map.of("tipo","ATRIBUTO","codigo",attr,"nome","Sabor livre","ativo",true),201).get("id").asLong();
        var data=new HashMap<String,Object>();data.put("codigo",unique());data.put("nome","Produto categoria nova");data.put("descricao","Descricao teste");
        data.put("preco",30);data.put("quantidadeEstoque",10);data.put("categoria",cat);data.put("atributos",Map.of(attr,"Morango"));
        long id=request(post("/api/v1/admin/produtos"),admin,data,201).get("id").asLong();
        var publico=request(get("/api/v1/produtos/"+id),null,null,200);
        assertEquals("Morango",publico.get("atributos").get(attr).asText());
        assertEquals(1,request(get("/api/v1/produtos").param("categoria",cat),null,null,200).get("totalElements").asInt());
        request(put(url+"/"+catId),admin,Map.of("tipo","CATEGORIA","codigo",cat,"nome","Nome alterado","ativo",true),200);
        assertTrue(request(get("/api/v1/produtos/categorias"),null,null,200).toString().contains("Nome alterado"));
        request(delete(url+"/"+attrId),admin,null,204);request(delete(url+"/"+catId),admin,null,204);
        assertEquals(cat,request(get("/api/v1/admin/produtos/"+id),admin,null,200).get("categoria").asText());
        data.put("codigo",unique());request(post("/api/v1/admin/produtos"),admin,data,400);
        request(put(url+"/"+catId),admin,category,200);
        data.put("atributos",Map.of("INEXISTENTE","valor"));request(post("/api/v1/admin/produtos"),admin,data,400);
    }

    @Test void rnf04LimiteRevalidadoNoCheckoutECashbackPreservado() throws Exception {
        var admin=login("admin@vitafortis.test"); var cliente=login("cliente2@vitafortis.test");var colab=login("colaborador@vitafortis.test");
        BigDecimal saldoAnterior=request(get("/api/v1/colaborador/cashback/resumo"),colab,null,200).get("saldo").decimalValue();
        var p=produto();var dto=cupom();dto.put("colaboradorId",colab.id());dto.put("percentualCashback",5);
        var coupon=request(post("/api/v1/admin/cupons"),admin,dto,201);long id=coupon.get("id").asLong();
        var order=pedido(cliente,p,id);
        assertEquals(90,request(post("/api/v1/pedidos/revisao"),cliente,order,200).get("total").asInt());
        long orderId=request(post("/api/v1/pedidos"),cliente,order,201).get("id").asLong();
        request(post("/api/v1/pedidos"),cliente,order,400);request(post("/api/v1/pedidos/revisao"),cliente,order,400);
        assertEquals(1,pedidos.countByCupomUtilizadoId(id));
        request(post("/api/v1/carrinhos/"+cliente.id()+"/itens"),cliente,Map.of("produtoId",p.getId(),"quantidade",1),200);
        request(post("/api/v1/carrinhos/"+cliente.id()+"/cupom/"+dto.get("codigo")),cliente,null,400);
        request(patch("/api/v1/admin/pedidos/"+orderId+"/pagamento/aprovar"),admin,null,200);
        var summary=request(get("/api/v1/colaborador/cashback/resumo"),colab,null,200);
        assertEquals(0, saldoAnterior.add(new BigDecimal("5.00")).compareTo(summary.get("saldo").decimalValue()));
        assertNull(summary.get("cupons"));
    }

    @Test void rnf04RejeitaDatasMinimoPercentualELimiteInvalidosPermiteRemoverRegras() throws Exception {
        var admin=login("admin@vitafortis.test");var cliente=login("cliente2@vitafortis.test");var dto=cupom();
        dto.put("desconto",101);request(post("/api/v1/admin/cupons"),admin,dto,400);dto.put("desconto",10);
        dto.put("limiteUso",0);request(post("/api/v1/admin/cupons"),admin,dto,422);dto.put("limiteUso",1);
        dto.put("dataInicio",LocalDateTime.now().plusDays(3).toString());request(post("/api/v1/admin/cupons"),admin,dto,400);
        dto=cupom();dto.put("dataInicio",LocalDateTime.now().plusHours(5).toString());
        long id=request(post("/api/v1/admin/cupons"),admin,dto,201).get("id").asLong();var p=produto();
        request(post("/api/v1/pedidos/revisao"),cliente,pedido(cliente,p,id),400);
        dto.put("dataInicio",null);dto.put("minSubtotal",101);request(put("/api/v1/admin/cupons/"+id),admin,dto,200);
        request(post("/api/v1/pedidos"),cliente,pedido(cliente,p,id),400);
        dto.put("minSubtotal",null);dto.put("dataVencimento",null);dto.put("limiteUso",null);
        request(put("/api/v1/admin/cupons/"+id),admin,dto,200);
        var saved=cupons.findById(id).orElseThrow();assertNull(saved.getDataInicio());assertNull(saved.getDataVencimento());assertNull(saved.getMinSubtotal());assertNull(saved.getLimiteUso());
        request(post("/api/v1/pedidos/revisao"),cliente,pedido(cliente,p,id),200);
        saved.setDataVencimento(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).minusMinutes(1));cupons.saveAndFlush(saved);
        request(post("/api/v1/pedidos"),cliente,pedido(cliente,p,id),400);
    }

    @Test void rnf04ComprasConcorrentesNaoUltrapassamLimite() throws Exception {
        var admin=login("admin@vitafortis.test");var a=login("cliente@vitafortis.test");var b=login("cliente2@vitafortis.test");
        long id=request(post("/api/v1/admin/cupons"),admin,cupom(),201).get("id").asLong();
        var bodyA=json.writeValueAsString(pedido(a,produto(),id));var bodyB=json.writeValueAsString(pedido(b,produto(),id));
        var ready=new CountDownLatch(2);var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            var fa=pool.submit(()->{ready.countDown();start.await();return mvc.perform(post("/api/v1/pedidos").session(a.session()).contentType(MediaType.APPLICATION_JSON).content(bodyA)).andReturn().getResponse().getStatus();});
            var fb=pool.submit(()->{ready.countDown();start.await();return mvc.perform(post("/api/v1/pedidos").session(b.session()).contentType(MediaType.APPLICATION_JSON).content(bodyB)).andReturn().getResponse().getStatus();});
            assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
            assertEquals(Set.of(201,400),new HashSet<>(List.of(fa.get(20,TimeUnit.SECONDS),fb.get(20,TimeUnit.SECONDS))));
            assertEquals(1,pedidos.countByCupomUtilizadoId(id));
        } finally { pool.shutdownNow(); }
    }
}
