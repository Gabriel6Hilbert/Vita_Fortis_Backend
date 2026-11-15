package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class EmailTransacionalService {
    private final ObjectProvider<JavaMailSender> mailSender;
    private final RestClient brevo;

    @Value("${vita-fortis.email.enabled:false}") private boolean habilitado;
    @Value("${vita-fortis.email.transport:smtp}") private String transporte;
    @Value("${vita-fortis.email.brevo-api-key:}") private String brevoApiKey;
    @Value("${vita-fortis.email.from:nao-responda@vitafortis.com.br}") private String remetente;
    @Value("${vita-fortis.email.reply-to:}") private String responderPara;
    @Value("${vita-fortis.frontend-url:http://localhost:5001}") private String frontendUrl;

    @Autowired
    public EmailTransacionalService(ObjectProvider<JavaMailSender> mailSender) {
        this(mailSender, RestClient.builder());
    }

    EmailTransacionalService(ObjectProvider<JavaMailSender> mailSender, RestClient.Builder http) {
        this.mailSender = mailSender;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.brevo = http.requestFactory(requestFactory).build();
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
        if ("brevo".equals(transporte)) {
            enviarViaBrevo(destino, assunto, texto);
            return;
        }
        if (!"smtp".equals(transporte)) throw new IllegalStateException("Transporte de e-mail inválido.");
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

    private void enviarViaBrevo(String destino, String assunto, String texto) {
        if (brevoApiKey.isBlank()) throw new IllegalStateException("Chave da Brevo não configurada.");
        Map<String, Object> mensagem = new LinkedHashMap<>();
        mensagem.put("sender", Map.of("email", remetente, "name", "Vita Fortis"));
        mensagem.put("to", List.of(Map.of("email", destino)));
        mensagem.put("subject", assunto);
        mensagem.put("textContent", texto);
        if (!responderPara.isBlank()) mensagem.put("replyTo", Map.of("email", responderPara));

        try {
            brevo.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", brevoApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mensagem)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().value() != 201) {
                            throw new IllegalStateException("A Brevo rejeitou o envio (HTTP "
                                    + response.getStatusCode().value() + ").");
                        }
                        return null;
                    });
        } catch (RestClientException erro) {
            throw new IllegalStateException("Não foi possível conectar ao serviço de e-mail da Brevo.");
        }
    }
}
