package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        service = new EmailTransacionalService(provider);
        ReflectionTestUtils.setField(service, "habilitado", true);
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
    void desabilitadoNaoUsaSmtp() {
        ReflectionTestUtils.setField(service, "habilitado", false);
        service.enviarStatusPedido(42L, "cliente@example.com", "Cliente",
                StatusCompra.ENTREGUE, FormaRecebimento.ENTREGA);
        verify(sender, never()).send(any(SimpleMailMessage.class));
    }
}
