package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailTransacionalServiceTest {
    private final JavaMailSender sender = mock(JavaMailSender.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
    private EmailTransacionalService service;

    @BeforeEach
    void setup() {
        when(provider.getIfAvailable()).thenReturn(sender);
        service = new EmailTransacionalService(provider, RestClient.builder());
        ReflectionTestUtils.setField(service, "habilitado", true);
        ReflectionTestUtils.setField(service, "transporte", "smtp");
        ReflectionTestUtils.setField(service, "remetente", "pedidos@vitafortis.com.br");
        ReflectionTestUtils.setField(service, "responderPara", "vitafortis@atendimentos.com.br");
        ReflectionTestUtils.setField(service, "frontendUrl", "https://vitafortis.example");
    }

    @Test
    void recuperacaoIncluiLinkSeguroEExpiracao() {
        service.enviarRecuperacaoSenha("cliente@example.com", "Cliente", "token-seguro");

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains("/entrar?recuperar=token-seguro"));
        assertTrue(captor.getValue().getText().contains("30 minutos"));
    }

    @Test
    void entregaNaoMencionaTransportadora() {
        service.enviarStatusPedido(42L, "cliente@example.com", "Cliente",
                StatusCompra.ENVIADO, FormaRecebimento.ENTREGA);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains("equipe Vita Fortis"));
        assertTrue(!captor.getValue().getText().toLowerCase().contains("transportadora"));
    }

    @Test
    void pedidoCriadoGeraMensagemDeRecebimentoParaCliente() {
        service.enviarStatusPedido(42L, "cliente@example.com", "Cliente",
                StatusCompra.PENDENTE, FormaRecebimento.RETIRADA);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        SimpleMailMessage mensagem = captor.getValue();
        assertEquals("Atualização do pedido #42 — Vita Fortis", mensagem.getSubject());
        assertEquals("cliente@example.com", mensagem.getTo()[0]);
        assertTrue(mensagem.getText().contains("foi recebido e aguarda a confirmação do pagamento"));
        assertTrue(mensagem.getText().contains("https://vitafortis.example/pedidos"));
    }

    @Test
    void brevoEnviaOsDoisModelosPorHttps() {
        var mensagens = new java.util.ArrayList<String>();
        configurarBrevo(HttpStatus.CREATED, mensagens);

        service.enviarRecuperacaoSenha("cliente@example.com", "Cliente", "token-seguro");
        service.enviarStatusPedido(42L, "cliente@example.com", "Cliente",
                StatusCompra.ENTREGUE, FormaRecebimento.ENTREGA);

        assertEquals(2, mensagens.size());
        assertTrue(mensagens.get(0).contains("/entrar?recuperar=token-seguro"));
        assertTrue(mensagens.get(1).contains("#42 foi entregue"));
        assertTrue(mensagens.get(1).contains("pedidos@vitafortis.com.br"));
        verifyNoInteractions(sender);
    }

    @Test
    void brevoNaoExpoeDadosDaRespostaDeErro() {
        configurarBrevo(HttpStatus.UNAUTHORIZED, new java.util.ArrayList<>());

        var erro = assertThrows(IllegalStateException.class,
                () -> service.enviarRecuperacaoSenha("cliente@example.com", "Cliente", "token-seguro"));
        assertEquals("A Brevo rejeitou o envio (HTTP 401).", erro.getMessage());
    }

    private void configurarBrevo(HttpStatus status, java.util.List<String> mensagens) {
        var http = RestClient.builder().requestInterceptor((request, body, execution) -> {
            assertEquals("https://api.brevo.com/v3/smtp/email", request.getURI().toString());
            assertEquals("chave-de-teste", request.getHeaders().getFirst("api-key"));
            mensagens.add(new String(body, java.nio.charset.StandardCharsets.UTF_8));
            return new MockClientHttpResponse(new byte[0], status);
        });
        service = new EmailTransacionalService(provider, http);
        ReflectionTestUtils.setField(service, "habilitado", true);
        ReflectionTestUtils.setField(service, "transporte", "brevo");
        ReflectionTestUtils.setField(service, "brevoApiKey", "chave-de-teste");
        ReflectionTestUtils.setField(service, "remetente", "pedidos@vitafortis.com.br");
        ReflectionTestUtils.setField(service, "responderPara", "");
        ReflectionTestUtils.setField(service, "frontendUrl", "https://vitafortis.example");
    }

    @Test
    void desabilitadoNaoUsaSmtp() {
        ReflectionTestUtils.setField(service, "habilitado", false);
        service.enviarStatusPedido(42L, "cliente@example.com", "Cliente",
                StatusCompra.ENTREGUE, FormaRecebimento.ENTREGA);
        verify(sender, never()).send(any(SimpleMailMessage.class));
    }
}
