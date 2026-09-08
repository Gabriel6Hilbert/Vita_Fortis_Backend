package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.RecuperacaoSenha;
import VitaFortis.demo.v1.repository.RecuperacaoSenhaRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RecuperacaoSenhaService {
    private final UsuarioRepository usuarios;
    private final RecuperacaoSenhaRepository recuperacoes;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final SecureRandom random = new SecureRandom();

    @Value("${vita-fortis.email.enabled:false}") private boolean emailEnabled;
    @Value("${vita-fortis.email.from:nao-responda@vitafortis.com.br}") private String remetente;
    @Value("${vita-fortis.frontend-url:http://localhost:5001}") private String frontendUrl;

    public RecuperacaoSenhaService(UsuarioRepository usuarios, RecuperacaoSenhaRepository recuperacoes,
                                   PasswordEncoder encoder, ObjectProvider<JavaMailSender> mailSender) {
        this.usuarios = usuarios;
        this.recuperacoes = recuperacoes;
        this.encoder = encoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void solicitar(String email) {
        usuarios.findByEmail(email.trim().toLowerCase()).filter(usuario -> usuario.isAtivo()).ifPresent(usuario -> {
            recuperacoes.findAllByUsuarioIdAndUtilizadoEmIsNull(usuario.getId())
                    .forEach(anterior -> anterior.setUtilizadoEm(LocalDateTime.now()));

            String token = gerarToken();
            RecuperacaoSenha recuperacao = new RecuperacaoSenha();
            recuperacao.setUsuario(usuario);
            recuperacao.setTokenHash(hash(token));
            recuperacao.setCriadoEm(LocalDateTime.now());
            recuperacao.setExpiraEm(LocalDateTime.now().plusMinutes(30));
            recuperacoes.save(recuperacao);

            if (emailEnabled) enviarEmail(usuario.getEmail(), usuario.getNome(), token);
        });
    }

    @Transactional
    public void redefinir(String token, String novaSenha) {
        RecuperacaoSenha recuperacao = recuperacoes.findByTokenHashAndUtilizadoEmIsNull(hash(token))
                .orElseThrow(() -> new IllegalArgumentException("Link de recuperação inválido ou já utilizado."));
        if (recuperacao.getExpiraEm().isBefore(LocalDateTime.now())) {
            recuperacao.setUtilizadoEm(LocalDateTime.now());
            throw new IllegalArgumentException("O link de recuperação expirou. Solicite um novo.");
        }
        recuperacao.getUsuario().setSenha(encoder.encode(novaSenha));
        recuperacao.setUtilizadoEm(LocalDateTime.now());
    }

    private void enviarEmail(String email, String nome, String token) {
        String separador = frontendUrl.contains("?") ? "&" : "?";
        String link = frontendUrl.replaceAll("/$", "") + "/entrar" + separador + "recuperar=" + token;
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("Redefinição de senha — Vita Fortis");
        mensagem.setText("Olá, " + nome + ".\n\nUse o link abaixo para criar uma nova senha. Ele expira em 30 minutos e pode ser utilizado uma única vez.\n\n" + link + "\n\nSe você não solicitou esta alteração, ignore esta mensagem.");
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) throw new IllegalStateException("O provedor de e-mail ainda não foi configurado.");
        sender.send(mensagem);
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception erro) {
            throw new IllegalStateException("Não foi possível proteger o token de recuperação.", erro);
        }
    }
}
