package VitaFortis.demo.v1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PedidoEmailListener {
    private static final Logger log = LoggerFactory.getLogger(PedidoEmailListener.class);
    private final EmailTransacionalService emails;

    public PedidoEmailListener(EmailTransacionalService emails) {
        this.emails = emails;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notificar(PedidoStatusAlteradoEvent evento) {
        try {
            emails.enviarStatusPedido(evento.pedidoId(), evento.email(), evento.nome(),
                    evento.status(), evento.recebimento());
        } catch (RuntimeException erro) {
            log.error("Falha ao enviar e-mail transacional do pedido {}", evento.pedidoId(), erro);
        }
    }
}
