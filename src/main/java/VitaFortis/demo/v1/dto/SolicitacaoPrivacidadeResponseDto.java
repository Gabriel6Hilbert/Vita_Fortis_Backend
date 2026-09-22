package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.StatusSolicitacaoPrivacidade;
import VitaFortis.demo.v1.enums.TipoSolicitacaoPrivacidade;

import java.time.LocalDateTime;

public record SolicitacaoPrivacidadeResponseDto(
        Long id,
        String protocolo,
        TipoSolicitacaoPrivacidade tipo,
        String descricao,
        StatusSolicitacaoPrivacidade status,
        LocalDateTime criadaEm,
        String decisao,
        String justificativaAdmin,
        LocalDateTime processadaEm
) { }
