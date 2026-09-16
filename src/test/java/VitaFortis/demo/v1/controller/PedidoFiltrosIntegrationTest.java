package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.Pedido;
import VitaFortis.demo.v1.enums.*;
import VitaFortis.demo.v1.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:rf36;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("homologacao")
@Transactional
class PedidoFiltrosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PedidoRepository pedidos;
    @Autowired UsuarioRepository usuarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;
    private final List<Long> ids = new ArrayList<>();

    @BeforeEach void preparar() {
        // Dados persistidos: primeiro/ultimo instante do dia e dia seguinte.
        criar("cliente@vitafortis.test", "2031-05-10T00:00:00", StatusCompra.PENDENTE, FormaRecebimento.RETIRADA, "PIX", StatusPagamento.PENDENTE);
        criar("cliente@vitafortis.test", "2031-05-10T23:59:59.999999", StatusCompra.PENDENTE, FormaRecebimento.RETIRADA, "PIX", StatusPagamento.PENDENTE);
        criar("cliente2@vitafortis.test", "2031-05-11T00:00:00", StatusCompra.ENVIADO, FormaRecebimento.ENTREGA, "CARTAO", StatusPagamento.APROVADO);
        criar("colaborador@vitafortis.test", "2031-05-09T23:59:59", StatusCompra.CANCELADO, FormaRecebimento.ENTREGA, "BOLETO", StatusPagamento.RECUSADO);
        em.clear();
    }

    private void criar(String email, String data, StatusCompra status, FormaRecebimento recebimento, String pagamento, StatusPagamento situacao) {
        var p = new Pedido();
        p.setUsuario(usuarios.findByEmail(email).orElseThrow());
        p.setStatus(status); p.setFormaRecebimento(recebimento); p.setFormaPagamento(pagamento);
        p.setStatusPagamento(situacao); p.setItems(new ArrayList<>());
        pedidos.saveAndFlush(p);
        jdbc.update("update PEDIDO set DATA_PEDIDO=? where PEDIDO_ID=?", Timestamp.valueOf(LocalDateTime.parse(data)), p.getId());
        ids.add(p.getId());
    }

    private Set<Long> consultar(Map<String, String> filtros) throws Exception {
        var request = get("/api/v1/admin/pedidos").with(user("admin@vitafortis.test").roles("ADMIN"));
        filtros.forEach(request::param);
        var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        Set<Long> encontrados = new HashSet<>();
        json.readTree(result.getResponse().getContentAsString()).forEach(p -> encontrados.add(p.get("id").asLong()));
        return encontrados;
    }

    @Test void filtrosIndividuaisECombinacoesComNumero() throws Exception {
        var filtros = List.of(Map.entry("inicio", "2031-05-10"), Map.entry("fim", "2031-05-10"),
                Map.entry("cliente", "CLIENTE@VITAFORTIS.TEST"), Map.entry("status", "PENDENTE"),
                Map.entry("recebimento", "RETIRADA"), Map.entry("pagamento", "PIX"),
                Map.entry("numero", ids.get(0).toString()), Map.entry("statusPagamento", "PENDENTE"));
        for (int mask = 0; mask < (1 << filtros.size()); mask++) {
            Map<String, String> query = new HashMap<>();
            for (int bit = 0; bit < filtros.size(); bit++) if ((mask & (1 << bit)) != 0)
                query.put(filtros.get(bit).getKey(), filtros.get(bit).getValue());
            var encontrados = consultar(query);
            Set<Long> esperados = new HashSet<>(ids);
            if (query.containsKey("inicio")) esperados.remove(ids.get(3));
            if (query.containsKey("fim")) esperados.remove(ids.get(2));
            if (query.containsKey("cliente") || query.containsKey("status") || query.containsKey("recebimento") || query.containsKey("pagamento") || query.containsKey("statusPagamento"))
                esperados.retainAll(ids.subList(0, 2));
            if (query.containsKey("numero")) esperados.removeIf(id -> !id.toString().contains(ids.get(0).toString()));
            assertEquals(esperados, encontrados, query.toString());
        }
    }

    @Test void outrasOpcoesNomeBuscaParcialVazioELimpeza() throws Exception {
        assertEquals(Set.of(ids.get(2)), consultar(Map.of("pagamento", "CARTAO")));
        assertEquals(Set.of(ids.get(3)), consultar(Map.of("pagamento", "BOLETO")));
        assertEquals(Set.of(ids.get(2), ids.get(3)), consultar(Map.of("recebimento", "ENTREGA")));
        assertEquals(Set.of(ids.get(3)), consultar(Map.of("status", "CANCELADO")));
        assertEquals(Set.of(ids.get(2)), consultar(Map.of("statusPagamento", "APROVADO")));
        assertEquals(Set.of(ids.get(3)), consultar(Map.of("statusPagamento", "RECUSADO")));
        assertTrue(consultar(Map.of("statusPagamento", "ESTORNADO")).isEmpty());
        String nome = usuarios.findByEmail("colaborador@vitafortis.test").orElseThrow().getNome();
        assertEquals(Set.of(ids.get(3)), consultar(Map.of("cliente", nome.toUpperCase(Locale.ROOT))));
        String parcial = ids.get(2).toString().substring(0, 1);
        assertEquals(new HashSet<>(ids.stream().filter(id -> id.toString().contains(parcial)).toList()), consultar(Map.of("numero", parcial)));
        assertTrue(consultar(Map.of("cliente", "inexistente-rf36")).isEmpty());
        assertTrue(consultar(Map.of("cliente", "%")).isEmpty());
        assertTrue(consultar(Map.of("recebimento", "RETIRADA", "pagamento", "BOLETO")).isEmpty());
        assertEquals(new HashSet<>(ids), consultar(Map.of()));
        assertEquals(new HashSet<>(ids), consultar(Map.of("numero", "", "cliente", " ", "inicio", "", "fim", "", "status", "", "recebimento", "", "pagamento", "", "statusPagamento", "")));
    }

    @Test void rejeitaPeriodoInvertidoEValoresInvalidos() throws Exception {
        for (var filtros : List.of(Map.of("inicio", "2031-05-11", "fim", "2031-05-10"),
                Map.of("inicio", "nao-e-data"), Map.of("status", "INVALIDO"), Map.of("recebimento", "INVALIDO"),
                Map.of("pagamento", "INVALIDO"), Map.of("statusPagamento", "INVALIDO"))) {
            var request = get("/api/v1/admin/pedidos").with(user("admin").roles("ADMIN"));
            filtros.forEach(request::param);
            mvc.perform(request).andExpect(status().is4xxClientError());
        }
    }

    @Test void preservaPermissoes() throws Exception {
        for (String role : List.of("CLIENTE", "COLABORADOR"))
            mvc.perform(get("/api/v1/admin/pedidos").param("pagamento", "PIX").with(user("outro").roles(role)))
                    .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/pedidos")).andExpect(status().is4xxClientError());
    }
}
