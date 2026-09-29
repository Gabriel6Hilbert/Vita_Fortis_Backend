package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.RecuperacaoSenha;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.repository.RecuperacaoSenhaRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecuperacaoSenhaServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RecuperacaoSenhaRepository recuperacoes = mock(RecuperacaoSenhaRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> mailSender = mock(ObjectProvider.class);
    private final RecuperacaoSenhaService service = new RecuperacaoSenhaService(usuarios, recuperacoes, encoder, mailSender);

    @Test
    void redefineSenhaEInvalidaTokensPendentes() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        RecuperacaoSenha atual = recuperacao(usuario, LocalDateTime.now().plusMinutes(10));
        RecuperacaoSenha outra = recuperacao(usuario, LocalDateTime.now().plusMinutes(20));
        when(recuperacoes.findDisponivelForUpdate(anyString())).thenReturn(Optional.of(atual));
        when(recuperacoes.findAllByUsuarioIdAndUtilizadoEmIsNull(7L)).thenReturn(List.of(atual, outra));
        when(encoder.encode("NovaSenha123!")).thenReturn("hash-bcrypt");

        service.redefinir("a".repeat(43), "NovaSenha123!");

        assertEquals("hash-bcrypt", usuario.getSenha());
        assertNotNull(atual.getUtilizadoEm());
        assertNotNull(outra.getUtilizadoEm());
        verify(recuperacoes).findDisponivelForUpdate(anyString());
    }

    @Test
    void marcaTokenExpiradoComoUtilizado() {
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        RecuperacaoSenha expirada = recuperacao(usuario, LocalDateTime.now().minusSeconds(1));
        when(recuperacoes.findDisponivelForUpdate(anyString())).thenReturn(Optional.of(expirada));

        assertThrows(IllegalArgumentException.class,
                () -> service.redefinir("b".repeat(43), "NovaSenha123!"));

        assertNotNull(expirada.getUtilizadoEm());
    }

    private RecuperacaoSenha recuperacao(Usuario usuario, LocalDateTime expiraEm) {
        RecuperacaoSenha recuperacao = new RecuperacaoSenha();
        recuperacao.setUsuario(usuario);
        recuperacao.setExpiraEm(expiraEm);
        recuperacao.setCriadoEm(LocalDateTime.now());
        return recuperacao;
    }
}
