package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.StatusSolicitacaoPrivacidade;
import VitaFortis.demo.v1.enums.TipoSolicitacaoPrivacidade;

import java.time.LocalDateTime;

public record SolicitacaoPrivacidadeAdminDto(
        Long id,
        String protocolo,
        Long clienteId,
        String clienteNome,
        String clienteEmail,
        TipoSolicitacaoPrivacidade tipo,
        String descricao,
        StatusSolicitacaoPrivacidade status,
        LocalDateTime criadaEm,
        String decisao,
        String responsavel,
        LocalDateTime processadaEm,
        String justificativaAdmin
) { }
