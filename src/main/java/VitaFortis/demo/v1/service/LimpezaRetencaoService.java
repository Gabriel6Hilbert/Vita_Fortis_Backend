package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.repository.RecuperacaoSenhaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class LimpezaRetencaoService {
    private static final Logger log = LoggerFactory.getLogger(LimpezaRetencaoService.class);
    private final RecuperacaoSenhaRepository recuperacoes;

    public LimpezaRetencaoService(RecuperacaoSenhaRepository recuperacoes) { this.recuperacoes = recuperacoes; }

    @Transactional
    @Scheduled(cron = "${vita-fortis.retencao.limpeza-cron:0 20 3 * * *}")
    public void excluirTokensExpirados() {
        long removidos = recuperacoes.deleteByExpiraEmBefore(LocalDateTime.now().minusHours(24));
        if (removidos > 0) log.info("Tokens expirados de recuperação removidos: {}", removidos);
    }
}
