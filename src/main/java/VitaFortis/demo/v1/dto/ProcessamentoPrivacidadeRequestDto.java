package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.StatusSolicitacaoPrivacidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProcessamentoPrivacidadeRequestDto(
        @NotNull StatusSolicitacaoPrivacidade status,
        @NotBlank @Size(max = 500) String decisao,
        @NotBlank @Size(max = 1000) String justificativa
) { }
