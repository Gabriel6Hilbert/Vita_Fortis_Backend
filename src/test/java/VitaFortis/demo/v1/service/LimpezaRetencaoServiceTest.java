package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.repository.RecuperacaoSenhaRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class LimpezaRetencaoServiceTest {
    @Test
    void removeTokensMaisAntigosQueVinteEQuatroHorasAposExpiracao() {
        RecuperacaoSenhaRepository repository = mock(RecuperacaoSenhaRepository.class);
        LocalDateTime inicio = LocalDateTime.now();
        new LimpezaRetencaoService(repository).excluirTokensExpirados();
        LocalDateTime fim = LocalDateTime.now();
        verify(repository).deleteByExpiraEmBefore(argThat(limite ->
                !limite.isBefore(inicio.minusHours(24)) && !limite.isAfter(fim.minusHours(24))));
    }
}
