package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailTransacionalService {
    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${vita-fortis.email.enabled:false}") private boolean habilitado;
    @Value("${vita-fortis.email.from:nao-responda@vitafortis.com.br}") private String remetente;
    @Value("${vita-fortis.email.reply-to:}") private String responderPara;
    @Value("${vita-fortis.frontend-url:http://localhost:5001}") private String frontendUrl;

    public EmailTransacionalService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public boolean habilitado() {
        return habilitado;
    }

    public void enviarRecuperacaoSenha(String email, String nome, String token) {
        String link = frontendUrl.replaceAll("/$", "") + "/entrar?recuperar=" + token;
        enviar(email, "Redefinição de senha — Vita Fortis",
                "Olá, " + nome + ".\n\nUse o link abaixo para criar uma nova senha. " +
                "Ele expira em 30 minutos e pode ser utilizado uma única vez.\n\n" + link +
                "\n\nSe você não solicitou esta alteração, ignore esta mensagem.");
    }

    public void enviarStatusPedido(Long pedidoId, String email, String nome, StatusCompra status,
                                   FormaRecebimento recebimento) {
        String situacao = switch (status) {
            case PENDENTE -> "foi recebido e aguarda a confirmação do pagamento";
            case PAGAMENTO_APROVADO -> "teve o pagamento aprovado";
            case EM_SEPARACAO -> "está em separação";
            case DISPONIVEL_RETIRADA -> "está disponível para retirada";
            case ENVIADO -> recebimento == FormaRecebimento.ENTREGA
                    ? "saiu para entrega pela equipe Vita Fortis" : "foi atualizado";
            case ENTREGUE -> "foi entregue";
            case CANCELADO -> "foi cancelado";
        };
        enviar(email, "Atualização do pedido #" + pedidoId + " — Vita Fortis",
                "Olá, " + nome + ".\n\nSeu pedido #" + pedidoId + " " + situacao + ".\n\n" +
                "Acompanhe os detalhes e o histórico em " + frontendUrl.replaceAll("/$", "") +
                "/pedidos. Em caso de dúvida, fale com a equipe pelos canais oficiais.");
    }

    private void enviar(String destino, String assunto, String texto) {
        if (!habilitado) return;
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) throw new IllegalStateException("O provedor de e-mail ainda não foi configurado.");
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destino);
        if (responderPara != null && !responderPara.isBlank()) mensagem.setReplyTo(responderPara);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        sender.send(mensagem);
    }
}
