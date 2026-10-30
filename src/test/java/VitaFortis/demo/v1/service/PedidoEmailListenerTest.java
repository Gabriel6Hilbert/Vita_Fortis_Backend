package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class PedidoEmailListenerTest {
    @Test
    void falhaDoSmtpNaoDesfazOperacaoDoPedido() {
        EmailTransacionalService emails = mock(EmailTransacionalService.class);
        doThrow(new IllegalStateException("SMTP indisponivel")).when(emails)
                .enviarStatusPedido(anyLong(), anyString(), anyString(), any(), any());
        PedidoEmailListener listener = new PedidoEmailListener(emails);

        assertDoesNotThrow(() -> listener.notificar(new PedidoStatusAlteradoEvent(
                10L, "cliente@example.com", "Cliente", StatusCompra.ENVIADO, FormaRecebimento.ENTREGA)));
    }
}
