package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.RecuperacaoSenha;
import VitaFortis.demo.v1.repository.RecuperacaoSenhaRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
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
    private final EmailTransacionalService emails;
    private final SecureRandom random = new SecureRandom();

    public RecuperacaoSenhaService(UsuarioRepository usuarios, RecuperacaoSenhaRepository recuperacoes,
                                   PasswordEncoder encoder, EmailTransacionalService emails) {
        this.usuarios = usuarios;
        this.recuperacoes = recuperacoes;
        this.encoder = encoder;
        this.emails = emails;
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

            if (emails.habilitado()) emails.enviarRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), token);
        });
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void redefinir(String token, String novaSenha) {
        RecuperacaoSenha recuperacao = recuperacoes.findDisponivelForUpdate(hash(token))
                .orElseThrow(() -> new IllegalArgumentException("Link de recuperação inválido ou já utilizado."));
        if (recuperacao.getExpiraEm().isBefore(LocalDateTime.now())) {
            recuperacao.setUtilizadoEm(LocalDateTime.now());
            throw new IllegalArgumentException("O link de recuperação expirou. Solicite um novo.");
        }
        recuperacao.getUsuario().setSenha(encoder.encode(novaSenha));
        recuperacao.setUtilizadoEm(LocalDateTime.now());
        recuperacoes.findAllByUsuarioIdAndUtilizadoEmIsNull(recuperacao.getUsuario().getId())
                .forEach(pendente -> pendente.setUtilizadoEm(LocalDateTime.now()));
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
