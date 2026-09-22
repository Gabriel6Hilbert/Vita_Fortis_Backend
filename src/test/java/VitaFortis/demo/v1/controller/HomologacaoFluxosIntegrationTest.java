package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
class HomologacaoFluxosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProdutoRepository produtos;

    @Test
    void relatoriosAplicamContratoDeFiltrosEPermissaoEmTodosOsFormatos() throws Exception {
        var admin=login("admin@vitafortis.test");
        var cliente=login("cliente@vitafortis.test");
        for(String formato:List.of("csv","xlsx","pdf")) {
            String url="/api/v1/admin/relatorios/CUPONS."+formato;
            mvc.perform(get(url).param("inicio","2026-09-01").param("fim","2026-09-30").param("busca","COLAB").session(admin.session()))
                    .andExpect(status().isOk()).andExpect(header().exists("Content-Disposition"));
            mvc.perform(get(url).param("inicio","2026-09-01").param("fim","2026-09-30").session(cliente.session())).andExpect(status().isForbidden());
            mvc.perform(get(url).param("inicio","2026-09-30").param("fim","2026-09-01").session(admin.session())).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/v1/admin/relatorios/PEDIDOS.csv").param("inicio","2026-09-01").param("fim","2026-09-30").param("pagamento","INVALIDO").session(admin.session()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void revisaoEntregaNaoDebitaEstoqueEConfirmacaoRevalidaTotal() throws Exception {
        var cliente=login("cliente@vitafortis.test");
        var admin=login("admin@vitafortis.test");
        var produto=produto();
        var endereco=mvc.perform(post("/api/v1/usuarios/{id}/enderecos",cliente.id()).session(cliente.session())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "cep","01001000","logradouro","Rua de teste","numero","10","bairro","Centro","cidade","Sao Paulo","uf","SP","principal",false))))
                .andExpect(status().isCreated()).andReturn();
        long enderecoId=json.readTree(endereco.getResponse().getContentAsString()).get("id").asLong();
        var body=new HashMap<String,Object>();
        body.put("usuarioId",cliente.id()); body.put("formaRecebimento","ENTREGA"); body.put("formaPagamento","PIX"); body.put("enderecoId",enderecoId);
        body.put("itens",List.of(Map.of("produtoId",produto.getId(),"quantidade",2)));
        mvc.perform(post("/api/v1/pedidos/revisao").session(cliente.session()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subtotal").value(49.80))
                .andExpect(jsonPath("$.frete").value(15)).andExpect(jsonPath("$.total").value(64.80))
                .andExpect(jsonPath("$.prazoEntregaDias").value(5));
        assertEquals(10,produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        body.put("totalRevisado",49.80);
        mvc.perform(post("/api/v1/pedidos").session(cliente.session()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
        assertEquals(10,produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque(),"Erro de total deve desfazer a reserva");
        body.put("totalRevisado",64.80);
        var criado=mvc.perform(post("/api/v1/pedidos").session(cliente.session()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(64.80)).andReturn();
        long id=json.readTree(criado.getResponse().getContentAsString()).get("id").asLong();
        assertEquals(10,produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/pagamento/aprovar",id).session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statusPagamento").value("APROVADO"))
                .andExpect(jsonPath("$.itens.length()").value(1));
        assertEquals(8,produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/pagamento/aprovar",id).session(admin.session())).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/status",id).param("valor","CANCELADO").session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statusPagamento").value("ESTORNADO"));
        assertEquals(10,produtos.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
    }

    @Test
    void importacaoValidaAntesDeGravarEPreservaCamposCsv() throws Exception {
        var admin=login("admin@vitafortis.test");
        String sku="IMP-"+UUID.randomUUID();
        String header="codigo;nome;descricao;marca;unidade;preco;estoque;categoria;imagemUrl;ativo\n";
        String valido=sku+";Produto;\"Descricao; com separador\";FTW;300g;20.00;2;PROTEINAS;;true\n";
        String invalido=sku+";Duplicado;Descricao;FTW;300g;-1;2;PROTEINAS;;true\n";
        mvc.perform(multipart("/api/v1/admin/produtos/importacao").file(csv(header+valido+invalido)).param("preVisualizar","true").session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rejeitados").value(1))
                .andExpect(jsonPath("$.registros[0].codigo").value(sku.toUpperCase()));
        mvc.perform(multipart("/api/v1/admin/produtos/importacao").file(csv(header+valido+invalido)).param("preVisualizar","false").session(admin.session()))
                .andExpect(status().isBadRequest());
        assertFalse(produtos.existsByCodigoIgnoreCase(sku));
        mvc.perform(multipart("/api/v1/admin/produtos/importacao").file(csv(header+valido)).param("preVisualizar","false").session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.inseridos").value(1));
        assertEquals("Descricao; com separador",produtos.findByCodigoIgnoreCase(sku).orElseThrow().getDescricao());
        var historico=mvc.perform(get("/api/v1/admin/produtos/importacao/historico").session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nomeArquivo").value("produtos.csv"))
                .andExpect(jsonPath("$[0].responsavel").value("admin@vitafortis.test"))
                .andExpect(jsonPath("$[0].resultado").value("CONCLUIDA")).andReturn();
        long historicoId=json.readTree(historico.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(get("/api/v1/admin/produtos/importacao/historico/{id}/arquivo",historicoId).session(admin.session()))
                .andExpect(status().isOk()).andExpect(content().bytes((header+valido).getBytes(StandardCharsets.UTF_8)));
        String negativo=valido.replace(sku,sku+"N").replace("20.00","-1");
        mvc.perform(multipart("/api/v1/admin/produtos/importacao").file(csv(header+negativo)).param("preVisualizar","true").session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rejeitados").value(1));
    }

    @Test
    void entregaValidaRegiaoERetiradaTemStatusProprio() throws Exception {
        var cliente=login("cliente@vitafortis.test");
        var admin=login("admin@vitafortis.test");
        var produto=produto();
        var body=new HashMap<String,Object>();
        body.put("usuarioId",cliente.id()); body.put("formaRecebimento","RETIRADA"); body.put("formaPagamento","PIX");
        body.put("itens",List.of(Map.of("produtoId",produto.getId(),"quantidade",1))); body.put("totalRevisado",new BigDecimal("24.90"));
        var criado=mvc.perform(post("/api/v1/pedidos").session(cliente.session()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andReturn();
        long id=json.readTree(criado.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/pagamento/aprovar",id).session(admin.session())).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/status",id).param("valor","EM_SEPARACAO").session(admin.session())).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/status",id).param("valor","DISPONIVEL_RETIRADA").session(admin.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DISPONIVEL_RETIRADA"));
        mvc.perform(patch("/api/v1/admin/pedidos/{id}/status",id).param("valor","ENTREGUE").session(admin.session())).andExpect(status().isOk());
    }

    @Test
    void colaboradorNaoRecebePedidosDeCompradoresNoResumo() throws Exception {
        var colaborador=login("colaborador@vitafortis.test");
        mvc.perform(get("/api/v1/colaborador/cashback/resumo").session(colaborador.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.saldo").isNumber())
                .andExpect(jsonPath("$.pedidos").doesNotExist())
                .andExpect(jsonPath("$.movimentos").doesNotExist())
                .andExpect(jsonPath("$.cupons").doesNotExist());
    }

    private MockMultipartFile csv(String content) { return new MockMultipartFile("arquivo","produtos.csv","text/csv",content.getBytes(StandardCharsets.UTF_8)); }
    private Produto produto() {
        var p=new Produto(); p.setCodigo("CHECK-"+UUID.randomUUID()); p.setNome("Produto de teste");
        p.setCategoria("PROTEINAS"); p.setPreco(new BigDecimal("24.90")); p.setQuantidadeEstoque(10);
        return produtos.saveAndFlush(p);
    }
    private Login login(String email) throws Exception {
        var result=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email",email,"senha","Teste@123")))).andExpect(status().isOk()).andReturn();
        return new Login(json.readTree(result.getResponse().getContentAsString()).get("id").asLong(),(MockHttpSession)result.getRequest().getSession(false));
    }
    private record Login(long id,MockHttpSession session) {}
}
